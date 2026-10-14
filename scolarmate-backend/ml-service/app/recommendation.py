from __future__ import annotations

import os
import json
import re
from functools import lru_cache
from typing import Any

import networkx as nx
import numpy as np
import torch
from sentence_transformers import SentenceTransformer
from sklearn.cluster import HDBSCAN, KMeans
from sklearn.feature_extraction import DictVectorizer
from sklearn.metrics import davies_bouldin_score, silhouette_score
from sklearn.mixture import GaussianMixture
from sklearn.preprocessing import StandardScaler
from torch import nn

from app.schemas import (
    PipelineDiagnostics,
    RecommendationRequest,
    RecommendationResponse,
    RecommendedScholarship,
    SignalScores,
)
from app.feature_schema import (
    FEATURE_SCHEMA_VERSION,
    FeatureStatus,
    encode_scholarship_features,
    encode_student_features,
    scholarship_features,
    student_features,
)


PIPELINE_VERSION = "unsupervised-hybrid-v1"
LATENT_DIMENSIONS = 32
RANKING_WEIGHTS = {"autoencoder": 0.25, "semantic": 0.35, "clustering": 0.20, "graph": 0.20}
SEMANTIC_MODEL = os.getenv("SCHOLARMATE_EMBEDDING_MODEL", "sentence-transformers/all-MiniLM-L6-v2")
TOKEN_PATTERN = re.compile(r"[^\w]+", re.UNICODE)


class Autoencoder(nn.Module):
    def __init__(self, feature_count: int) -> None:
        super().__init__()
        self.encoder = nn.Sequential(
            nn.Linear(feature_count, 128),
            nn.ReLU(),
            nn.Linear(128, 64),
            nn.ReLU(),
            nn.Linear(64, LATENT_DIMENSIONS),
        )
        self.decoder = nn.Sequential(
            nn.Linear(LATENT_DIMENSIONS, 64),
            nn.ReLU(),
            nn.Linear(64, 128),
            nn.ReLU(),
            nn.Linear(128, feature_count),
        )

    def forward(self, values: torch.Tensor) -> torch.Tensor:
        return self.decoder(self.encoder(values))


@lru_cache(maxsize=1)
def _sentence_model() -> SentenceTransformer:
    return SentenceTransformer(SEMANTIC_MODEL, device="cpu")


def warm_up_semantic_model() -> str:
    """Load the embedding model during startup instead of the first request."""
    _sentence_model()
    return SEMANTIC_MODEL


def _tokens(value: str | None) -> list[str]:
    if not value:
        return []
    return [part.strip() for part in re.split(r"[,;/|]", value) if part.strip()]


def _student_text(student: Any) -> str:
    values = [
        student.course,
        student.college,
        student.category,
        student.state,
        student.domicile_state,
        student.gender,
        f"year of study {student.year_of_study}" if student.year_of_study is not None else None,
        f"CGPA {student.cgpa}" if student.cgpa is not None else None,
        f"family income {student.family_income}" if student.family_income is not None else None,
    ]
    return " ".join(value for value in values if value)


def _scholarship_text(scholarship: Any) -> str:
    values = [
        scholarship.name,
        scholarship.description,
        scholarship.provider,
        scholarship.course,
        scholarship.category,
        scholarship.eligible_categories,
        scholarship.state,
        f"minimum CGPA {scholarship.minimum_cgpa}" if scholarship.minimum_cgpa is not None else None,
        f"maximum family income {scholarship.maximum_family_income}"
        if scholarship.maximum_family_income is not None
        else None,
    ]
    return " ".join(value for value in values if value)


def semantic_scores(student: Any, scholarships: list[Any]) -> tuple[list[float], str]:
    try:
        model = _sentence_model()
        texts = [_student_text(student)] + [_scholarship_text(item) for item in scholarships]
        vectors = model.encode(texts, normalize_embeddings=True, show_progress_bar=False)
        return [float(np.clip(vectors[0] @ vector, 0.0, 1.0)) for vector in vectors[1:]], "available"
    except Exception:
        return [0.0] * len(scholarships), "unavailable"


@lru_cache(maxsize=8)
def _fit_catalog(records_json: str) -> tuple[DictVectorizer, StandardScaler, Autoencoder, np.ndarray, int]:
    records = json.loads(records_json)
    vectorizer = DictVectorizer(sparse=False)
    values = vectorizer.fit_transform(records).astype(np.float32)
    scaler = StandardScaler()
    values = scaler.fit_transform(values).astype(np.float32)
    torch.manual_seed(42)
    torch.set_num_threads(max(1, min(4, torch.get_num_threads())))
    tensor = torch.from_numpy(values)
    model = Autoencoder(values.shape[1])
    optimizer = torch.optim.Adam(model.parameters(), lr=0.005)
    loss_function = nn.MSELoss()
    model.train()
    for _ in range(30):
        optimizer.zero_grad()
        loss = loss_function(model(tensor), tensor)
        loss.backward()
        optimizer.step()
    model.eval()
    with torch.no_grad():
        latent = model.encoder(tensor).cpu().numpy()
    return vectorizer, scaler, model, latent, values.shape[1]


