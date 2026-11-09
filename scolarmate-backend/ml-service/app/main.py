import logging
from contextlib import asynccontextmanager
from time import perf_counter
from typing import Any

from fastapi import FastAPI

from app.adaptive_ranking import rank_adaptive_questions
from app.online_learning import online_ranker
from app.recommendation import SEMANTIC_MODEL, build_recommendations, warm_up_semantic_model
from app.semantic_search import compute_semantic_similarities
from app.schemas import (
    AdaptiveQuestionRankingRequest,
    AdaptiveQuestionRankingResponse,
    BatchFeedbackRequest,
    FeedbackEventRequest,
    FeedbackEventResponse,
    OnlineScoringRequest,
    OnlineScoringResponse,
    OnlineScoringResult,
    RecommendationRequest,
    RecommendationResponse,
    SemanticSearchRequest,
    SemanticSearchResponse,
    SemanticSearchResult,
)

log = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(_: FastAPI):
    try:
        model_name = warm_up_semantic_model()
        log.info("ML semantic model warmed model=%s", model_name)
    except Exception:
        log.exception("ML semantic model warm-up failed; requests will use fallback scoring")
    yield


app = FastAPI(
    title="ScholarMate ML Recommendation, Search & Online Learning Service",
    version="2.0.0",
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "scholarmate-ml",
        "online_learning_samples": str(online_ranker.samples_seen),
    }


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


@app.post("/api/v1/adaptive/rank-questions", response_model=AdaptiveQuestionRankingResponse)
def rank_questions(request: AdaptiveQuestionRankingRequest) -> AdaptiveQuestionRankingResponse:
    started_at = perf_counter()
    response = rank_adaptive_questions(request)
    log.info(
        "ML question ranking completed questions=%s duration_ms=%.2f",
        len(response.ranked_questions),
        (perf_counter() - started_at) * 1000,
    )
    return response


@app.post("/api/v1/search/semantic", response_model=SemanticSearchResponse)
def semantic_search(request: SemanticSearchRequest) -> SemanticSearchResponse:
    started_at = perf_counter()
    docs = [doc.model_dump() for doc in request.documents]
    sims = compute_semantic_similarities(request.query, docs)
    results = [SemanticSearchResult(**sim) for sim in sims[: request.top_k]]
    log.info(
        "Semantic search query='%s' docs=%s matches=%s duration_ms=%.2f",
        request.query,
        len(docs),
        len(results),
        (perf_counter() - started_at) * 1000,
    )
    return SemanticSearchResponse(results=results, query=request.query, model=SEMANTIC_MODEL)


@app.post("/api/v1/online-learning/feedback", response_model=FeedbackEventResponse)
def learn_feedback(request: FeedbackEventRequest) -> FeedbackEventResponse:
    started_at = perf_counter()
    result = online_ranker.learn_one(request.event_type, request.features)
    log.info(
        "Online learning feedback processed student=%s scholarship=%s event=%s samples=%s duration_ms=%.2f",
        request.student_id,
        request.scholarship_id,
        request.event_type,
        online_ranker.samples_seen,
        (perf_counter() - started_at) * 1000,
    )
    return FeedbackEventResponse(
        status=result.get("status", "ok"),
        event_type=result.get("event_type"),
        samples_seen=result.get("samples_seen", online_ranker.samples_seen),
        online_auc=result.get("online_auc", 0.5),
        model_version=result.get("model_version", online_ranker.version),
        message=result.get("message"),
    )


@app.post("/api/v1/online-learning/train-batch", response_model=dict[str, Any])
def train_batch(request: BatchFeedbackRequest) -> dict[str, Any]:
    started_at = perf_counter()
    learned_count = 0
    for event in request.events:
        res = online_ranker.learn_one(event.event_type, event.features)
        if res.get("status") == "learned":
            learned_count += 1
    online_ranker.save()
    duration_ms = (perf_counter() - started_at) * 1000
    log.info("Batch feedback training completed batch_size=%s learned=%s duration_ms=%.2f", len(request.events), learned_count, duration_ms)
    return {
        "status": "batch_completed",
        "total_events": len(request.events),
        "learned_count": learned_count,
        "total_samples_seen": online_ranker.samples_seen,
        "online_auc": float(online_ranker.metric.get()) if online_ranker.metric.get() is not None else 0.5,
        "duration_ms": duration_ms,
    }


@app.post("/api/v1/online-learning/score", response_model=OnlineScoringResponse)
def score_candidates(request: OnlineScoringRequest) -> OnlineScoringResponse:
    scores = []
    for candidate in request.candidates:
        score = online_ranker.score_candidate(candidate.features)
        scores.append(OnlineScoringResult(scholarship_id=candidate.scholarship_id, online_score=score))
    return OnlineScoringResponse(scores=scores, model_version=online_ranker.version)
