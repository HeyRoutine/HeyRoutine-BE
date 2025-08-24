import os
import torch
from transformers import XLNetTokenizer, AutoModelForSequenceClassification
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = os.getenv("MODEL_DIR", str(BASE_DIR / "kobert_ckpt" / "fold5" / "best"))
MODEL_DIR = os.path.abspath(os.path.expanduser(MODEL_DIR))

tokenizer = XLNetTokenizer.from_pretrained(MODEL_DIR, local_files_only=True, use_fast=False)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR, local_files_only=True)
model.eval()

# PAD 토큰 처리
tokenizer.pad_token = "[PAD]"
model.config.pad_token_id = tokenizer.convert_tokens_to_ids("[PAD]")

# 임베딩 크기 확인 후 리사이즈
emb = model.get_input_embeddings()
if emb.num_embeddings != len(tokenizer):
    model.resize_token_embeddings(len(tokenizer))


# ===== 2) 추론 함수 =====
def predict(texts, max_len=256, device=None):
    if isinstance(texts, str):
        texts = [texts]

    enc = tokenizer(
        texts,
        padding=True,
        truncation=True,
        max_length=max_len,
        return_tensors="pt",
    )
    # BERT 계열은 token_type_ids(세그먼트) 0/1 사용. 여기서는 0으로 고정.
    enc["token_type_ids"] = torch.zeros_like(enc["input_ids"])

    if device:
        model.to(device)
        enc = {k: v.to(device) for k, v in enc.items()}

    with torch.no_grad():
        logits = model(**enc).logits
        pred_ids = logits.argmax(-1).tolist()

    id2label = model.config.id2label
    def to_label(i):
        # int/str 키 모두 대응
        return id2label.get(i, id2label.get(str(i), str(i)))
    labels = [to_label(i) for i in pred_ids]
    return labels
