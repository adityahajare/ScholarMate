from __future__ import annotations

import re
from dataclasses import dataclass
from enum import StrEnum
from typing import Any

from pydantic import BaseModel, ConfigDict, model_validator


FEATURE_SCHEMA_VERSION = "scholarmate-features-v1"


def to_camel(value: str) -> str:
    first, *rest = value.split("_")
    return first + "".join(part.capitalize() for part in rest)


class FeatureStatus(StrEnum):
    KNOWN = "KNOWN"
    UNKNOWN = "UNKNOWN"
    NOT_APPLICABLE = "NOT_APPLICABLE"


class EducationStage(StrEnum):
    SCHOOL = "SCHOOL"
    DIPLOMA = "DIPLOMA"
    UG = "UG"
    PG = "PG"
    PHD_RESEARCH = "PHD_RESEARCH"
    PROFESSIONAL = "PROFESSIONAL"
    VOCATIONAL = "VOCATIONAL"
    SKILL_DEVELOPMENT = "SKILL_DEVELOPMENT"
    OTHER = "OTHER"
    CLASS_11_12 = "CLASS_11_12"
    UNDERGRADUATE = "UNDERGRADUATE"
    POSTGRADUATE = "POSTGRADUATE"
    ITI_VOCATIONAL = "ITI_VOCATIONAL"
    PROFESSIONAL_OTHER = "PROFESSIONAL_OTHER"


EDUCATION_STAGES = frozenset(stage.value for stage in EducationStage)


class FeatureValue(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)

    status: FeatureStatus = FeatureStatus.UNKNOWN
    value: str | float | int | bool | list[str] | None = None

    @model_validator(mode="after")
    def validate_status_value(self) -> FeatureValue:
        if self.status == FeatureStatus.KNOWN and self.value is None:
            raise ValueError("KNOWN features require a value")
        if self.status != FeatureStatus.KNOWN and self.value is not None:
            raise ValueError("UNKNOWN and NOT_APPLICABLE features cannot carry a value")
        if isinstance(self.value, str) and not self.value.strip():
            raise ValueError("KNOWN string features cannot be blank")
        if isinstance(self.value, list) and not self.value:
            raise ValueError("KNOWN list features cannot be empty")
        return self


@dataclass(frozen=True)
class FeatureSpec:
    name: str
    kind: str
    model_key: str
    applies_to: frozenset[str] = frozenset()
    scale: float | None = None
    aliases: tuple[str, ...] = ()


STUDENT_FEATURES = (
    FeatureSpec("education_stage", "category", "education_stage"),
    FeatureSpec("age_years", "numeric", "age_years", scale=40),
    FeatureSpec("student_status", "category", "student_status"),
    FeatureSpec("country", "category", "country"),
    FeatureSpec("state", "category", "state"),
    FeatureSpec("district", "category", "district"),
    FeatureSpec("rural_urban", "category", "rural_urban"),
    FeatureSpec("domicile_state", "category", "domicile_state"),
    FeatureSpec("course", "category", "course"),
    FeatureSpec("institution", "category", "institution", aliases=("college",)),
    FeatureSpec("institution_type", "category", "institution_type"),
    FeatureSpec("category", "category", "category"),
    FeatureSpec("gender", "category", "gender"),
    FeatureSpec("class_number", "numeric", "class_number", frozenset({"SCHOOL"}), 12),
    FeatureSpec("school_board", "category", "school_board", frozenset({"SCHOOL"})),
    FeatureSpec("school_type", "category", "school_type", frozenset({"SCHOOL"})),
    FeatureSpec("school_medium", "category", "school_medium", frozenset({"SCHOOL"})),
    FeatureSpec("stream", "category", "stream", frozenset({"SCHOOL"})),
    FeatureSpec("branch", "category", "branch", frozenset({"DIPLOMA", "UG", "PG"})),
    FeatureSpec("year_of_study", "numeric", "year_of_study", frozenset({"DIPLOMA", "UG", "PG", "PHD_RESEARCH"}), 12),
    FeatureSpec("semester", "numeric", "semester", frozenset({"DIPLOMA", "UG", "PG", "PHD_RESEARCH"}), 12),
    FeatureSpec("current_score", "numeric", "academic_score", scale=10, aliases=("cgpa",)),
    FeatureSpec("previous_score", "numeric", "previous_score", scale=100),
    FeatureSpec("score_trend", "category", "score_trend"),
    FeatureSpec("entrance_exam_score", "numeric", "entrance_exam_score", scale=100),
    FeatureSpec("research_area", "category", "research_area", frozenset({"PHD_RESEARCH"})),
    FeatureSpec("research_stage", "category", "research_stage", frozenset({"PHD_RESEARCH"})),
    FeatureSpec("work_experience_years", "numeric", "work_experience_years", scale=40),
    FeatureSpec("employment_status", "category", "employment_status"),
    FeatureSpec("career_goal", "category", "career_goal"),
    FeatureSpec("annual_family_income", "numeric", "family_income", scale=10_000_000, aliases=("family_income",)),
    FeatureSpec("family_size", "numeric", "family_size", scale=20),
    FeatureSpec("dependents", "numeric", "dependents", scale=20),
    FeatureSpec("financial_need_level", "category", "financial_need_level"),
)