def _encode(
    student_record: dict[str, float],
    scholarship_records: list[dict[str, float]],
) -> tuple[np.ndarray, Autoencoder, int]:
    records_json = json.dumps(scholarship_records, sort_keys=True, separators=(",", ":"))
    vectorizer, scaler, model, scholarship_latent, feature_count = _fit_catalog(records_json)
    student_values = vectorizer.transform([student_record]).astype(np.float32)
    student_values = scaler.transform(student_values).astype(np.float32)
    with torch.no_grad():
        student_latent = model.encoder(torch.from_numpy(student_values)).cpu().numpy()
    return np.vstack((student_latent, scholarship_latent)), model, feature_count


def _cosine_scores(student: np.ndarray, candidates: np.ndarray) -> list[float]:
    student_norm = max(float(np.linalg.norm(student)), 1e-12)
    candidate_norms = np.maximum(np.linalg.norm(candidates, axis=1), 1e-12)
    return np.clip((candidates @ student) / (candidate_norms * student_norm), 0.0, 1.0).tolist()


def _metric_scores(values: np.ndarray, labels: np.ndarray) -> dict[str, float | None]:
    distinct = set(int(label) for label in labels)
    if -1 in distinct:
        distinct.remove(-1)
    if len(distinct) < 2 or len(distinct) >= len(labels):
        return {"silhouette": None, "daviesBouldin": None}
    try:
        return {
            "silhouette": float(silhouette_score(values, labels)),
            "daviesBouldin": float(davies_bouldin_score(values, labels)),
        }
    except ValueError:
        return {"silhouette": None, "daviesBouldin": None}


