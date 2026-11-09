from __future__ import annotations

from typing import Any
from pydantic import BaseModel, ConfigDict, Field
from app.feature_schema import EducationStage, FeatureValue, to_camel


class ApiModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True, extra="allow")


class StudentProfile(ApiModel):
    user_id: str | None = None
    age_years: float | None = None
    date_of_birth: str | None = None
    age_band: str | None = None
    student_status: str | None = None
    education_stage: EducationStage | None = None
    country: str | None = None
    course: str | None = None
    college: str | None = None
    institution: str | None = None
    institution_type: str | None = None
    cgpa: float | None = None
    percentage: float | None = None
    previous_score: float | None = None
    family_income: float | None = None
    family_size: int | None = None
    category: str | None = None
    state: str | None = None
    district: str | None = None
    city: str | None = None
    rural_urban: str | None = None
    domicile_state: str | None = None
    year_of_study: int | None = None
    semester: int | None = None
    branch: str | None = None
    specialization: str | None = None
    university: str | None = None
    gender: str | None = None
    disability_status: str | None = None
    employment_status: str | None = None
    work_experience_years: float | None = None
    career_goal: str | None = None
    features: dict[str, FeatureValue] = Field(default_factory=dict)
    behaviors: dict[str, FeatureValue] = Field(default_factory=dict)


class ScholarshipCandidate(ApiModel):
    id: int
    name: str
    description: str | None = None
    provider: str | None = None
    education_stage: EducationStage | None = None
    required_education_stage: EducationStage | None = None
    course: str | None = None
    category: str | None = None
    eligible_categories: str | None = None
    state: str | None = None
    minimum_cgpa: float | None = None
    maximum_family_income: float | None = None
    minimum_year_of_study: int | None = None
    maximum_year_of_study: int | None = None
    amount: float | None = None
    minimum_percentage: float | None = None
    maximum_percentage: float | None = None
    required_class: int | None = None
    required_branch: str | None = None
    required_specialization: str | None = None
    required_category: str | None = None
    required_gender: str | None = None
    required_state: str | None = None
    required_domicile: str | None = None
    application_deadline: str | None = None
    application_url: str | None = None
    source_url: str | None = None
    features: dict[str, FeatureValue] = Field(default_factory=dict)


class RecommendationRequest(ApiModel):
    student: StudentProfile
    scholarships: list[ScholarshipCandidate] = Field(min_length=1)


class SignalScores(ApiModel):
    autoencoder: float
    semantic: float
    clustering: float
    graph: float


class RecommendedScholarship(ApiModel):
    scholarship_id: int
    score: float
    signals: SignalScores
    clusters: dict[str, int | None]
    reasons: list[str]


class PipelineDiagnostics(ApiModel):
    version: str
    feature_schema_version: str
    candidate_count: int
    latent_dimensions: int
    semantic_model: str
    semantic_status: str
    clustering_metrics: dict[str, dict[str, float | None]]
    ranking_weights: dict[str, float]


class RecommendationResponse(ApiModel):
    recommendations: list[RecommendedScholarship]
    pipeline: PipelineDiagnostics


class AdaptiveQuestionRankingRequest(ApiModel):
    student_id: str | None = None
    candidate_scholarship_ids: list[str] = Field(default_factory=list)
    known_field_keys: list[str] = Field(default_factory=list)
    missing_field_keys: list[str] = Field(default_factory=list)
    already_asked_field_keys: list[str] = Field(default_factory=list)
    scholarship_requirement_field_map: dict[str, list[str]] = Field(default_factory=dict)
    scholarship_names: dict[str, str] = Field(default_factory=dict)


class RankedQuestion(ApiModel):
    field_key: str
    priority_score: float
    reason: str
    target_scholarship_count: int = 0


class AdaptiveQuestionRankingResponse(ApiModel):
    ranked_questions: list[RankedQuestion]


class SemanticScholarshipDoc(ApiModel):
    id: int
    name: str | None = None
    description: str | None = None
    provider: str | None = None
    category: str | None = None


class SemanticSearchRequest(ApiModel):
    query: str
    documents: list[SemanticScholarshipDoc] = Field(default_factory=list)
    top_k: int = 20


class SemanticSearchResult(ApiModel):
    scholarship_id: int
    score: float


class SemanticSearchResponse(ApiModel):
    results: list[SemanticSearchResult]
    query: str
    model: str


class FeedbackEventRequest(ApiModel):
    student_id: int
    scholarship_id: int
    event_type: str
    features: dict[str, Any] = Field(default_factory=dict)
    event_timestamp: str | None = None


class FeedbackEventResponse(ApiModel):
    status: str
    event_type: str | None = None
    samples_seen: int = 0
    online_auc: float = 0.5
    model_version: str = "river-online-v1"
    message: str | None = None


class BatchFeedbackRequest(ApiModel):
    events: list[FeedbackEventRequest] = Field(default_factory=list)


class OnlineScoringCandidate(ApiModel):
    scholarship_id: int
    features: dict[str, Any] = Field(default_factory=dict)


class OnlineScoringRequest(ApiModel):
    student_id: int
    candidates: list[OnlineScoringCandidate] = Field(default_factory=list)


class OnlineScoringResult(ApiModel):
    scholarship_id: int
    online_score: float


class OnlineScoringResponse(ApiModel):
    scores: list[OnlineScoringResult]
    model_version: str