SCHOLARSHIP_FEATURES = (
    FeatureSpec("required_education_stage", "category", "education_stage"),
    FeatureSpec("required_class", "numeric", "class_number", scale=12),
    FeatureSpec("required_course", "category", "course", aliases=("course",)),
    FeatureSpec("required_degree", "category", "degree"),
    FeatureSpec("required_branch", "category", "branch"),
    FeatureSpec("required_specialization", "category", "specialization"),
    FeatureSpec("minimum_cgpa", "numeric", "academic_score", scale=10),
    FeatureSpec("maximum_cgpa", "numeric", "academic_score_max", scale=10),
    FeatureSpec("minimum_percentage", "numeric", "academic_score", scale=100),
    FeatureSpec("maximum_percentage", "numeric", "academic_score_max", scale=100),
    FeatureSpec("previous_qualification", "category", "previous_qualification"),
    FeatureSpec("required_year", "numeric", "year_of_study", scale=12, aliases=("minimum_year_of_study",)),
    FeatureSpec("maximum_year_of_study", "numeric", "year_of_study_max", scale=12),
    FeatureSpec("required_semester", "numeric", "semester", scale=12),
    FeatureSpec("entrance_exam_requirement", "category", "entrance_exam_score"),
    FeatureSpec("academic_performance_requirement", "category", "score_trend"),
    FeatureSpec("minimum_family_income", "numeric", "minimum_family_income", scale=10_000_000),
    FeatureSpec("maximum_family_income", "numeric", "family_income", scale=10_000_000),
    FeatureSpec("financial_need_requirement", "category", "financial_need_level"),
    FeatureSpec("required_category", "category", "category", aliases=("category",)),
    FeatureSpec("required_gender", "category", "gender"),
    FeatureSpec("required_disability", "category", "disability_status"),
    FeatureSpec("minority_requirement", "category", "minority_status"),
    FeatureSpec("required_state", "category", "state", aliases=("state",)),
    FeatureSpec("required_district", "category", "district"),
    FeatureSpec("required_domicile", "category", "domicile_state"),
    FeatureSpec("amount", "numeric", "scholarship_amount", scale=10_000_000),
    FeatureSpec("tuition_coverage", "numeric", "tuition_coverage", scale=10_000_000),
    FeatureSpec("hostel_coverage", "numeric", "hostel_coverage", scale=10_000_000),
    FeatureSpec("recurring", "category", "recurring"),
    FeatureSpec("renewal_available", "category", "renewal_available"),
    FeatureSpec("application_mode", "category", "application_mode"),
    FeatureSpec("mandatory_documents_count", "numeric", "mandatory_documents_count", scale=20),
    FeatureSpec("application_complexity", "category", "application_complexity"),
    FeatureSpec("source_type", "category", "source_type"),
    FeatureSpec("source_reliability", "numeric", "source_reliability", scale=1),
    FeatureSpec("evidence_confidence", "numeric", "evidence_confidence", scale=1),
)

