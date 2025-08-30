from __future__ import annotations
import numpy as np
import pandas as pd
import scipy.sparse as sp
from typing import Optional, Dict
from dataclasses import dataclass
from sklearn.preprocessing import normalize
from .db import read_df


@dataclass
class RecsConfig:
    # 계획(plan) 점수 가중
    alpha_importance: float = 0.5           # importance 반영 비율
    bm25_k1: float = 1.2                    # BM25 k1
    bm25_b: float = 0.75                    # BM25 b

    # 설문(feature) 가중
    survey_weight: float = 0.8              # 설문 피처 기본 가중
    use_survey_idf: bool = True             # 설문 피처 IDF 사용
    idf_bias: float = 0.2                   # log(1 + N/(1+df)) + bias
    drop_common_rate: float = 0.90          # 너무 흔한 설문 피처 제거 비율 상한

    # 아이템(루틴) IDF(허브 디바이싱)
    use_item_idf: bool = True
    item_idf_bias: float = 0.1
    item_idf_power: float = 0.5             # 제곱근 효과
    item_idf_cap_min: float = 0.8
    item_idf_cap_max: float = 1.8
    item_idf_apply_to_features: bool = False  # 설문 피처엔 기본 미적용

    # KNN/후처리
    knn_topk_per_col: int = 100
    shrinkage_beta: float = 50
    neighbors_per_history: int = 100
    final_topk: int = 20
    exclude_already_planned: bool = True


