from app import recommendation
from app.schemas import RecommendationRequest


def make_request() -> RecommendationRequest:
    return RecommendationRequest.model_validate(
        {
            "student": {
                "course": "Engineering",
                "college": "State Engineering College",
                "cgpa": 8.4,
                "familyIncome": 240000,
                "category": "OBC",
                "state": "Maharashtra",
                "domicileState": "Maharashtra",
                "yearOfStudy": 2,
                "gender": "Female",
            },
            "scholarships": [
                {
                    "id": 1,
                    "name": "Maharashtra Engineering Merit Scholarship",
                    "description": "Engineering students may apply for this merit scholarship.",
                    "provider": "State Department",
                    "course": "Engineering",
                    "category": "OBC",
                    "eligibleCategories": "OBC, SC",
                    "state": "Maharashtra",
                    "minimumCgpa": 7.0,
                    "maximumFamilyIncome": 500000,
                    "minimumYearOfStudy": 1,
                    "maximumYearOfStudy": 4,
                },
                {
                    "id": 2,
                    "name": "National Arts Fellowship",
                    "description": "Fellowship for students pursuing fine arts.",
                    "provider": "National Arts Council",
                    "course": "Fine Arts",
                    "category": "General",
                    "state": "Delhi",
                    "minimumCgpa": 7.5,
                    "maximumFamilyIncome": 300000,
                    "minimumYearOfStudy": 2,
                    "maximumYearOfStudy": 4,
                },
                {
                    "id": 3,
                    "name": "Women in Technology Award",
                    "description": "Financial award for women studying technology.",
                    "provider": "Technology Foundation",
                    "course": "Computer Science",
                    "category": "OBC",
                    "state": "Maharashtra",
                    "minimumCgpa": 8.0,
                    "maximumFamilyIncome": 400000,
                    "minimumYearOfStudy": 1,
                    "maximumYearOfStudy": 4,
                },
            ],
        }
    )


def test_build_recommendations_returns_ranked_signals_and_cluster_diagnostics(monkeypatch):
    monkeypatch.setattr(
        recommendation,
        "semantic_scores",
        lambda student, scholarships: ([0.9, 0.2, 0.7], "available"),
    )

    response = recommendation.build_recommendations(make_request())

    assert len(response.recommendations) == 3
    assert {item.scholarship_id for item in response.recommendations} == {1, 2, 3}
    assert response.recommendations[0].score >= response.recommendations[-1].score
    assert response.pipeline.latent_dimensions == 32
    assert set(response.pipeline.clustering_metrics) == {"kmeans", "gmm", "hdbscan"}
    assert set(response.recommendations[0].signals.model_dump()) == {
        "autoencoder",
        "semantic",
        "clustering",
        "graph",
    }
    assert set(response.recommendations[0].clusters) == {"kmeans", "gmm", "hdbscan"}


def test_semantic_unavailable_is_reported_and_other_weights_are_normalized(monkeypatch):
    monkeypatch.setattr(
        recommendation,
        "semantic_scores",
        lambda student, scholarships: ([0.0] * len(scholarships), "unavailable"),
    )

    response = recommendation.build_recommendations(make_request())

    assert response.pipeline.semantic_status == "unavailable"
    assert "semantic" not in response.pipeline.ranking_weights
    assert sum(response.pipeline.ranking_weights.values()) == 1.0
    assert all(item.signals.semantic == 0.0 for item in response.recommendations)