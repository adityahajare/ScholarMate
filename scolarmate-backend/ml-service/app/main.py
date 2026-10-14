import logging
from contextlib import asynccontextmanager
from time import perf_counter

from fastapi import FastAPI

from app.recommendation import build_recommendations, warm_up_semantic_model
from app.schemas import RecommendationRequest, RecommendationResponse


log = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(_: FastAPI):
    try:
        model_name = warm_up_semantic_model()
        log.info("ML semantic model warmed model=%s", model_name)
    except Exception:
        log.exception("ML semantic model warm-up failed; requests will use fallback scoring")
    yield


app = FastAPI(title="ScholarMate ML Recommendation Service", version="1.0.0", lifespan=lifespan)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "scholarmate-ml"}


@app.post("/api/v1/recommend", response_model=RecommendationResponse)
def recommend(request: RecommendationRequest) -> RecommendationResponse:
    started_at = perf_counter()
    response = build_recommendations(request)
    log.info(
        "ML recommendation completed candidates=%s duration_ms=%.2f semantic_status=%s",
        len(request.scholarships),
        (perf_counter() - started_at) * 1000,
        response.pipeline.semantic_status,
    )
    return response
