from __future__ import annotations

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
