from __future__ import annotations

import logging
from typing import Dict, List
from app.schemas import (
    AdaptiveQuestionRankingRequest,
    AdaptiveQuestionRankingResponse,
    RankedQuestion,
)

log = logging.getLogger(__name__)

# Heuristic information-entropy weights for fields based on eligibility partitioning power
FIELD_DISCRIMINATIVE_WEIGHTS: Dict[str, float] = {
    "annual_family_income": 0.40,
    "domicile_state": 0.35,
    "caste_category": 0.30,
    "education_level": 0.28,
    "course_name": 0.26,
    "previous_percentage": 0.25,
    "current_year": 0.22,
    "gender": 0.20,
    "disability_status": 0.18,
    "accommodation_type": 0.15,
    "has_domicile_certificate": 0.12,
    "income_certificate_available": 0.12,
}

DEFAULT_FIELD_WEIGHT = 0.10


def rank_adaptive_questions(
    request: AdaptiveQuestionRankingRequest,
) -> AdaptiveQuestionRankingResponse:
    """
    Ranks missing candidate question fields based on:
      1. Number of candidate scholarships that depend on the field
      2. Inherent discriminative power of the field (information gain)
      3. Candidate relevance and student context
    """
    missing_fields = [f for f in request.missing_field_keys if f not in request.already_asked_field_keys]
    if not missing_fields:
        return AdaptiveQuestionRankingResponse(ranked_questions=[])

    total_candidates = max(len(request.candidate_scholarship_ids), 1)
    field_req_map = request.scholarship_requirement_field_map or {}
    sch_names = request.scholarship_names or {}

    ranked: List[RankedQuestion] = []

    for field_key in missing_fields:
        target_sch_ids = field_req_map.get(field_key, [])
        target_count = len(target_sch_ids)

        # Fraction of candidate scholarships influenced by this field
        coverage_score = target_count / total_candidates

        # Inherent discriminatory weight
        disc_weight = FIELD_DISCRIMINATIVE_WEIGHTS.get(field_key, DEFAULT_FIELD_WEIGHT)

        # Composite priority score between 0.05 and 0.99
        if target_count > 0:
            priority_score = min(0.99, max(0.10, (coverage_score * 0.60) + (disc_weight * 0.40)))
        else:
            # General profile building field
            priority_score = min(0.30, max(0.05, disc_weight * 0.50))

        # Build clear, transparent explanation for student and agent
        reason = _generate_ranking_reason(field_key, target_sch_ids, sch_names, target_count)

        ranked.append(
            RankedQuestion(
                field_key=field_key,
                priority_score=round(priority_score, 3),
                reason=reason,
                target_scholarship_count=target_count,
            )
        )

    # Sort descending by priority_score, then target_scholarship_count
    ranked.sort(key=lambda q: (q.priority_score, q.target_scholarship_count), reverse=True)

    log.info(
        "Ranked %d questions for student=%s candidates=%d",
        len(ranked),
        request.student_id,
        total_candidates,
    )

    return AdaptiveQuestionRankingResponse(ranked_questions=ranked)


def _generate_ranking_reason(
    field_key: str,
    target_sch_ids: List[str],
    sch_names: Dict[str, str],
    target_count: int,
) -> str:
    if target_count == 0:
        return f"Helps complete your academic profile for broader scholarship discovery"

    sample_names = [sch_names.get(sid, sid) for sid in target_sch_ids[:2]]
    names_str = ", ".join(f"'{name}'" for name in sample_names)

    if field_key == "annual_family_income":
        return f"Resolves family income eligibility for {target_count} scholarship(s) including {names_str}"
    elif field_key in ("domicile_state", "permanent_state"):
        return f"Verifies state and domicile eligibility criteria for {target_count} scholarship(s)"
    elif field_key == "caste_category":
        return f"Required to evaluate social category reservation benefits for {target_count} scheme(s)"
    elif field_key in ("previous_percentage", "cgpa"):
        return f"Evaluates merit criteria across {target_count} scholarship(s)"
    elif field_key == "gender":
        return f"Checks gender-specific scholarship eligibility for {target_count} scheme(s)"
    elif field_key == "accommodation_type":
        return f"Checks hosteller allowance criteria for {target_count} scheme(s)"
    else:
        return f"Required by verified criteria across {target_count} candidate scholarship(s) including {names_str}"
