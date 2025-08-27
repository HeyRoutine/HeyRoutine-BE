from fastapi import FastAPI, HTTPException, Body
from typing import Any, Dict, Optional
import os, pandas as pd, json, asyncio, contextlib
from aiokafka import AIOKafkaProducer, AIOKafkaConsumer
from dotenv import load_dotenv
import logging
from app.ml.artifacts import load_ranker
from app.ml.predict import predict_for_user

load_dotenv()

logger = logging.getLogger(__name__)

MODEL_DIR   = os.getenv("MODEL_DIR", "./artifacts/ranker_single")
DEPOSIT_CSV = os.getenv("DEPOSIT_CSV", "./data/대학생가능예금상품.csv")
SAVINGS_CSV = os.getenv("SAVINGS_CSV", "./data/대학생가능적금상품.csv")

# ===== Kafka 설정 (서비스마다 바꿔주면 3개도 OK) =====
KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "localhost:9092")
MODEL_NAME      = os.getenv("MODEL_NAME", "ranker3")
REQUEST_TOPIC   = os.getenv("REQUEST_TOPIC", f"{MODEL_NAME}.requests")
RESPONSE_TOPIC  = os.getenv("RESPONSE_TOPIC", f"{MODEL_NAME}.responses")
GROUP_ID        = os.getenv("GROUP_ID", f"{MODEL_NAME}-group")

app = FastAPI(title=f"Ranker API ({MODEL_NAME})", version="1.1.0")

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

# ===== aiokafka 리소스 =====
producer: Optional[AIOKafkaProducer] = None
consumer_task: Optional[asyncio.Task] = None   

async def start_producer():
    global producer
    producer = AIOKafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP,
        value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
        enable_idempotence=True,  # 중복 전송 방지
    )
    await producer.start()

async def stop_producer():
    if producer is not None:
        await producer.stop()

async def consume_loop():
    while True:  # 죽으면 자동 재시작
        consumer = AIOKafkaConsumer(
            REQUEST_TOPIC,
            bootstrap_servers=KAFKA_BOOTSTRAP,
            group_id=GROUP_ID,
            enable_auto_commit=False,
            auto_offset_reset="earliest",
            value_deserializer=None,
            max_poll_interval_ms=900_000,
            session_timeout_ms=30_000,
            heartbeat_interval_ms=10_000,
        )
        try:
            await consumer.start()

            # 선택: 파티션 할당 대기 로깅 (없어도 동작엔 문제 없음)
            for i in range(30):
                parts = consumer.assignment()
                if parts:
                    break
                await asyncio.sleep(1)
                
            async for msg in consumer:
                try:
                    payload = json.loads(msg.value.decode("utf-8"))
                except Exception as e:
                    try:
                        await consumer.commit()
                    except Exception as ce:
                        logger.error("[KAFKA] commit failed after skip: %s", ce)
                    continue
                
                if not isinstance(payload, dict):
                    logger.warning("[KAFKA] skip non-object JSON at %s:%s@%s: %r",
                                msg.topic, msg.partition, msg.offset, payload)
                    try:
                        await consumer.commit()
                    except Exception as ce:
                        logger.error("[KAFKA] commit failed after non-object: %s", ce)
                    continue

                uid   = payload.get("user_id")
                tx    = payload.get("transactions", [])
                top_k = int(payload.get("top_k", 10))
                corr  = payload.get("correlation_id")
                reply_to = payload.get("reply_to", RESPONSE_TOPIC)

                if not isinstance(tx, list) or len(tx) == 0:
                    err = {"error": "transactions empty or invalid", "correlation_id": corr}
                    try:
                        await producer.send_and_wait(reply_to, err)
                    except Exception as pe:
                        logger.error("[KAFKA] produce error(err reply): %s", pe)
                    finally:
                        try:
                            await consumer.commit()
                        except Exception as ce:
                            logger.error("[KAFKA] commit failed: %s", ce)
                    continue

                # 무거운 예측은 이벤트루프 블록 방지
                try:
                    loop = asyncio.get_running_loop()
                    df_tx = pd.DataFrame(tx)
                    if uid is not None and "user_id" in df_tx.columns:
                        df_tx = df_tx[df_tx["user_id"] == uid]

                    recs = await loop.run_in_executor(
                        None,
                        lambda: predict_for_user(
                            model=model, feat_cols=feat_cols,
                            df_tx_user=df_tx, df_prod=df_prod_loaded,
                            top_k=top_k, best_iter=best_iter
                        )
                    )
                    out = {
                        "model": MODEL_NAME,
                        "user_id": uid if uid is not None else (
                            df_tx["user_id"].iloc[0] if "user_id" in df_tx.columns and not df_tx.empty else None
                        ),
                        "top_k": top_k,
                        "results": recs.to_dict(orient="records"),
                        "correlation_id": corr,
                    }
                except Exception as e:
                    out = {"error": f"prediction failed: {e}", "correlation_id": corr}
                try:
                    await producer.send_and_wait(reply_to, out)
                except Exception as pe:
                    logger.error("[KAFKA] produce error: %s (reply_to=%s)", pe, reply_to)
                finally:
                    try:
                        await consumer.commit()
                    except Exception as ce:
                        logger.error("[KAFKA] commit failed: %s", ce)

        except asyncio.CancelledError:
            raise
        except Exception as e:
            # ← 여기서 예외를 반드시 로그로 남기고, while True로 자동 재시작
            logger.exception("💥 consume_loop crashed: %s  (will restart in 2s)", e)
            await asyncio.sleep(2)
        finally:
            try:
                await consumer.stop()
            except Exception:
                logger.exception("consumer.stop() failed")



# ===== FastAPI lifespan =====
@app.on_event("startup")
async def on_startup():
    await start_producer()
    abc = {
        "id" : "123",
        "message" : "hihihihihi"
    }
    await producer.send_and_wait(RESPONSE_TOPIC,abc)
    global consumer_task
    consumer_task = asyncio.create_task(consume_loop())

@app.on_event("shutdown")
async def on_shutdown():
    if consumer_task:
        consumer_task.cancel()
        with contextlib.suppress(asyncio.CancelledError):
            await consumer_task
    await stop_producer()



# ===== HTTP =====
@app.get("/health")
def health():
    return {
        "ok": True,
        "model_dir": MODEL_DIR,
        "deposit_csv": DEPOSIT_CSV,
        "savings_csv": SAVINGS_CSV,
        "n_feat": len(feat_cols),
        "n_products": len(df_prod_loaded),
        "kafka": {
            "bootstrap": KAFKA_BOOTSTRAP,
            "request_topic": REQUEST_TOPIC,
            "response_topic": RESPONSE_TOPIC,
            "group_id": GROUP_ID,
            "model": MODEL_NAME,
        }
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