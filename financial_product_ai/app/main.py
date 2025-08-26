
from fastapi import FastAPI, HTTPException, Body
from typing import Any, Dict
import os, pandas as pd

from app.ml.artifacts import load_ranker
from app.ml.predict import predict_for_user

MODEL_DIR   = os.getenv("MODEL_DIR", "./artifacts/ranker_single")
DEPOSIT_CSV = os.getenv("DEPOSIT_CSV", "./data/대학생가능예금상품.csv")
SAVINGS_CSV = os.getenv("SAVINGS_CSV", "./data/대학생가능적금상품.csv")

app = FastAPI(title="Ranker API", version="1.1.0")

model, feat_cols, best_iter = load_ranker(MODEL_DIR)

# ✅ 상품 CSV 2개를 읽어와서 합치기 (서버 시작 시 1회)
if not os.path.exists(DEPOSIT_CSV):
    raise FileNotFoundError(f"Deposit CSV not found: {DEPOSIT_CSV}")
if not os.path.exists(SAVINGS_CSV):
    raise FileNotFoundError(f"Savings CSV not found: {SAVINGS_CSV}")

data_deposit = pd.read_csv(DEPOSIT_CSV, encoding="utf-8-sig", index_col=0)
data_savings = pd.read_csv(SAVINGS_CSV, encoding="utf-8-sig", index_col=0)

# 타입 컬럼이 없다면 파일명 기준으로 보강 (있으면 생략)
if "type" not in data_deposit.columns:
    data_deposit["type"] = "deposit"
if "type" not in data_savings.columns:
    data_savings["type"] = "savings"

df_prod_loaded = pd.concat([data_deposit, data_savings], ignore_index=True)

# 금리 컬럼명 통일 (학습 때 'interesRate' 사용했으면 맞춤)
if "interesRate" not in df_prod_loaded.columns and "interestRate" in df_prod_loaded.columns:
    df_prod_loaded = df_prod_loaded.rename(columns={"interestRate": "interesRate"})

@app.get("/health")
def health():
    return {
        "ok": True,
        "model_dir": MODEL_DIR,
        "deposit_csv": DEPOSIT_CSV,
        "savings_csv": SAVINGS_CSV,
        "n_feat": len(feat_cols),
        "n_products": len(df_prod_loaded),
    }

@app.post("/predict")
def predict(payload: Dict[str, Any] = Body(...)):
    # 요청은 user_id(선택), transactions(필수), top_k(선택)만 받음
    if "transactions" not in payload:
        raise HTTPException(400, "transactions 필드는 필수입니다.")

    df_tx = pd.DataFrame(payload["transactions"])
    top_k = int(payload.get("top_k", 10))
    uid = payload.get("user_id")

    if uid is not None and "user_id" in df_tx.columns:
        df_tx = df_tx[df_tx["user_id"] == uid]
    if df_tx.empty:
        raise HTTPException(400, "해당 유저의 거래 데이터가 비었습니다.")

    try:
        recs = predict_for_user(
            model=model, feat_cols=feat_cols,
            df_tx_user=df_tx, df_prod=df_prod_loaded,
            top_k=top_k, best_iter=best_iter
        )
    except Exception as e:
        raise HTTPException(500, f"prediction failed: {e}")

    return {
        "user_id": uid if uid is not None else (df_tx["user_id"].iloc[0] if "user_id" in df_tx.columns else None),
        "top_k": top_k,
        "results": recs.to_dict(orient="records"),
    }