BEHAVIOR_FEATURES = (
    FeatureSpec("views_last_7_days", "numeric", "views_last_7_days", scale=100),
    FeatureSpec("views_last_30_days", "numeric", "views_last_30_days", scale=500),
    FeatureSpec("saves_last_30_days", "numeric", "saves_last_30_days", scale=100),
    FeatureSpec("applications_last_30_days", "numeric", "applications_last_30_days", scale=50),
    FeatureSpec("searches_last_30_days", "numeric", "searches_last_30_days", scale=500),
)

# V1 keeps the full domain vocabulary available while the model remains intentionally
# compact. Fields that are not applicable to a user's education stage retain their
# NOT_APPLICABLE status instead of being coerced to zero.
STUDENT_FEATURES += (
    FeatureSpec("date_of_birth", "category", "date_of_birth"),
    FeatureSpec("age_band", "category", "age_band"),
    FeatureSpec("city", "category", "city"),
    FeatureSpec("village_town", "category", "village_town"),
    FeatureSpec("academic_year", "category", "academic_year"),
    FeatureSpec("percentage", "numeric", "percentage", scale=100),
    FeatureSpec("grade", "category", "grade"),
    FeatureSpec("previous_class_result", "numeric", "previous_class_result", scale=100, applies_to=frozenset({"SCHOOL"})),
    FeatureSpec("hostel_day_scholar", "category", "hostel_day_scholar"),
    FeatureSpec("diploma_course", "category", "diploma_course", frozenset({"DIPLOMA"})),
    FeatureSpec("specialization", "category", "specialization"),
    FeatureSpec("previous_qualification", "category", "previous_qualification"),
    FeatureSpec("tenth_percentage", "numeric", "tenth_percentage", scale=100),
    FeatureSpec("twelfth_percentage", "numeric", "twelfth_percentage", scale=100),
    FeatureSpec("diploma_percentage", "numeric", "diploma_percentage", scale=100),
    FeatureSpec("university", "category", "university"),
    FeatureSpec("course_duration_years", "numeric", "course_duration_years", scale=10),
    FeatureSpec("expected_completion_year", "numeric", "expected_completion_year", scale=2100),
    FeatureSpec("research_degree", "category", "research_degree", frozenset({"PHD_RESEARCH"})),
    FeatureSpec("research_topic", "category", "research_topic", frozenset({"PHD_RESEARCH"})),
    FeatureSpec("research_experience_years", "numeric", "research_experience_years", scale=40, applies_to=frozenset({"PHD_RESEARCH"})),
    FeatureSpec("publications", "numeric", "publications", scale=100, applies_to=frozenset({"PHD_RESEARCH"})),
    FeatureSpec("research_projects", "numeric", "research_projects", scale=100, applies_to=frozenset({"PHD_RESEARCH"})),
    FeatureSpec("fellowship_status", "category", "fellowship_status", frozenset({"PHD_RESEARCH"})),
    FeatureSpec("certification", "category", "certification"),
    FeatureSpec("skill_area", "category", "skill_area"),
    FeatureSpec("training_provider", "category", "training_provider"),
    FeatureSpec("course_level", "category", "course_level"),
    FeatureSpec("completion_status", "category", "completion_status"),
    FeatureSpec("current_occupation", "category", "current_occupation"),
    FeatureSpec("current_income", "numeric", "current_income", scale=10_000_000),
    FeatureSpec("desired_skill", "category", "desired_skill"),
    FeatureSpec("academic_performance_band", "category", "academic_performance_band"),
    FeatureSpec("rank_percentile", "numeric", "rank_percentile", scale=100),
    FeatureSpec("subject_strength", "category", "subject_strength"),
    FeatureSpec("subject_weakness", "category", "subject_weakness"),
    FeatureSpec("entrance_exam_percentile", "numeric", "entrance_exam_percentile", scale=100),
    FeatureSpec("financial_need_score", "numeric", "financial_need_score", scale=1),
    FeatureSpec("number_of_dependents", "numeric", "number_of_dependents", scale=20, aliases=("dependents",)),
    FeatureSpec("earning_members", "numeric", "earning_members", scale=20),
    FeatureSpec("non_earning_members", "numeric", "non_earning_members", scale=20),
    FeatureSpec("education_expenses", "numeric", "education_expenses", scale=10_000_000),
    FeatureSpec("hostel_expenses", "numeric", "hostel_expenses", scale=10_000_000),
    FeatureSpec("tuition_fees", "numeric", "tuition_fees", scale=10_000_000),
    FeatureSpec("disability_status", "category", "disability_status"),
    FeatureSpec("disability_percentage", "numeric", "disability_percentage", scale=100),
    FeatureSpec("minority_status", "category", "minority_status"),
    FeatureSpec("orphan_status", "category", "orphan_status"),
    FeatureSpec("single_parent_status", "category", "single_parent_status"),
    FeatureSpec("parent_occupation", "category", "parent_occupation"),
    FeatureSpec("parent_employment_sector", "category", "parent_employment_sector"),
    FeatureSpec("armed_forces_family", "category", "armed_forces_family"),
    FeatureSpec("farmer_family", "category", "farmer_family"),
    FeatureSpec("target_industry", "category", "target_industry"),
    FeatureSpec("target_job_role", "category", "target_job_role"),
    FeatureSpec("higher_study_interest", "category", "higher_study_interest"),
    FeatureSpec("research_interest", "category", "research_interest"),
    FeatureSpec("government_job_interest", "category", "government_job_interest"),
    FeatureSpec("private_job_interest", "category", "private_job_interest"),
    FeatureSpec("entrepreneurship_interest", "category", "entrepreneurship_interest"),
    FeatureSpec("skill_development_interest", "category", "skill_development_interest"),
    FeatureSpec("study_abroad_interest", "category", "study_abroad_interest"),
)

