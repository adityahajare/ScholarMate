# Runtime Validation Report

**Generated**: 2026-10-06T08:05:00+05:30
**Target**: ScholarMate Spring Boot backend, scraper subsystem, and ML service

## Summary

| Step | Status | Exit Code | Details |
|---|---|---:|---|
| Spring startup | PASS | 0 | Java 25.0.2, PostgreSQL 16.13, `/actuator/health` returned 200 |
| Scraper tests | PASS | 0 | Targeted scraper and endpoint-security Maven tests passed |
| ML unit tests | PASS | 0 | 11 tests passed; 2 non-fatal sklearn warnings |
| Backend compile | PASS | 0 | Maven compile completed under Java 25 |
| Frontend production build | PASS | 0 | Vite production bundle completed successfully |
| ML health | PASS | 0 | `/health` returned `ok` |
| ML recommendation | PASS | 0 | 3/3 candidates ranked; semantic signal available; 2,384 ms first request and 217/220 ms warm requests |
| Backend boundary checks | PASS | 0 | 236 active sources and 3,817 scholarships returned |

## Efficiency Findings

- ML startup now warms the embedding model before serving requests. The first measured request was 2,384 ms and warm requests were 217-220 ms for three candidates.
- The expanded V1 feature contract now covers education-stage-specific academic, financial, social, geographic, career, document, source, benefit, deadline, and behavior fields.
- Derived age band, score trend, financial need, deadline urgency, and document burden signals are generated when source data is available.
- Hard eligibility checks are applied for explicitly supplied ML requirements; the Java service continues to enforce database-backed eligibility before invoking ML.
- ML recommendation and backend scraper operations now emit duration logs for lightweight runtime monitoring.
- Semantic scoring was available, so the fallback weighting path was not used.
- Scraper catalog is populated and readable; live scraping execution was not triggered because both scraper routes correctly require authentication.
- `GenericScrapingEngine` now uses bounded concurrency (default maximum: 6 sources) while preserving deterministic result ordering and isolating per-source failures.

## Environment

- Docker: UNAVAILABLE — Docker daemon is not available; no container-fidelity test was run.
- Node.js: AVAILABLE — v22.20.0.
- Browser E2E: UNVERIFIED — Chromium is not installed in the local Playwright runtime.
- Database: AVAILABLE — local PostgreSQL on `localhost:5432`.

## Test Evidence

- [`01-ml-health.ps1`](./01-ml-health.ps1)
- [`02-ml-recommendation.ps1`](./02-ml-recommendation.ps1)
- [`03-backend-scraper-ml-boundaries.ps1`](./03-backend-scraper-ml-boundaries.ps1)
- Existing scraper tests under `scolarmate-backend/src/test/java/com/scholarmate/scraper/`
- Existing ML tests under `scolarmate-backend/ml-service/tests/`

## Verdict

`overall: PASS` for local service health, scraper data availability, security boundaries, ML ranking, warm-request efficiency, and frontend production build. Live scraper execution and browser E2E remain environment-gated checks requiring authenticated credentials and Chromium, respectively.
