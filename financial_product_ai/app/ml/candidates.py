
import numpy as np, pandas as pd

def _to_series(x):
    if isinstance(x, pd.DataFrame):
        return x.iloc[0] if not x.empty else pd.Series()
    return x

def rule_filter_candidates_relaxed(df_prod: pd.DataFrame, user_feat,
                                   min_per_type: int = 10, relax_factor: float = 2.0) -> pd.DataFrame:
    uf = _to_series(user_feat)
    p = df_prod.copy()
    if "interesRate" not in p.columns and "interestRate" in p.columns:
        p = p.rename(columns={"interestRate": "interesRate"})
    p["minSubscriptionBalance"] = pd.to_numeric(p.get("minSubscriptionBalance", 0.0), errors="coerce").fillna(0.0)
    p["interesRate"] = pd.to_numeric(p.get("interesRate", 0.0), errors="coerce").fillna(0.0)
    p["subscriptionPeriod"] = pd.to_numeric(p.get("subscriptionPeriod", 0), errors="coerce").fillna(0).astype(int)
    p["type"] = p["type"].astype(str)

    base = uf.get("r3_bal_mean", np.nan)
    if pd.isna(base): base = uf.get("bal_mean", 0.0)
    base = 0.0 if pd.isna(base) else float(base)
    dep_cap = max(0.0, 0.4 * base)

    monthly_surplus = max(0.0, float(uf.get("sum_in", 0.0) - uf.get("sum_out", 0.0)))
    sav_cap = 0.30 * monthly_surplus

    dep_mask = (p["type"] == "deposit") & (p["minSubscriptionBalance"] <= dep_cap)
    sav_mask = (p["type"] == "savings") & (p["minSubscriptionBalance"] <= sav_cap)
    base_df = p[dep_mask | sav_mask].copy()

    def _ensure(tp: str, cap: float):
        nonlocal base_df
        cur = base_df[base_df["type"] == tp]
        if len(cur) >= min_per_type: return
        need = min_per_type - len(cur)
        relax_cap = max(cap * relax_factor, 1.0)
        pool_relax = p[(p["type"] == tp) & (p["minSubscriptionBalance"] <= relax_cap)]
        if not pool_relax.empty and need > 0:
            add = pool_relax.sort_values(["interesRate","subscriptionPeriod"], ascending=[False, True]).head(need)
            base_df = pd.concat([base_df, add], ignore_index=True)
            cur = base_df[base_df["type"] == tp]; need = min_per_type - len(cur)
        if need > 0:
            add2 = p[p["type"] == tp].sort_values(["interesRate","subscriptionPeriod"], ascending=[False, True]).head(need)
            base_df = pd.concat([base_df, add2], ignore_index=True)

    _ensure("deposit", dep_cap)
    _ensure("savings", sav_cap)

    if "accountTypeCode" in base_df.columns:
        base_df["accountTypeCode"] = pd.to_numeric(base_df["accountTypeCode"], errors="coerce")
        base_df = base_df.dropna(subset=["accountTypeCode"])
        base_df["accountTypeCode"] = base_df["accountTypeCode"].astype("Int64")
        base_df = base_df.drop_duplicates(subset=["accountTypeCode"])

    out = base_df.sort_values(["type","interesRate","subscriptionPeriod"], ascending=[True, False, True]).reset_index(drop=True)
    return out