SCHOLARSHIP_FEATURES += (
    FeatureSpec("required_previous_qualification", "category", "previous_qualification"),
    FeatureSpec("required_region", "category", "region"),
    FeatureSpec("institution_state", "category", "institution_state"),
    FeatureSpec("institution_type", "category", "institution_type"),
    FeatureSpec("minimum_amount", "numeric", "minimum_amount", scale=10_000_000),
    FeatureSpec("maximum_amount", "numeric", "maximum_amount", scale=10_000_000),
    FeatureSpec("books_allowance", "numeric", "books_allowance", scale=10_000_000),
    FeatureSpec("living_allowance", "numeric", "living_allowance", scale=10_000_000),
    FeatureSpec("travel_allowance", "numeric", "travel_allowance", scale=10_000_000),
    FeatureSpec("equipment_allowance", "numeric", "equipment_allowance", scale=10_000_000),
    FeatureSpec("duration_months", "numeric", "duration_months", scale=120),
    FeatureSpec("renewal_conditions", "category", "renewal_conditions"),
    FeatureSpec("application_start", "category", "application_start"),
    FeatureSpec("days_remaining", "numeric", "days_remaining", scale=365),
    FeatureSpec("deadline_urgency", "numeric", "deadline_urgency", scale=1),
    FeatureSpec("application_portal", "category", "application_portal"),
    FeatureSpec("verification_required", "category", "verification_required"),
    FeatureSpec("institute_verification", "category", "institute_verification"),
    FeatureSpec("document_verification", "category", "document_verification"),
    FeatureSpec("number_of_documents", "numeric", "number_of_documents", scale=30),
    FeatureSpec("conditional_documents_count", "numeric", "conditional_documents_count", scale=30),
    FeatureSpec("document_complexity", "category", "document_complexity"),
    FeatureSpec("digital_document_required", "category", "digital_document_required"),
    FeatureSpec("income_certificate", "category", "income_certificate"),
    FeatureSpec("caste_certificate", "category", "caste_certificate"),
    FeatureSpec("bonafide", "category", "bonafide"),
    FeatureSpec("marksheet", "category", "marksheet"),
    FeatureSpec("aadhaar", "category", "aadhaar"),
    FeatureSpec("bank_details", "category", "bank_details"),
    FeatureSpec("last_updated", "category", "last_updated"),
    FeatureSpec("last_verified", "category", "last_verified"),
    FeatureSpec("source_update_frequency", "category", "source_update_frequency"),
    FeatureSpec("academic_selectivity", "numeric", "academic_selectivity", scale=1),
    FeatureSpec("financial_accessibility", "numeric", "financial_accessibility", scale=1),
    FeatureSpec("geographic_coverage", "category", "geographic_coverage"),
    FeatureSpec("category_coverage", "category", "category_coverage"),
    FeatureSpec("application_difficulty", "numeric", "application_difficulty", scale=1),
    FeatureSpec("document_burden", "numeric", "document_burden", scale=1),
    FeatureSpec("scholarship_value", "numeric", "scholarship_value", scale=10_000_000),
    FeatureSpec("renewal_value", "numeric", "renewal_value", scale=1),
    FeatureSpec("source_confidence", "numeric", "source_confidence", scale=1),
)

