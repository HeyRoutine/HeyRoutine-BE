import pymysql
import pandas as pd
from contextlib import contextmanager
from .config import settings

DB_CFG = {
    "host": settings.mysql_host,
    "port": settings.mysql_port,
    "user": settings.mysql_user,
    "password": settings.mysql_password,
    "db": settings.mysql_db,
    "charset": "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor,
    "autocommit": False,
}

def get_conn():
    return pymysql.connect(**DB_CFG)

@contextmanager
def db_cursor():
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            yield cur
        conn.commit()
    except:
        conn.rollback()
        raise
    finally:
        conn.close()

def read_df(sql: str, params=None) -> pd.DataFrame:
    conn = get_conn()
    try:
        return pd.read_sql(sql, conn, params=params)
    finally:
        conn.close()
