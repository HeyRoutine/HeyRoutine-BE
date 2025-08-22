from __future__ import annotations
import numpy as np
import pandas as pd
import scipy.sparse as sp
from typing import Optional, Dict, Any, List
from dataclasses import dataclass
from sklearn.preprocessing import normalize
from .db import read_df

@dataclass
class RecsConfig:
    alpha_importance: float = 0.5
    survey_weight: float = 0.8
    use_survey_idf: bool = True
    knn_topk_per_col: int = 100
    shrinkage_beta: float = 50
    neighbors_per_history: int = 100
    final_topk: int = 20
    exclude_already_planned: bool = True
    bm25_k1: float = 1.2
    bm25_b: float = 0.75
    recency_half_life_days: int = 30
    idf_bias: float = 0.2
    drop_common_rate: float = 0.90
    # --- 아이템(popularity) IDF 옵션 ---
    use_item_idf: bool = True              # 켜기/끄기
    item_idf_bias: float = 0.1             # log(1 + U/(1+df))에 더하는 bias
    item_idf_power: float = 0.5            # sqrt(idf) 효과(과도한 패널티 방지)
    item_idf_cap_min: float = 0.8         # 가중 하한(선택)
    item_idf_cap_max: float = 1.8          # 가중 상한(선택)
    item_idf_apply_to_features: bool = False  # F: 설문 피처 열엔 적용 안 함


