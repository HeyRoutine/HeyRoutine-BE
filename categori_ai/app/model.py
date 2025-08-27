import os
import re
import unicodedata
import torch
from typing import Optional, List
from transformers import XLNetTokenizer, AutoModelForSequenceClassification
from pathlib import Path

# ===== 0) 경로/모델 로드 =====
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

# ===== 1) 문자열 정규화 & 1차 교통 룰 =====
def normalize(s: str) -> str:
    return unicodedata.normalize("NFKC", s).strip()

_TRANSPORT_POS_PATTERNS = [
    r"티머니|캐시비|대중교통|교통카드",
    r"서울교통공사|부산교통공사|대구도시철도|인천교통공사|도시철도공사|메트로|지하철|전철",
    r"시외버스|고속버스|터미널",
    r"택시|카카오\s*t|kakao\s*t|ut\b|우버|t\s*map\s*택시|tmap\s*택시|kakaot|대리운전",
    r"코레일|korail|srt|sr주식회사|수서고속철도|한국철도공사|ktx",
    r"하이패스|통행료|톨게이트|한국도로공사|고속도로",
    r"공영주차|주차장|파킹|parking|park(?:ing)?\b",
    r"쏘카|그린카|카셰어링|딜카|피플카",
]

_TRANSPORT_NEG_PATTERNS = [
    r"티스테이션",
    r"코레일유통",
    r"티머니몰|티머니페이",
    r"파킹박|(?:주)?파킹클라우드.*정기권",
]

_COMPILED_POS = [re.compile(p, flags=re.I) for p in _TRANSPORT_POS_PATTERNS]
_COMPILED_NEG = [re.compile(p, flags=re.I) for p in _TRANSPORT_NEG_PATTERNS]

def rule_transport_category(merchant_name: str) -> Optional[str]:
    name = normalize(merchant_name)
    for neg in _COMPILED_NEG:
        if neg.search(name):
            return None
    for pos in _COMPILED_POS:
        if pos.search(name):
            return "교통"
    return None

# ===== 2) 추론 함수 (룰 우선 → KoBERT 폴백) =====
def predict(texts, max_len: int = 256, device: Optional[str] = None, use_transport_rule: bool = True) -> List[str]:
    if isinstance(texts, str):
        texts = [texts]

    # 2-1) 룰 우선 적용
    preds: List[Optional[str]] = [None] * len(texts)
    model_indices, model_inputs = [], []
    if use_transport_rule:
        for i, t in enumerate(texts):
            cat = rule_transport_category(t)
            if cat:
                preds[i] = cat
            else:
                model_indices.append(i)
                model_inputs.append(t)
    else:
        model_indices = list(range(len(texts)))
        model_inputs = texts

    if model_inputs:
        enc = tokenizer(
            model_inputs,
            padding=True,
            truncation=True,
            max_length=max_len,
            return_tensors="pt",
        )
        enc["token_type_ids"] = torch.zeros_like(enc["input_ids"])

        if device:
            model.to(device)
            enc = {k: v.to(device) for k, v in enc.items()}

        with torch.no_grad():
            logits = model(**enc).logits
            pred_ids = logits.argmax(-1).tolist()

        id2label = model.config.id2label
        def to_label(i):
            return id2label.get(i, id2label.get(str(i), str(i)))

        model_labels = [to_label(i) for i in pred_ids]
        for idx, lab in zip(model_indices, model_labels):
            preds[idx] = lab

    return [p if p is not None else "" for p in preds]
