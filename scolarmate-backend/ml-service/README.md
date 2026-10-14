# ScholarMate ML Service

This Python service is a separate recommendation module. Spring remains responsible for PostgreSQL access and deterministic eligibility checks; only eligible scholarship candidates are sent to this service for ranking.

The pipeline combines a 32-dimensional PyTorch autoencoder, K-Means, Gaussian Mixture and HDBSCAN cluster signals, Sentence-Transformers semantic similarity, and graph PageRank similarity over shared course/category/state nodes. Cluster IDs are learned per request and are not assigned human labels. The response includes per-signal scores and clustering metrics.

## Feature schema

Requests use `scholarmate-features-v1`. `educationStage` accepts `SCHOOL`, `DIPLOMA`, `UG`, `PG`, `PHD_RESEARCH`, `PROFESSIONAL`, `VOCATIONAL`, `SKILL_DEVELOPMENT`, or `OTHER`. When omitted, a stage may be inferred from a legacy course name; an explicit stage or explicit feature status always takes precedence.

Each extensible `features` or `behaviors` entry uses the same status envelope:

```json
{
	"branch": {"status": "NOT_APPLICABLE"},
	"class_number": {"status": "KNOWN", "value": 10},
	"current_score": {"status": "UNKNOWN"}
}
```

`KNOWN` requires a value, including a legitimate numeric zero. `UNKNOWN` and `NOT_APPLICABLE` cannot carry values. Missing stage-specific fields are `UNKNOWN` when they apply and `NOT_APPLICABLE` when they do not apply to a known stage. Unknown stage means applicability is also unknown, so stage-specific features remain `UNKNOWN` rather than being discarded.

The structured vector emits separate one-hot indicators for all three statuses and encodes a value only for `KNOWN`; it never fills missing numeric data with zero. Spring stores student features/behaviors and scholarship features as JSONB status maps and forwards them to this service. Current profile fields remain supported and are mapped into the V1 catalog; new profile forms and interactions can be added incrementally through the same status map. The catalog lives in `app/feature_schema.py`; the API response reports its version.

## Run locally

Use Python 3.11 or newer. From this directory:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

The first request downloads the configured sentence-transformer model. Set `SCHOLARMATE_EMBEDDING_MODEL` to another Sentence-Transformers model if needed. If model loading fails, the API reports the semantic signal as unavailable and reweights the other signals; it does not claim that embeddings were used.

Spring calls `POST /api/v1/recommend` at `scholarmate.ml.base-url` (default `http://localhost:8000`).

## Verify

```powershell
python -m pytest
```