MODEL_FEATURES = tuple(dict.fromkeys(
    spec.model_key for spec in (*STUDENT_FEATURES, *SCHOLARSHIP_FEATURES, *BEHAVIOR_FEATURES)
))
STATUS_VALUES = tuple(status.value for status in FeatureStatus)
TOKEN_PATTERN = re.compile(r"[^\w]+", re.UNICODE)


def infer_education_stage(course: str | None) -> str | None:
    if not course:
        return None
    normalized = course.lower()
    if re.search(r"\b(class|standard|std\.?)[\s-]*(1[0-2]|[1-9])\b", normalized):
        return "SCHOOL"
    if any(token in normalized for token in ("phd", "ph.d", "doctoral", "research scholar")):
        return "PHD_RESEARCH"
    if any(token in normalized for token in ("diploma", "polytechnic")):
        return "DIPLOMA"
    if any(token in normalized for token in ("master", "postgraduate", "post-graduate", " m.tech", " mba", " m.sc")):
        return "PG"
    if any(token in normalized for token in ("bachelor", "undergraduate", "b.tech", "b.e.", " b.sc", " b.com", " b.a.")):
        return "UG"
    if any(token in normalized for token in ("vocational", "iti", "skill development")):
        return "VOCATIONAL"
    return None


def _feature_value(value: Any) -> FeatureValue | None:
    if value is None:
        return None
    parsed = value if isinstance(value, FeatureValue) else FeatureValue.model_validate(value)
    return parsed


def _raw_value(record: Any, spec: FeatureSpec) -> Any:
    for name in (spec.name, *spec.aliases):
        value = getattr(record, name, None)
        if value is not None:
            return value
    return None


def canonical_education_stage(value: str | None) -> str | None:
    if not value:
        return None
    return {
        "UNDERGRADUATE": "UG",
        "POSTGRADUATE": "PG",
        "CLASS_11_12": "SCHOOL",
        "ITI_VOCATIONAL": "VOCATIONAL",
        "PROFESSIONAL_OTHER": "PROFESSIONAL",
    }.get(str(value).upper(), str(value).upper())


def _known(value: Any) -> FeatureValue:
    return FeatureValue(status=FeatureStatus.KNOWN, value=value)