class HybridKNNRecommender:
    def __init__(self, config: Optional[RecsConfig] = None):
        self.cfg = config or RecsConfig()
        self.master: Optional[pd.DataFrame] = None
        self.plan: Optional[pd.DataFrame] = None
        self.survey_long: Optional[pd.DataFrame] = None

        self.users: Optional[pd.Index] = None
        self.items: Optional[pd.Index] = None
        self.uid2idx: Dict[str, int] = {}
        self.col2idx: Dict[str, int] = {}
        self.idx2col: Dict[int, str] = {}
        self.is_routine: Optional[np.ndarray] = None

        self.X: Optional[sp.csr_matrix] = None
        self.S_topk: Optional[sp.csr_matrix] = None
        self._g_plan: Optional[pd.DataFrame] = None

    @classmethod
    def from_mysql(cls,
                schema: str,
                plan_table: Optional[str] = "user_weekly_plan_with_routine",
                master_table: Optional[str] = "template",
                survey_table: Optional[str] = "user_survey_flags",
                plan_sql: Optional[str] = None,
                master_sql: Optional[str] = None,
                survey_sql: Optional[str] = None,
                config: Optional[RecsConfig] = None) -> "HybridKNNRecommender":
        
        self = cls(config)

        # 1) SQL 우선, 없으면 테이블 기반 최소 컬럼 SELECT
        if plan_sql is None:
            if plan_table is None:
                raise RuntimeError("PLAN_SQL 또는 PLAN_TABLE을 지정하세요.")
            plan_sql = f"""
                SELECT `user_id`, `importance`, `routine_name`
                FROM `{schema}`.`{plan_table}`
            """

        if master_sql is None:
            if master_table is None:
                raise RuntimeError("MASTER_SQL 또는 MASTER_TABLE을 지정하세요.")
            master_sql = f"""
                SELECT `template_id`, `category`, `routine_name`
                FROM `{schema}`.`{master_table}`
            """

        if survey_sql is None:
            if survey_table is None:
                # 설문을 안 쓰는 경우: 빈 DF로 처리
                survey_df = pd.DataFrame({"user_id": []})
            else:
                survey_sql = f"""
                    SELECT *
                    FROM `{schema}`.`{survey_table}`
                """


        # 2) 로드
        plan_df   = read_df(plan_sql)
        master_df = read_df(master_sql)
        if isinstance(survey_sql, str):
            survey_df = read_df(survey_sql)
        elif "survey_df" not in locals():
            survey_df = pd.DataFrame({"user_id": []})
        
        

        # 3) 전처리 & 빌드
        self._load_and_prepare_from_dfs(plan_df, master_df, survey_df)
        self._build_matrices()
        self._build_similarity()
        return self
    


    # -------------------- Preparation --------------------
    def _load_and_prepare_from_dfs(self,
                                   plan: pd.DataFrame,
                                   master: pd.DataFrame,
                                   survey: pd.DataFrame):
        # types
        plan["user_id"] = plan["user_id"].astype(str)
        plan["importance"] = pd.to_numeric(plan["importance"], errors="coerce").fillna(0.0).astype("float32")
        master["template_id"] = master["template_id"].astype(str)
        master = master.rename(columns={'template_id': 'routine_id'})
        


        # plan: routine_name -> routine_id 매핑
        # (routine_name 중복 시 첫 행 사용)
        m_map = master[["routine_name", "routine_id"]].drop_duplicates("routine_name")
        plan = plan.merge(m_map, on="routine_name", how="left")
        plan = plan.dropna(subset=["routine_id"]).copy()
        plan["routine_id"] = plan["routine_id"].astype(str)


        # --- (1) plan 기반 점수 (BM25 + importance) ---
        cfg = self.cfg
        if len(plan) == 0:
            g_plan = pd.DataFrame(columns=["user_id", "routine_id", "score"])
        else:
            u_len = plan.groupby("user_id")["routine_id"].size().rename("u_len")
            g = plan.merge(u_len, on="user_id", how="left")
            g["tf"] = 1.0

            agg = g.groupby(["user_id", "routine_id", "u_len"]).agg(
                tf=("tf", "sum"),
                mean_importance=("importance", "mean")
            ).reset_index()

            avg_len = float(u_len.mean()) if len(u_len) > 0 else 1.0
            bm25 = ((cfg.bm25_k1 + 1.0) * agg["tf"]) / (
                cfg.bm25_k1 * (1 - cfg.bm25_b + cfg.bm25_b * (agg["u_len"] / max(avg_len, 1e-6))) + agg["tf"]
            )

            agg["mean_importance"] = pd.to_numeric(agg["mean_importance"], errors="coerce").fillna(0.0).astype("float32")
            agg["score"] = bm25 * (0.5 + cfg.alpha_importance * (agg["mean_importance"] / 5.0))
            g_plan = agg[["user_id", "routine_id", "score"]].copy()

        # --- (2) survey → long-format feature items ---
        if "user_id" not in survey.columns:
            survey_long = pd.DataFrame(columns=["user_id", "flag", "idf", "feature_id", "score"])
        else:
            survey_cols = [c for c in survey.columns if c.lower() != "user_id"]
            if len(survey_cols) == 0:
                survey_long = pd.DataFrame(columns=["user_id", "flag", "idf", "feature_id", "score"])
            else:
                survey_long = survey.melt(id_vars=["user_id"],
                                          value_vars=survey_cols,
                                          var_name="flag", value_name="val")
                survey_long["user_id"] = survey_long["user_id"].astype(str)

                def to01(v):
                    try:
                        return int(float(str(v).strip()) > 0)
                    except Exception:
                        return 0

                survey_long["val01"] = survey_long["val"].apply(to01).astype("int8")
                survey_long = survey_long[survey_long["val01"] == 1].copy()

                # IDF (너무 흔한 피처는 제거)
                if self.cfg.use_survey_idf and len(survey_long) > 0:
                    N = survey_long["user_id"].nunique()
                    df = survey_long.groupby("flag")["user_id"].nunique().rename("df").reset_index()
                    df["rate"] = df["df"] / max(N, 1)
                    df = df[df["rate"] <= self.cfg.drop_common_rate]
                    df["idf"] = np.log(1 + (N / (1 + df["df"]))) + self.cfg.idf_bias
                    survey_long = survey_long.merge(df[["flag", "idf"]], on="flag", how="inner")
                    survey_long["idf"] = survey_long["idf"].fillna(0.0).astype("float32")
                else:
                    survey_long["idf"] = 1.0

                survey_long["feature_id"] = "F:" + survey_long["flag"].astype(str)
                survey_long["score"] = (self.cfg.survey_weight * survey_long["idf"].astype(float)).astype("float32")

        # store
        self.master = master.copy()
        self.plan = plan.copy()
        self.survey_long = survey_long
        self._g_plan = g_plan
        self._survey_users_all = (
            survey["user_id"].astype(str).str.strip().unique().tolist()
            if "user_id" in survey.columns else []
        )

    # -------------------- Matrices --------------------
    def _build_matrices(self):
        g_plan = self._g_plan if self._g_plan is not None else pd.DataFrame(columns=["user_id", "routine_id", "score"])
        survey_long = self.survey_long if self.survey_long is not None else pd.DataFrame(columns=["user_id", "feature_id", "score"])

        survey_users_all = set(getattr(self, "_survey_users_all", []))
        users = pd.Index(
            sorted(
                set(g_plan["user_id"].astype(str)) |
                set(survey_long["user_id"].astype(str)) |
                survey_users_all
            ),
            name="user_id"
        )

        routines_idx = pd.Index(g_plan["routine_id"].astype(str).unique())
        features_idx = pd.Index(survey_long["feature_id"].astype(str).unique()) if "feature_id" in survey_long.columns else pd.Index([], dtype=object)
        items = routines_idx.union(features_idx)
        items.name = "col_id"

        uid2idx = {u: i for i, u in enumerate(users)}
        col2idx = {c: i for i, c in enumerate(items)}
        idx2col = {i: c for c, i in col2idx.items()}
        is_routine = np.array([not str(cid).startswith("F:") for cid in items], dtype=bool)

        # build X
        rows, cols, data = [], [], []

        if len(g_plan) > 0:
            rows.extend(g_plan["user_id"].astype(str).map(uid2idx).tolist())
            cols.extend(g_plan["routine_id"].astype(str).map(col2idx).tolist())
            data.extend(g_plan["score"].astype(float).tolist())

        if len(survey_long) > 0:
            rows.extend(survey_long["user_id"].astype(str).map(uid2idx).tolist())
            cols.extend(survey_long["feature_id"].astype(str).map(col2idx).tolist())
            data.extend(survey_long["score"].astype(float).tolist())

        if len(users) == 0 or len(items) == 0:
            X = sp.csr_matrix((0, 0), dtype=np.float32)
        else:
            X = sp.csr_matrix((np.array(data, dtype=np.float32),
                               (np.array(rows, dtype=np.int32), np.array(cols, dtype=np.int32))),
                              shape=(len(users), len(items)))

        self.users, self.items = users, items
        self.uid2idx, self.col2idx, self.idx2col = uid2idx, col2idx, idx2col
        self.is_routine = is_routine
        self.X = X

    # -------------------- Similarity --------------------
    def _build_similarity(self):
        assert self.X is not None
        X = self.X.tocsr()
        cfg = self.cfg

        # 열 L2 정규화
        Xn = normalize(X, norm="l2", axis=0, copy=True)

        # 아이템 IDF 가중 (루틴 열에만 적용; 설문 피처 제외가 기본)
        if cfg.use_item_idf and X.shape[1] > 0:
            X_bin = X.copy(); X_bin.data = np.ones_like(X_bin.data)
            df = np.asarray(X_bin.sum(axis=0)).ravel().astype(np.float32)  # 각 열을 가진 사용자 수
            U = float(X.shape[0])
            idf = np.log1p(U / (1.0 + df)) + float(cfg.item_idf_bias)

            if not cfg.item_idf_apply_to_features and self.is_routine is not None:
                w = np.ones_like(idf, dtype=np.float32)
                w[self.is_routine] = idf[self.is_routine]
            else:
                w = idf.astype(np.float32)

            if cfg.item_idf_power is not None:
                w = np.power(w, float(cfg.item_idf_power)).astype(np.float32)
            if (cfg.item_idf_cap_min is not None) or (cfg.item_idf_cap_max is not None):
                mn = cfg.item_idf_cap_min if cfg.item_idf_cap_min is not None else -np.inf
                mx = cfg.item_idf_cap_max if cfg.item_idf_cap_max is not None else  np.inf
                w = np.clip(w, mn, mx).astype(np.float32)

            W = sp.diags(w, offsets=0, format="csr")
            S_cos = (Xn.T @ (Xn @ W)).tocsr()
        else:
            S_cos = (Xn.T @ Xn).tocsr()

        # shrinkage: 공사용자수 / (공사용자수 + beta)
        if cfg.shrinkage_beta > 0 and S_cos.nnz > 0:
            Xb = X.copy(); Xb.data = np.ones_like(Xb.data)
            C = (Xb.T @ Xb).tocsr().tocoo()
            # (row,col) → shrink 값 딕셔너리
            shrink = {(r, c): v / (v + cfg.shrinkage_beta) for r, c, v in zip(C.row, C.col, C.data)}

            Scoo = S_cos.tocoo()
            new_data = np.array([val * shrink.get((r, c), 0.0)
                                 for r, c, val in zip(Scoo.row, Scoo.col, Scoo.data)],
                                dtype=np.float32)
            S = sp.csr_matrix((new_data, (Scoo.row, Scoo.col)), shape=S_cos.shape)
        else:
            S = S_cos.tocsr()

        S.setdiag(0.0)
        S.eliminate_zeros()
        self.S_topk = self._topk_rows_sparse(S, cfg.knn_topk_per_col)

    @staticmethod
    def _topk_rows_sparse(M: sp.csr_matrix, k: int) -> sp.csr_matrix:
        M = M.tocsr().astype(np.float32)
        if M.shape[0] == 0 or M.nnz == 0:
            return sp.csr_matrix(M.shape, dtype=np.float32)

        rows, cols, data = [], [], []
        for i in range(M.shape[0]):
            s, e = M.indptr[i], M.indptr[i + 1]
            idx = M.indices[s:e]; val = M.data[s:e]
            if val.size == 0:
                continue
            if val.size > k:
                sel = np.argpartition(val, -k)[-k:]
                sel = sel[np.argsort(-val[sel])]
            else:
                sel = np.argsort(-val)
            rows.extend([i] * len(sel))
            cols.extend(idx[sel].tolist())
            data.extend(val[sel].tolist())

        return sp.csr_matrix(
            (np.array(data, dtype=np.float32),
             (np.array(rows, dtype=np.int32), np.array(cols, dtype=np.int32))),
            shape=M.shape
        )

    # -------------------- Inference --------------------
    def _score_candidates(self, u_idx: int, zero_out_owned: bool = True) -> np.ndarray:
        if self.X is None or self.S_topk is None:
            return np.zeros(0, dtype=np.float32)

        row = self.X.getrow(u_idx).tocsr()
        nz_cols, nz_vals = row.indices, row.data
        if nz_cols.size == 0:
            return np.zeros(self.X.shape[1], dtype=np.float32)

        acc = np.zeros(self.X.shape[1], dtype=np.float32)
        for j, ruj in zip(nz_cols, nz_vals):
            s, e = self.S_topk.indptr[j], self.S_topk.indptr[j + 1]
            cols, vals = self.S_topk.indices[s:e], self.S_topk.data[s:e]
            if cols.size == 0:
                continue
            use = cols[:self.cfg.neighbors_per_history]
            acc[use] += (vals[:use.size] * ruj).astype(np.float32)

        if zero_out_owned:
            acc[nz_cols] = 0.0
        return acc

    def recommend(self,
                  user_id: str,
                  topk: Optional[int] = None,
                  exclude_already_planned: Optional[bool] = None,
                  allow_owned: bool = False) -> pd.DataFrame:
        if topk is None:
            topk = self.cfg.final_topk
        if exclude_already_planned is None:
            exclude_already_planned = self.cfg.exclude_already_planned

        u_idx = self.uid2idx.get(str(user_id))
        if u_idx is None:
            return pd.DataFrame(columns=["routine_id", "routine_name", "category", "score"])

        scores = self._score_candidates(u_idx, zero_out_owned=(not allow_owned))

        # 루틴 열만 선택
        ridxs = np.where(self.is_routine)[0]
        if ridxs.size == 0:
            return pd.DataFrame(columns=["routine_id", "routine_name", "category", "score"])

        rscores = scores[ridxs]
        k = int(min(topk, rscores.size))
        if k <= 0:
            return pd.DataFrame(columns=["routine_id", "routine_name", "category", "score"])

        sel = np.argpartition(-rscores, k - 1)[:k]
        order = sel[np.argsort(-rscores[sel])]
        chosen = ridxs[order]
        out = pd.DataFrame({
            "routine_id": [self.idx2col[i] for i in chosen],
            "score": rscores[order]
        })

        # 메타데이터 조인
        meta = self.master[["routine_id", "routine_name", "category"]].drop_duplicates("routine_id")
        out = out.merge(meta, on="routine_id", how="left")

        # 이미 계획된 루틴 제외
        if exclude_already_planned and self.plan is not None and len(self.plan) > 0:
            owned = set(self.plan[self.plan["user_id"].astype(str) == str(user_id)]["routine_id"].astype(str))
            if owned:
                out = out[~out["routine_id"].astype(str).isin(owned)]

        return out.reset_index(drop=True)