def _find(cols: List[str], keys: List[str]) -> Optional[str]:
    L = [c.lower() for c in cols]
    for k in keys:
        for i, c in enumerate(L):
            if k in c:
                return cols[i]
    return None

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

    # --------- Build from MySQL (no CSV) ---------
    @classmethod
    def from_mysql(cls, schema: str,
                   plan_table: Optional[str] = None,
                   master_table: Optional[str] = None,
                   survey_table: Optional[str] = None,
                   plan_sql: Optional[str] = None,
                   master_sql: Optional[str] = None,
                   survey_sql: Optional[str] = None,
                   config: Optional[RecsConfig] = None) -> "HybridKNNRecommender":
        self = cls(config)

        # Build SQL if only table names given
        if not plan_sql:
            if not plan_table:
                raise RuntimeError("DB 모드: PLAN_TABLE 또는 PLAN_SQL 필요")
            plan_sql = f"SELECT * FROM `{schema}`.`{plan_table}`"
        if not master_sql:
            if not master_table:
                raise RuntimeError("DB 모드: MASTER_TABLE 또는 MASTER_SQL 필요")
            master_sql = f"SELECT * FROM `{schema}`.`{master_table}`"
        if not survey_sql and survey_table:
            survey_sql = f"SELECT * FROM `{schema}`.`{survey_table}`"

        plan_df   = read_df(plan_sql)
        master_df = read_df(master_sql)
        survey_df = read_df(survey_sql) if survey_sql else pd.DataFrame({"user_id": []})

        self._load_and_prepare_from_dfs(plan_df, master_df, survey_df)
        self._build_matrices()
        self._build_similarity()
        return self

    # --------- Internal: data prep from DataFrames ---------
    def _load_and_prepare_from_dfs(self, plan: pd.DataFrame, master: pd.DataFrame, survey: pd.DataFrame):
        # drop noisy cols in survey
        survey = survey.drop(columns=['update_at', 'updated_at'], errors='ignore')

        # plan columns normalize
        p_user = _find(plan.columns, ["user_id","userid","member_id","user"])
        p_item = _find(plan.columns, ["routine_id","routine","rt_id","rtid"])
        p_imp  = _find(plan.columns, ["importance","priority","weight","score"])
        p_dow  = _find(plan.columns, ["dow","weekday","dayofweek"])
        p_hour = _find(plan.columns, ["hour","hh"])
        p_created = _find(plan.columns, ["created_at","create_at","createdat","reg_date","inserted_at","timestamp","ts"])

        if p_user and p_user != "user_id":    plan = plan.rename(columns={p_user:"user_id"})
        if p_item and p_item != "routine_id": plan = plan.rename(columns={p_item:"routine_id"})
        if p_imp and p_imp != "importance":   plan = plan.rename(columns={p_imp:"importance"})
        if p_dow and p_dow != "dow":          plan = plan.rename(columns={p_dow:"dow"})
        if p_hour and p_hour != "hour":       plan = plan.rename(columns={p_hour:"hour"})
        if p_created and p_created != "created_at": plan = plan.rename(columns={p_created:"created_at"})

        # master columns normalize
        m_item = _find(master.columns, ["routine_id","routine","rt_id","rtid","id"])
        m_cat  = _find(master.columns, ["category","tag","type"])
        m_name = _find(master.columns, ["title","name"])
        if m_item and m_item != "routine_id": master = master.rename(columns={m_item:"routine_id"})
        if m_cat and m_cat != "category":     master = master.rename(columns={m_cat:"category"})
        if m_name and m_name != "title":      master = master.rename(columns={m_name:"title"})

        # types
        plan["user_id"] = plan["user_id"].astype(str)
        plan["routine_id"] = plan["routine_id"].astype(str)
        master["routine_id"] = master["routine_id"].astype(str)

        # ensure numeric importance
        if "importance" in plan.columns:
            plan["importance"] = pd.to_numeric(plan["importance"], errors="coerce").fillna(0.0).astype("float32")

        # parse created_at safely; if invalid, drop it
        has_created = "created_at" in plan.columns
        if has_created:
            plan["created_at"] = pd.to_datetime(plan["created_at"], errors="coerce")
            if plan["created_at"].notna().sum() == 0:
                plan = plan.drop(columns=["created_at"])
                has_created = False

        # --- 1) plan-based scores (BM25 + recency + importance) ---
        cfg = self.cfg
        u_len = plan.groupby("user_id")["routine_id"].size().rename("u_len")
        g = plan.merge(u_len, on="user_id", how="left")
        g["tf"] = 1.0

        agg_dict = {
            "tf": ("tf", "sum"),
            "mean_importance": ("importance", "mean") if "importance" in g.columns else ("tf", "sum"),
        }
        if has_created:
            agg_dict["last_ts"] = ("created_at", "max")
        else:
            agg_dict["last_ts"] = ("routine_id", "size")  # fallback numeric proxy

        agg = g.groupby(["user_id","routine_id","u_len"]).agg(**agg_dict).reset_index()

        avg_len = float(u_len.mean()) if len(u_len)>0 else 1.0
        bm25 = ((cfg.bm25_k1 + 1.0) * agg["tf"]) / (cfg.bm25_k1 * (1 - cfg.bm25_b + cfg.bm25_b * (agg["u_len"] / max(avg_len, 1e-6))) + agg["tf"])

        # recency
        if has_created:
            max_ts = pd.to_datetime(plan["created_at"]).max()
            # If still NaT (just in case), neutralize recency
            if pd.isna(max_ts):
                recency = 1.0
            else:
                days = (max_ts - pd.to_datetime(agg["last_ts"])).dt.days.astype("float32")
                days = days.clip(lower=0).fillna(days.max() if len(days)>0 else 0)
                recency = (0.5 ** (days / max(cfg.recency_half_life_days, 1))).astype("float32")
        else:
            recency = 1.0

        agg["mean_importance"] = pd.to_numeric(agg["mean_importance"], errors="coerce").fillna(0.0).astype("float32")
        agg["score"] = bm25 * (0.5 + cfg.alpha_importance * (agg["mean_importance"]/5.0)) * recency
        g_plan = agg[["user_id","routine_id","score"]].copy()

        # --- 2) survey → feature items ---
        if "user_id" not in survey.columns:
            survey_long = pd.DataFrame(columns=["user_id","flag","idf","feature_id","score"])
        else:
            survey_cols = [c for c in survey.columns if c.lower() != "user_id"]
            survey_long = survey.melt(id_vars=["user_id"], value_vars=survey_cols, var_name="flag", value_name="val")
            def to01(v):
                if pd.isna(v): return 0
                try:
                    return int(float(str(v).strip()) > 0)
                except Exception:
                    return 0
            survey_long["user_id"] = survey_long["user_id"].astype(str)
            survey_long["val01"] = survey_long["val"].apply(to01).astype("int8")
            survey_long = survey_long[survey_long["val01"] == 1].copy()

            if self.cfg.use_survey_idf and len(survey_long) > 0:
                N = survey_long["user_id"].nunique()
                df = survey_long.groupby("flag")["user_id"].nunique().rename("df").reset_index()
                df["rate"] = df["df"] / max(N, 1)
                df = df[df["rate"] <= self.cfg.drop_common_rate]
                df["idf"] = np.log(1 + (N / (1 + df["df"]))) + self.cfg.idf_bias
                survey_long = survey_long.merge(df[["flag","idf"]], on="flag", how="inner")
                survey_long["idf"] = survey_long["idf"].fillna(0.0).astype("float32")
            else:
                survey_long["idf"] = 1.0

            survey_long["feature_id"] = "F:" + survey_long["flag"].astype(str)
            survey_long["score"] = (self.cfg.survey_weight * survey_long["idf"].astype(float)).astype("float32")

        # store
        self.master = master
        self.plan = plan
        self.survey_long = survey_long
        self._g_plan = g_plan

    def _build_matrices(self):
        g_plan = self._g_plan
        survey_long = self.survey_long

        users = pd.Index(
            sorted(set(g_plan["user_id"].astype(str)).union(set(survey_long["user_id"].astype(str)))),
            name="user_id"
        )
        routines_idx = pd.Index(g_plan["routine_id"].astype(str).unique())
        features_idx = pd.Index(survey_long["feature_id"].astype(str).unique()) if "feature_id" in survey_long.columns else pd.Index([], dtype=object)
        items = routines_idx.union(features_idx)
        items.name = "col_id"

        uid2idx = {u: i for i, u in enumerate(users)}
        col2idx = {c: i for i, c in enumerate(items)}
        idx2col = {i: c for c, i in col2idx.items()}
        is_routine = np.array([not str(s).startswith("F:") for s in items], dtype=bool)

        row_u = g_plan["user_id"].astype(str).map(uid2idx).values
        col_i = g_plan["routine_id"].astype(str).map(col2idx).values
        dat_s = g_plan["score"].astype(float).values

        if len(survey_long) > 0:
            row_u2 = survey_long["user_id"].astype(str).map(uid2idx).values
            col_f = survey_long["feature_id"].astype(str).map(col2idx).values
            dat_f = survey_long["score"].astype(float).values
            rows = np.concatenate([row_u, row_u2])
            cols = np.concatenate([col_i, col_f])
            data = np.concatenate([dat_s, dat_f])
        else:
            rows, cols, data = row_u, col_i, dat_s

        X = sp.csr_matrix((data, (rows, cols)), shape=(len(users), len(items)))

        self.users, self.items = users, items
        self.uid2idx, self.col2idx, self.idx2col = uid2idx, col2idx, idx2col
        self.is_routine = is_routine
        self.X = X

    def _build_similarity(self):
        assert self.X is not None
        cfg = self.cfg

        X = self.X.tocsr()

        # 1) 열 L2 정규화
        Xn = normalize(X, norm="l2", axis=0, copy=True)

        # 2) IDF 가중 계산
        if cfg.use_item_idf:
            X_bin = X.copy(); X_bin.data = np.ones_like(X_bin.data)
            df = np.asarray(X_bin.sum(axis=0)).ravel().astype(np.float32)  # 각 열의 사용자 수
            U = X.shape[0]
            idf = np.log1p(U / (1.0 + df)) + float(cfg.item_idf_bias)

            # 설문 피처(F:)엔 적용 안 함(옵션)
            if not cfg.item_idf_apply_to_features and self.is_routine is not None:
                mask_item = self.is_routine.astype(bool)
                w = np.ones_like(idf, dtype=np.float32)
                w[mask_item] = idf[mask_item]
            else:
                w = idf.astype(np.float32)

            # 과벌점 완화: power + clip
            if cfg.item_idf_power is not None:
                w = np.power(w, float(cfg.item_idf_power)).astype(np.float32)
            if (cfg.item_idf_cap_min is not None) or (cfg.item_idf_cap_max is not None):
                mn = cfg.item_idf_cap_min if cfg.item_idf_cap_min is not None else -np.inf
                mx = cfg.item_idf_cap_max if cfg.item_idf_cap_max is not None else  np.inf
                w = np.clip(w, mn, mx).astype(np.float32)

            W = sp.diags(w, offsets=0, format="csr")

            # 3) 유사도 계산에 가중 반영 (오른쪽에만 적용: 허브 디바이싱 효과)
            # 대칭 가중을 원하면: S_cos = (W @ (Xn.T @ Xn) @ W).tocsr()
            S_cos = (Xn.T @ (Xn @ W)).tocsr()
        else:
            S_cos = (Xn.T @ Xn).tocsr()

        # 4) shrinkage
        if cfg.shrinkage_beta > 0:
            X_bin = self.X.copy(); X_bin.data = np.ones_like(X_bin.data)
            C = (X_bin.T @ X_bin).tocsr()
            Ccoo = C.tocoo()
            shrink_vals = Ccoo.data / (Ccoo.data + cfg.shrinkage_beta)

            Scoo = S_cos.tocoo()
            from collections import defaultdict
            d = defaultdict(float)
            for r, c, v in zip(Ccoo.row, Ccoo.col, shrink_vals):
                d[(r, c)] = v
            new_data = np.array(
                [v * d.get((r, c), 0.0) for r, c, v in zip(Scoo.row, Scoo.col, Scoo.data)],
                dtype=np.float32
            )
            S = sp.csr_matrix((new_data, (Scoo.row, Scoo.col)), shape=S_cos.shape)
        else:
            S = S_cos.tocsr()

        S.setdiag(0.0)
        S.eliminate_zeros()
        self.S_topk = self._topk_rows_sparse(S, cfg.knn_topk_per_col)


    @staticmethod
    def _topk_rows_sparse(M: sp.csr_matrix, k: int) -> sp.csr_matrix:
        M = M.tocsr().astype(np.float32)
        rows, cols, data = [], [], []
        for i in range(M.shape[0]):
            s, e = M.indptr[i], M.indptr[i+1]
            idx = M.indices[s:e]; val = M.data[s:e]
            if val.size == 0:
                continue
            if val.size > k:
                sel = np.argpartition(val, -k)[-k:]
                sel = sel[np.argsort(-val[sel])]
            else:
                sel = np.argsort(-val)
            rows.extend([i]*len(sel)); cols.extend(idx[sel].tolist()); data.extend(val[sel].tolist())
        return sp.csr_matrix((np.array(data,dtype=np.float32),(np.array(rows),np.array(cols))), shape=M.shape)

    def _score_candidates(self, u_idx: int, zero_out_owned: bool = True) -> np.ndarray:
        row = self.X.getrow(u_idx).tocsr()
        nz_cols, nz_vals = row.indices, row.data
        if nz_cols.size == 0:
            return np.zeros(self.X.shape[1], dtype=np.float32)
        acc = np.zeros(self.X.shape[1], dtype=np.float32)
        for j, ruj in zip(nz_cols, nz_vals):
            s, e = self.S_topk.indptr[j], self.S_topk.indptr[j+1]
            cols, vals = self.S_topk.indices[s:e], self.S_topk.data[s:e]
            if cols.size == 0:
                continue
            use = cols[:self.cfg.neighbors_per_history]
            acc[use] += (vals[:use.size] * ruj).astype(np.float32)
        if zero_out_owned:
            acc[nz_cols] = 0.0
        return acc

    def recommend(self, user_id: str, topk: Optional[int]=None,
                  exclude_already_planned: Optional[bool]=None,
                  allow_owned: bool = False) -> pd.DataFrame:
        if topk is None:
            topk = self.cfg.final_topk
        if exclude_already_planned is None:
            exclude_already_planned = self.cfg.exclude_already_planned

        u_idx = self.uid2idx.get(str(user_id))
        if u_idx is None:
            return pd.DataFrame(columns=["routine_id","title","category","score"])

        scores = self._score_candidates(u_idx, zero_out_owned=(not allow_owned))

        ridxs = np.where(self.is_routine)[0]
        rscores = scores[ridxs]
        if rscores.size == 0:
            return pd.DataFrame(columns=["routine_id","title","category","score"])
        k = min(topk, rscores.size)
        top = np.argpartition(-rscores, k-1)[:k]
        order = top[np.argsort(-rscores[top])]
        chosen = ridxs[order]
        out = pd.DataFrame({
            "routine_id": [self.idx2col[i] for i in chosen],
            "score": rscores[order]
        })
        cols = ["routine_id"]
        if self.master is not None:
            if "title" in self.master.columns: cols.append("title")
            if "category" in self.master.columns: cols.append("category")
            out = out.merge(self.master[cols].drop_duplicates("routine_id"), on="routine_id", how="left")

        if exclude_already_planned and self.plan is not None:
            planned = set(self.plan[self.plan["user_id"].astype(str)==str(user_id)]["routine_id"].astype(str))
            out = out[~out["routine_id"].astype(str).isin(planned)]

        return out.reset_index(drop=True)