def _derived_student_features(profile: Any) -> dict[str, FeatureValue]:
    derived: dict[str, FeatureValue] = {}
    age = getattr(profile, "age_years", None)
    if age is not None:
        band = "CHILD" if age < 14 else "ADOLESCENT" if age < 18 else "ADULT"
        derived["age_band"] = _known(band)

    current = getattr(profile, "cgpa", None)
    previous = getattr(profile, "previous_score", None)
    if current is not None and previous is not None:
        current_score = float(current) * 10 if float(current) <= 10 else float(current)
        if current_score > float(previous) + 0.5:
            derived["score_trend"] = _known("IMPROVING")
        elif current_score < float(previous) - 0.5:
            derived["score_trend"] = _known("DECLINING")
        else:
            derived["score_trend"] = _known("STABLE")

    income = getattr(profile, "family_income", None)
    family_size = getattr(profile, "family_size", None)
    if income is not None and family_size and float(family_size) > 0:
        per_member = float(income) / float(family_size)
        derived["financial_need_score"] = _known(max(0.0, min(1.0, 1.0 - per_member / 500_000)))
        if per_member < 100_000:
            derived["financial_need_level"] = _known("HIGH")
        elif per_member < 250_000:
            derived["financial_need_level"] = _known("MEDIUM")
        else:
            derived["financial_need_level"] = _known("LOW")
    return derived


def _derived_scholarship_features(scholarship: Any) -> dict[str, FeatureValue]:
    derived: dict[str, FeatureValue] = {}
    deadline = getattr(scholarship, "application_deadline", None)
    if deadline:
        try:
            from datetime import date
            deadline_date = date.fromisoformat(str(deadline)[:10])
            days = (deadline_date - date.today()).days
            derived["days_remaining"] = _known(max(days, 0))
            derived["deadline_urgency"] = _known(max(0.0, min(1.0, 1.0 - max(days, 0) / 180)))
        except ValueError:
            pass

    documents = getattr(scholarship, "mandatory_documents_count", None)
    if documents is not None:
        derived["document_burden"] = _known(max(0.0, min(1.0, float(documents) / 10)))
    confidence = getattr(scholarship, "evidence_confidence", None)
    if confidence is not None:
        derived["source_confidence"] = _known(float(confidence))
    return derived


def _resolve_features(
    record: Any,
    specs: tuple[FeatureSpec, ...],
    stage: str | None = None,
    explicit: dict[str, FeatureValue] | None = None,
    behavior: dict[str, FeatureValue] | None = None,
    behavior_only: bool = False,
) -> dict[str, FeatureValue]:
    explicit = explicit or {}
    behavior = behavior or {}
    allowed_names = {spec.name for spec in specs}
    supplied = {**explicit, **(behavior if behavior_only else {})}
    unknown_names = set(supplied) - allowed_names
    if unknown_names:
        raise ValueError(f"Unsupported V1 feature name(s): {', '.join(sorted(unknown_names))}")

    values: dict[str, FeatureValue] = {}
    for spec in specs:
        candidate = _feature_value(supplied.get(spec.name))
        if candidate is not None:
            if candidate.status == FeatureStatus.KNOWN and spec.applies_to and stage not in spec.applies_to:
                raise ValueError(f"Feature '{spec.name}' is not applicable to education stage '{stage}'")
            values[spec.name] = candidate
            continue

        raw = _raw_value(record, spec)
        if raw is not None:
            values[spec.name] = FeatureValue(status=FeatureStatus.KNOWN, value=raw)
        elif spec.applies_to and stage is not None and stage not in spec.applies_to:
            values[spec.name] = FeatureValue(status=FeatureStatus.NOT_APPLICABLE)
        else:
            values[spec.name] = FeatureValue(status=FeatureStatus.UNKNOWN)
    return values


