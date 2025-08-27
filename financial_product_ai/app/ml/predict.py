
import numpy as np, pandas as pd
from .features import build_user_features_multi
from .candidates import rule_filter_candidates_relaxed

def predict_for_user(model, feat_cols, df_tx_user: pd.DataFrame, df_prod: pd.DataFrame,
                     top_k: int = 10, min_per_type: int = 12, relax_factor: float = 2.0,
                     best_iter=None) -> pd.DataFrame:
    uf_df = build_user_features_multi(df_tx_user)
    if uf_df.empty: raise ValueError("No transactions for user.")
    uf = uf_df.iloc[0]

    cands = rule_filter_candidates_relaxed(df_prod, uf, min_per_type=min_per_type, relax_factor=relax_factor)
    if cands.empty:
        for rf in (relax_factor*1.5, relax_factor*2.5):
            cands = rule_filter_candidates_relaxed(df_prod, uf, min_per_type=min_per_type, relax_factor=rf)
            if not cands.empty: break
        if cands.empty: cands = df_prod.copy()

    feats_df = cands.copy()
    for k, v in uf.items():
        feats_df[k] = v
    if "interesRate" not in feats_df.columns and "interestRate" in feats_df.columns:
        feats_df = feats_df.rename(columns={"interestRate": "interesRate"})

    Z = feats_df.reindex(columns=feat_cols, fill_value=0)
    Z = Z.apply(pd.to_numeric, errors="coerce").fillna(0)

    feats_df["score"] = model.predict(Z, num_iteration=best_iter)

    keep = ["bankName", "accountTypeName","accountDscription", "subscriptionPeriod","interesRate"]
    for c in keep:
        if c not in feats_df.columns: feats_df[c] = np.nan

    ranked = feats_df.sort_values("score", ascending=False).reset_index(drop=True)
    ranked["rank"] = np.arange(1, len(ranked)+1)
    return ranked[keep + ["score","rank"]].head(top_k)
