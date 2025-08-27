
import numpy as np, pandas as pd

def build_user_features_multi(df_tx_all: pd.DataFrame) -> pd.DataFrame:
    def _one(g: pd.DataFrame) -> pd.Series:
        t = g.copy()
        if not np.issubdtype(t["ts"].dtype, np.datetime64):
            t["ts"] = pd.to_datetime(t["ts"], errors="coerce")
        t = t.dropna(subset=["ts"])
        mask_in  = (t["transactionType"].astype(str) == "1")
        mask_out = (t["transactionType"].astype(str) == "2")
        sum_in   = t.loc[mask_in,  "transactionBalance"].fillna(0).sum()
        sum_out  = t.loc[mask_out, "transactionBalance"].fillna(0).sum()
        bal = t["transactionAfterBalance"].fillna(0)
        bal_mean, bal_std, bal_min, bal_max = bal.mean(), bal.std(ddof=0), bal.min(), bal.max()
        if len(t):
            max_ts = t["ts"].max()
            r3 = t[t["ts"] >= (max_ts - pd.DateOffset(months=3))]
            r6 = t[t["ts"] >= (max_ts - pd.DateOffset(months=6))]
            r3_bal_mean = r3["transactionAfterBalance"].mean() if len(r3) else np.nan
            r6_bal_mean = r6["transactionAfterBalance"].mean() if len(r6) else np.nan
        else:
            r3_bal_mean = r6_bal_mean = np.nan
        surplus_ratio  = (sum_in - sum_out) / sum_in if sum_in > 0 else 0.0
        liquidity_pref = (bal_std / (bal_mean + 1e-6)) if bal_mean > 0 else 0.0
        g_in = t.loc[mask_in, ["ts","transactionBalance"]]
        if len(g_in):
            q80 = g_in["transactionBalance"].quantile(0.8)
            big_in = g_in[g_in["transactionBalance"] >= q80]
            month_ok = big_in.groupby(big_in["ts"].dt.to_period("M")).size()
            has_salary_pattern = int((month_ok >= 1).mean() >= 0.6)
        else:
            has_salary_pattern = 0
        return pd.Series(dict(
            sum_in=sum_in, sum_out=sum_out,
            bal_mean=bal_mean, bal_std=bal_std, bal_min=bal_min, bal_max=bal_max,
            r3_bal_mean=r3_bal_mean, r6_bal_mean=r6_bal_mean,
            surplus_ratio=surplus_ratio, liquidity_pref=liquidity_pref,
            has_salary_pattern=has_salary_pattern
        ))
    t = df_tx_all.copy()
    if "user_id" not in t.columns: t["user_id"] = 0
    return t.groupby("user_id", group_keys=False).apply(_one).reset_index()