def _cluster(latent: np.ndarray) -> tuple[dict[str, np.ndarray], dict[str, dict[str, float | None]]]:
    row_count = len(latent)
    if row_count < 3:
        labels = np.zeros(row_count, dtype=int)
        return {"kmeans": labels, "gmm": labels, "hdbscan": np.full(row_count, -1)}, {
            "kmeans": _metric_scores(latent, labels),
            "gmm": _metric_scores(latent, labels),
            "hdbscan": _metric_scores(latent, np.full(row_count, -1)),
        }

    cluster_count = min(8, max(2, round(np.sqrt(row_count))), row_count - 1)
    labels_by_model: dict[str, np.ndarray] = {
        "kmeans": KMeans(n_clusters=cluster_count, n_init=10, random_state=42).fit_predict(latent),
        "gmm": GaussianMixture(n_components=cluster_count, random_state=42).fit_predict(latent),
    }
    labels_by_model["hdbscan"] = HDBSCAN(
        min_cluster_size=min(15, max(2, row_count // 10)),
        min_samples=None,
    ).fit_predict(latent)
    metrics = {name: _metric_scores(latent, labels) for name, labels in labels_by_model.items()}
    return labels_by_model, metrics


def _cluster_scores(labels_by_model: dict[str, np.ndarray]) -> list[float]:
    score_by_candidate: list[list[float]] = [[] for _ in range(len(next(iter(labels_by_model.values()))) - 1)]
    student_labels = {name: labels[0] for name, labels in labels_by_model.items()}
    for index in range(1, len(next(iter(labels_by_model.values())))):
        for name, labels in labels_by_model.items():
            student_cluster = int(student_labels[name])
            candidate_cluster = int(labels[index])
            matches = student_cluster == candidate_cluster and candidate_cluster != -1
            score_by_candidate[index - 1].append(1.0 if matches else 0.0)
    return [float(np.mean(scores)) for scores in score_by_candidate]


def _graph_scores(student: Any, scholarships: list[Any]) -> list[float]:
    graph = nx.Graph()
    student_node = "student"
    graph.add_node(student_node)
    student_attributes = _graph_attributes(student, scholarship=False)
    for attribute in student_attributes:
        graph.add_edge(student_node, attribute)

    scholarship_nodes: list[str] = []
    for item in scholarships:
        node = f"scholarship:{item.id}"
        scholarship_nodes.append(node)
        graph.add_node(node)
        for attribute in _graph_attributes(item, scholarship=True):
            graph.add_edge(node, attribute)

    if graph.degree(student_node) == 0 or not scholarship_nodes:
        return [0.0] * len(scholarships)
    scores = nx.pagerank(graph, alpha=0.85, personalization={student_node: 1.0})
    values = np.array([scores.get(node, 0.0) for node in scholarship_nodes], dtype=float)
    maximum = float(values.max()) if len(values) else 0.0
    return (values / maximum).tolist() if maximum > 0 else [0.0] * len(scholarships)


def _graph_attributes(record: Any, scholarship: bool) -> set[str]:
    if scholarship:
        values = {
            "course": _tokens(record.course),
            "category": _tokens(record.category) + _tokens(record.eligible_categories),
            "state": _tokens(record.state),
        }
    else:
        values = {
            "course": _tokens(record.course),
            "category": _tokens(record.category),
            "state": _tokens(record.state) + _tokens(record.domicile_state),
        }
    return {
        f"{kind}:{' '.join(TOKEN_PATTERN.sub(' ', value.lower()).split())}"
        for kind, entries in values.items()
        for value in entries
        if value.strip()
    }


def _known_feature(features: dict[str, Any], name: str) -> Any | None:
    feature = features.get(name)
    return feature.value if feature and feature.status == FeatureStatus.KNOWN else None


def _hard_eligible(student: Any, scholarship: Any) -> bool:
    """Apply only explicit, comparable requirements before unsupervised ranking."""
    user = student_features(student)
    required = scholarship_features(scholarship)
    explicit_requirements = set((getattr(scholarship, "features", None) or {}).keys())

    comparisons = (
        ("required_education_stage", "education_stage"),
        ("required_category", "category"),
        ("required_gender", "gender"),
        ("required_state", "state"),
        ("required_domicile", "domicile_state"),
        ("required_course", "course"),
        ("required_branch", "branch"),
    )
    for requirement_name, user_name in comparisons:
        if requirement_name not in explicit_requirements:
            continue
        requirement = _known_feature(required, requirement_name)
        actual = _known_feature(user, user_name)
        if requirement is not None and actual is not None:
            required_values = {str(item).strip().lower() for item in (requirement if isinstance(requirement, list) else str(requirement).split(","))}
            actual_values = {str(item).strip().lower() for item in (actual if isinstance(actual, list) else str(actual).split(","))}
            if required_values and actual_values.isdisjoint(required_values):
                return False

    numeric_checks = (
        ("minimum_cgpa", "current_score", lambda actual, required: actual >= required),
        ("maximum_family_income", "annual_family_income", lambda actual, required: actual <= required),
        ("required_year", "year_of_study", lambda actual, required: actual >= required),
        ("maximum_year_of_study", "year_of_study", lambda actual, required: actual <= required),
    )
    for requirement_name, user_name, predicate in numeric_checks:
        if requirement_name not in explicit_requirements:
            continue
        requirement = _known_feature(required, requirement_name)
        actual = _known_feature(user, user_name)
        if requirement is not None and actual is not None:
            actual_value = float(actual)
            if requirement_name == "minimum_cgpa" and actual_value <= 1 and float(requirement) > 1:
                actual_value *= 10
            if not predicate(actual_value, float(requirement)):
                return False
    return True


def build_recommendations(request: RecommendationRequest) -> RecommendationResponse:
    student = request.student
    scholarships = [item for item in request.scholarships if _hard_eligible(student, item)]
    if not scholarships:
        return RecommendationResponse(
            recommendations=[],
            pipeline=PipelineDiagnostics(
                version=PIPELINE_VERSION,
                feature_schema_version=FEATURE_SCHEMA_VERSION,
                candidate_count=0,
                latent_dimensions=LATENT_DIMENSIONS,
                semantic_model=SEMANTIC_MODEL,
                semantic_status="not_evaluated",
                clustering_metrics={},
                ranking_weights=RANKING_WEIGHTS,
            ),
        )
    student_features = encode_student_features(student)
    scholarship_features = [encode_scholarship_features(item) for item in scholarships]
    latent, _, _ = _encode(student_features, scholarship_features)
    autoencoder = _cosine_scores(latent[0], latent[1:])
    labels_by_model, metrics = _cluster(latent)
    cluster_scores = _cluster_scores(labels_by_model)
    semantic, semantic_status = semantic_scores(student, scholarships)
    graph = _graph_scores(student, scholarships)

    active_weights = dict(RANKING_WEIGHTS)
    if semantic_status != "available":
        active_weights.pop("semantic")
        weight_total = sum(active_weights.values())
        active_weights = {name: weight / weight_total for name, weight in active_weights.items()}

    ranked: list[RecommendedScholarship] = []
    for index, scholarship in enumerate(scholarships):
        signal_values = {
            "autoencoder": float(autoencoder[index]),
            "semantic": float(semantic[index]),
            "clustering": float(cluster_scores[index]),
            "graph": float(graph[index]),
        }
        score = sum(signal_values[name] * weight for name, weight in active_weights.items())
        clusters = {name: int(labels[index + 1]) for name, labels in labels_by_model.items()}
        top_signal = max(active_weights, key=lambda name: signal_values[name])
        ranked.append(
            RecommendedScholarship(
                scholarship_id=scholarship.id,
                score=round(float(score), 6),
                signals=SignalScores(**signal_values),
                clusters=clusters,
                reasons=[f"Highest matching signal: {top_signal}"],
            )
        )

    ranked.sort(key=lambda item: item.score, reverse=True)
    return RecommendationResponse(
        recommendations=ranked,
        pipeline=PipelineDiagnostics(
            version=PIPELINE_VERSION,
            feature_schema_version=FEATURE_SCHEMA_VERSION,
            candidate_count=len(scholarships),
            latent_dimensions=LATENT_DIMENSIONS,
            semantic_model=SEMANTIC_MODEL,
            semantic_status=semantic_status,
            clustering_metrics=metrics,
            ranking_weights=active_weights,
        ),
    )
