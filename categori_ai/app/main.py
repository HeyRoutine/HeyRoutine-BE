from fastapi import FastAPI
from .model import predict
from .schemas import TextRequest, LabelResponse

app = FastAPI(title="KoBERT Label API", version="1.0.0")

@app.get("/healthz")
def healthz():
    return {"status": "ok"}

@app.post("/predict", response_model=LabelResponse)
def do_predict(req: TextRequest):
    labels = predict(req.texts)
    return {"labels": labels}
