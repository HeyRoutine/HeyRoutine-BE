import pandas as pd
from sqlalchemy import create_engine, text
from contextlib import contextmanager
from .config import settings

# DB 설정
DB_URL = (
    f"mysql+pymysql://{settings.mysql_user}:{settings.mysql_password}"
    f"@{settings.mysql_host}:{settings.mysql_port}/{settings.mysql_db}"
    "?charset=utf8mb4"
)

# SQLAlchemy 엔진 생성 (pool_recycle 방지 옵션 포함)
engine = create_engine(DB_URL, pool_pre_ping=True, pool_recycle=3600)

# 커넥션 컨텍스트 매니저
@contextmanager
def db_cursor():
    conn = engine.connect()
    trans = conn.begin()
    try:
        yield conn
        trans.commit()
    except:
        trans.rollback()
        raise
    finally:
        conn.close()

# pandas DataFrame 읽기
def read_df(sql: str, params=None) -> pd.DataFrame:
    with engine.connect() as conn:
        return pd.read_sql(text(sql), conn, params=params)