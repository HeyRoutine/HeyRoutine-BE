from typing import Optional
from .recommender_core import RecsConfig, HybridKNNRecommender
from .config import settings

MODEL_VERSION = "hybrid-knn-db-1.0"
_MODEL: Optional[HybridKNNRecommender] = None

def load_model() -> HybridKNNRecommender:
    global _MODEL
    if _MODEL is None:
        cfg = RecsConfig(
            alpha_importance=settings.alpha_importance,
            survey_weight=settings.survey_weight,
            use_survey_idf=settings.use_survey_idf,
            knn_topk_per_col=settings.knn_topk_per_col,
            shrinkage_beta=settings.shrinkage_beta,
            neighbors_per_history=settings.neighbors_per_history,
            final_topk=settings.final_topk,
        )
        _MODEL = HybridKNNRecommender.from_mysql(
            schema=settings.mysql_db,
            plan_table=settings.plan_table,
            master_table=settings.master_table,
            survey_table=settings.survey_table,
            plan_sql=settings.plan_sql,
            master_sql=settings.master_sql,
            survey_sql=settings.survey_sql,
            config=cfg,
        )
    return _MODEL

def recommend(model: HybridKNNRecommender, user_id: str, top_k: int,
              exclude_already_planned: Optional[bool], allow_owned: Optional[bool]):
    return model.recommend(
        user_id=user_id,
        topk=top_k,
        exclude_already_planned=exclude_already_planned,
        allow_owned=bool(allow_owned),
    )
