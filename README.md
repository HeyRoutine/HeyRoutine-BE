# HeyRoutine-BE-ai – AI 서비스 모노레포

생활 루틴 추천, 카드 가맹점 카테고리 분류, 예·적금 금융상품 랭킹 3개 서비스를 **FastAPI**로 제공하는 모노레포입니다.

- `routine_ai/` – 생활 루틴 추천 (MySQL 기반 Hybrid KNN)
- `categori_ai/` – 가맹점명 → 카테고리 분류 (룰 우선 + KoBERT 폴백)
- `financial_product_ai/` – 예·적금 상품 랭킹 (LightGBM Ranker)

## 폴더 구조

```
.
├─ routine_ai/
│  ├─ app/
│  │  ├─ main.py            # FastAPI 엔드포인트 (/health, /recommend)
│  │  ├─ model.py           # 모델 로딩/호출 래퍼
│  │  ├─ recommender_core.py# Hybrid KNN 핵심 로직
│  │  ├─ config.py          # .env 로드, 환경변수 매핑
│  │  └─ db.py              # SQLAlchemy로 MySQL 접근
│  ├─ .env                  # DB 등 환경변수 (샘플 포함)
│  └─ requirements.txt
│
├─ categori_ai/
│  ├─ app/
│  │  ├─ main.py            # /healthz, /predict
│  │  ├─ model.py           # 룰 + KoBERT 추론
│  │  ├─ schemas.py         # 요청/응답 스키마
│  │  └─ kobert_ckpt/fold5/best/  # 토크나이저/모델(로컬 체크포인트)
│  └─ requirements.txt
│
├─ financial_product_ai/
│  ├─ app/
│  │  ├─ main.py            # /health, /predict, /info
│  │  └─ ml/                # 피처/후보생성/추론 유틸
│  ├─ artifacts/ranker_single/   # meta.json, model.pkl, model.txt
│  ├─ data/                     # 대학생가능예금상품.csv, 대학생가능적금상품.csv
│  └─ requirements.txt
└─ README.md (이 파일)
```

---

## 개발 환경

- **Python** 3.10 ~ 3.12 권장
- OS: macOS / Linux / Windows 10+  
- 필수: `pip`, `virtualenv`(권장), MySQL 인스턴스(루틴 추천용)

### 주요 의존성

- 공통: `fastapi`, `uvicorn`
- `routine_ai`: `python-dotenv`, `SQLAlchemy`, `PyMySQL`, `pandas`, `numpy`, `scipy`, `scikit-learn`
- `categori_ai`: `torch`, `transformers`, `sentencepiece`
- `financial_product_ai`: `pandas`, `numpy`, `lightgbm`, `joblib`

---

## 빌드 & 설치

```bash
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
python -m pip install --upgrade pip
```

### 1) routine_ai
```bash
cd routine_ai
pip install -r requirements.txt
```

환경변수 `.env` 파일 수정:
```env
MYSQL_HOST=...
MYSQL_PORT=3306
MYSQL_USER=...
MYSQL_PASSWORD=...
MYSQL_DB=heyroutine
PLAN_TABLE=user_weekly_plan_with_routine
MASTER_TABLE=routine_master_table
SURVEY_TABLE=user_survey_flags
```

### 2) categori_ai
```bash
cd categori_ai
pip install -r requirements.txt
```

### 3) financial_product_ai
```bash
cd financial_product_ai
pip install -r requirements.txt
```

---

## 실행 방법

### routine_ai
```bash
cd routine_ai
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### categori_ai
```bash
cd categori_ai
uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
```

### financial_product_ai
```bash
cd financial_product_ai
uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
```

---

## 환경변수 요약

- routine_ai: `MYSQL_*`, `PLAN_TABLE`, `MASTER_TABLE`, `SURVEY_TABLE`, 추천 파라미터
- categori_ai: `MODEL_DIR` (옵션)
- financial_product_ai: `MODEL_DIR`, `DEPOSIT_CSV`, `SAVINGS_CSV`

---

## 트러블슈팅

- DB 연결 안됨 → `.env` 수정 및 RDS 접근 권한 확인
- KoBERT 로딩 실패 → `sentencepiece` 설치, 경로 점검
- LightGBM 오류 → `meta.json`과 입력 피처 일치 여부 확인
