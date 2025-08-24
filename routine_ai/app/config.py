from dataclasses import dataclass
import os
from dotenv import load_dotenv

# load .env before anything imports settings
load_dotenv()

def _get(name: str, default=None, cast=None):
    v = os.getenv(name, default)
    if cast and v is not None:
        try:
            return cast(v)
        except Exception:
            return default
    return v

@dataclass
class Settings:
    mysql_host: str = _get("MYSQL_HOST", "127.0.0.1")
    mysql_port: int = _get("MYSQL_PORT", 3306, int)
    mysql_user: str = _get("MYSQL_USER", "root")
    mysql_password: str = _get("MYSQL_PASSWORD", "")
    mysql_db: str = _get("MYSQL_DB", "")

    # Either table names or raw SQL
    plan_table: str | None = _get("PLAN_TABLE")
    master_table: str | None = _get("MASTER_TABLE")
    survey_table: str | None = _get("SURVEY_TABLE")

    plan_sql: str | None = _get("PLAN_SQL")
    master_sql: str | None = _get("MASTER_SQL")
    survey_sql: str | None = _get("SURVEY_SQL")

    # KNN knobs (optional)
    final_topk: int = _get("FINAL_TOPK", 20, int)
    knn_topk_per_col: int = _get("KNN_TOPK_PER_COL", 100, int)
    neighbors_per_history: int = _get("NEIGHBORS_PER_HISTORY", 100, int)
    shrinkage_beta: float = _get("SHRINKAGE_BETA", 100.0, float)
    alpha_importance: float = _get("ALPHA_IMPORTANCE", 0.5, float)
    survey_weight: float = _get("SURVEY_WEIGHT", 0.8, float)
    use_survey_idf: bool = _get("USE_SURVEY_IDF", "true").lower() not in {"0","false","no"}
    drop_common_rate: float = _get("DROP_COMMON_RATE", 0.90, float)
    bm25_k1: float = _get("BM25_K1", 1.2, float)
    bm25_b: float = _get("BM25_B", 0.75, float)
    recency_half_life_days: int = _get("RECENCY_HALF_LIFE_DAYS", 30, int)
    idf_bias: float = _get("IDF_BIAS", 0.2, float)

settings = Settings()