def student_features(profile: Any) -> dict[str, FeatureValue]:
    explicit_features = {**_derived_student_features(profile), **(getattr(profile, "features", None) or {})}
    explicit_stage = getattr(profile, "education_stage", None)
    explicit_stage_feature = _feature_value(explicit_features.get("education_stage"))
    if explicit_stage is not None:
        stage = explicit_stage
    elif explicit_stage_feature is not None:
        stage = explicit_stage_feature.value if explicit_stage_feature.status == FeatureStatus.KNOWN else None
    else:
        stage = infer_education_stage(getattr(profile, "course", None))
    stage = canonical_education_stage(stage)
    values = _resolve_features(profile, STUDENT_FEATURES, stage, explicit_features)
    if explicit_stage is not None or (explicit_stage_feature is None and stage in EDUCATION_STAGES):
        values["education_stage"] = FeatureValue(status=FeatureStatus.KNOWN, value=stage)
    behavior = _resolve_features(
        profile,
        BEHAVIOR_FEATURES,
        stage,
        behavior=getattr(profile, "behaviors", None),
        behavior_only=True,
    )
    return {**values, **behavior}


def scholarship_features(scholarship: Any) -> dict[str, FeatureValue]:
    explicit = {**_derived_scholarship_features(scholarship), **(getattr(scholarship, "features", None) or {})}
    values = _resolve_features(scholarship, SCHOLARSHIP_FEATURES, explicit=explicit)
    values.update({
        spec.name: FeatureValue(status=FeatureStatus.NOT_APPLICABLE)
        for spec in BEHAVIOR_FEATURES
    })
    return values


def model_feature_values(
    source_features: dict[str, FeatureValue],
    specs: tuple[FeatureSpec, ...],
    scholarship: bool,
) -> dict[str, FeatureValue]:
    by_model_key: dict[str, list[tuple[FeatureValue, FeatureSpec]]] = {}
    for spec in (*specs, *BEHAVIOR_FEATURES):
        feature = source_features.get(spec.name)
        if feature is not None:
            by_model_key.setdefault(spec.model_key, []).append((feature, spec))

    result: dict[str, FeatureValue] = {}
    for model_key in MODEL_FEATURES:
        choices = by_model_key.get(model_key, [])
        known = next(
            ((feature, spec) for feature, spec in choices if feature.status == FeatureStatus.KNOWN),
            None,
        )
        if known is not None:
            feature, spec = known
            if isinstance(feature.value, (int, float)) and not isinstance(feature.value, bool) and spec.scale:
                feature = FeatureValue(status=FeatureStatus.KNOWN, value=float(feature.value) / spec.scale)
            result[model_key] = feature
        elif choices and all(feature.status == FeatureStatus.NOT_APPLICABLE for feature, _ in choices):
            result[model_key] = FeatureValue(status=FeatureStatus.NOT_APPLICABLE)
        elif scholarship and model_key in {spec.model_key for spec in BEHAVIOR_FEATURES}:
            result[model_key] = FeatureValue(status=FeatureStatus.NOT_APPLICABLE)
        else:
            result[model_key] = FeatureValue(status=FeatureStatus.UNKNOWN)
    return result


def encode_model_features(
    features: dict[str, FeatureValue],
) -> dict[str, float]:
    encoded: dict[str, float] = {}
    for name in MODEL_FEATURES:
        feature = features[name]
        for status in STATUS_VALUES:
            encoded[f"{name}__status={status}"] = float(feature.status.value == status)
        if feature.status != FeatureStatus.KNOWN:
            continue

        raw_value = feature.value
        if isinstance(raw_value, bool):
            encoded[f"{name}__value"] = float(raw_value)
        elif isinstance(raw_value, (int, float)):
            encoded[f"{name}__value"] = float(raw_value)
        else:
            values = raw_value if isinstance(raw_value, list) else [raw_value]
            for value in values:
                normalized = " ".join(TOKEN_PATTERN.sub(" ", str(value).lower()).split())
                if normalized:
                    encoded[f"{name}__value={normalized}"] = 1.0
    return encoded


def encode_student_features(profile: Any) -> dict[str, float]:
    return encode_model_features(
        model_feature_values(student_features(profile), STUDENT_FEATURES, scholarship=False)
    )


def encode_scholarship_features(scholarship: Any) -> dict[str, float]:
    return encode_model_features(
        model_feature_values(scholarship_features(scholarship), SCHOLARSHIP_FEATURES, scholarship=True)
    )
