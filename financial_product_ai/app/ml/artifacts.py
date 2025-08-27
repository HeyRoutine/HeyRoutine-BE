
import os, json, joblib, lightgbm as lgb

def load_ranker(model_dir: str):
    meta_path = os.path.join(model_dir, "meta.json")
    model_path = os.path.join(model_dir, "model.pkl")
    if not os.path.exists(meta_path): raise FileNotFoundError(f"meta.json not found in {model_dir}")
    if not os.path.exists(model_path): raise FileNotFoundError(f"model.pkl not found in {model_dir}")
    with open(meta_path, "r", encoding="utf-8") as f:
        meta = json.load(f)
    model = joblib.load(model_path)
    feat_cols = meta["feat_cols"]
    best_iter = meta.get("best_iteration")
    return model, feat_cols, best_iter
