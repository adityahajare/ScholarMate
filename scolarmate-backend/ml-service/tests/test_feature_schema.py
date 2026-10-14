import pytest
from pydantic import ValidationError

from app.feature_schema import (
    EducationStage,
    FeatureStatus,
    FeatureValue,
    encode_scholarship_features,
    encode_student_features,
    student_features,
)
from app.schemas import ScholarshipCandidate, StudentProfile
from app.recommendation import _hard_eligible


def test_school_profile_distinguishes_not_applicable_from_unknown():
    profile = StudentProfile.model_validate({
        "educationStage": "SCHOOL",
        "course": "Class 10",
        "state": "Maharashtra",
    })

    features = student_features(profile)
    encoded = encode_student_features(profile)

    assert features["branch"].status == FeatureStatus.NOT_APPLICABLE
    assert features["current_score"].status == FeatureStatus.UNKNOWN
    assert features["class_number"].status == FeatureStatus.UNKNOWN
    assert encoded["branch__status=NOT_APPLICABLE"] == 1.0
    assert encoded["academic_score__status=UNKNOWN"] == 1.0
    assert "branch__value" not in encoded
    assert "academic_score__value" not in encoded


def test_legacy_profile_values_are_known_and_zero_is_not_treated_as_missing():
    profile = StudentProfile.model_validate({"cgpa": 0.0, "familyIncome": 0.0})

    features = student_features(profile)
    encoded = encode_student_features(profile)

    assert features["current_score"].status == FeatureStatus.KNOWN
    assert features["annual_family_income"].status == FeatureStatus.KNOWN
    assert encoded["academic_score__status=KNOWN"] == 1.0
    assert encoded["academic_score__value"] == 0.0
    assert encoded["family_income__status=KNOWN"] == 1.0


def test_scholarship_known_requirement_is_normalized_and_behavior_is_not_applicable():
    scholarship = ScholarshipCandidate.model_validate({
        "id": 1,
        "name": "Merit Scholarship",
        "minimumCgpa": 7.5,
    })

    encoded = encode_scholarship_features(scholarship)

    assert encoded["academic_score__status=KNOWN"] == 1.0
    assert encoded["academic_score__value"] == 0.75
    assert encoded["views_last_7_days__status=NOT_APPLICABLE"] == 1.0


def test_non_known_feature_cannot_carry_a_value():
    with pytest.raises(ValidationError):
        FeatureValue(status=FeatureStatus.UNKNOWN, value=0)


def test_unknown_education_stage_is_rejected():
    with pytest.raises(ValidationError):
        StudentProfile.model_validate({"educationStage": "ADULT"})


def test_legacy_course_infers_education_stage():
    profile = StudentProfile.model_validate({"course": "B.Tech Computer Science"})

    assert EducationStage.UG.value == "UG"
    assert student_features(profile)["education_stage"].value == "UG"


def test_explicit_unknown_stage_is_not_overridden_by_course_inference():
    profile = StudentProfile.model_validate({
        "course": "B.Tech Computer Science",
        "features": {"education_stage": {"status": "UNKNOWN"}},
    })

    features = student_features(profile)

    assert features["education_stage"].status == FeatureStatus.UNKNOWN
    assert features["branch"].status == FeatureStatus.UNKNOWN


def test_derived_features_are_status_aware():
    profile = StudentProfile.model_validate({
        "ageYears": 16,
        "cgpa": 8.0,
        "previousScore": 70.0,
        "familyIncome": 120000.0,
        "familySize": 4,
    })

    features = student_features(profile)

    assert features["age_band"].value == "ADOLESCENT"
    assert features["score_trend"].value == "IMPROVING"
    assert features["financial_need_level"].value == "HIGH"
    assert features["financial_need_score"].status == FeatureStatus.KNOWN


def test_hard_eligibility_rejects_known_requirement_mismatch_but_keeps_unknown_safe():
    student = StudentProfile.model_validate({
        "educationStage": "UG",
        "state": "Maharashtra",
        "cgpa": 8.2,
    })
    matching = ScholarshipCandidate.model_validate({
        "id": 1,
        "name": "Matching",
        "minimumCgpa": 7.5,
        "features": {"required_state": {"status": "KNOWN", "value": "Maharashtra"}},
    })
    mismatching = ScholarshipCandidate.model_validate({
        "id": 2,
        "name": "Mismatching",
        "minimumCgpa": 7.5,
        "features": {"required_state": {"status": "KNOWN", "value": "Kerala"}},
    })
    unknown_requirement = ScholarshipCandidate.model_validate({
        "id": 3,
        "name": "Unknown requirement",
        "minimumCgpa": 7.5,
    })

    assert _hard_eligible(student, matching)
    assert not _hard_eligible(student, mismatching)
    assert _hard_eligible(student, unknown_requirement)
