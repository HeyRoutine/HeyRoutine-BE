from fastapi import FastAPI
from pydantic import BaseModel
from .model import load_model, recommend, MODEL_VERSION

app = FastAPI(title="Recs DB App", version=MODEL_VERSION)
MODEL = load_model()

class RecReq(BaseModel):
    user_id: str
    top_k: int = 20
    exclude_already_planned: bool | None = None
    allow_owned: bool | None = False

@app.get("/health")
def health():
    return {"ok": True, "version": MODEL_VERSION}

@app.post("/recommend")
def do_recommend(req: RecReq):
    df = recommend(MODEL, req.user_id, req.top_k, req.exclude_already_planned, req.allow_owned)
    return {"items": df.to_dict(orient="records")}
