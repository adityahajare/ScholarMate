# ScholarMate Existing Repository Inspection

Inspected on 2026-10-15 before starting the requested phase work.

## Repository shape

This is an existing full-stack repository with three runtime parts:

- React/Vite frontend: `SchlorMateFrontend/SchlorMate`
- Spring Boot backend: `scolarmate-backend`
- FastAPI ML service: `scolarmate-backend/ml-service`
- Local orchestration: root `docker-compose.yml`

The attached `App.jsx` snapshots are read-only attachments and are not source files. The actual frontend entry point is `SchlorMateFrontend/SchlorMate/src/App.jsx`.

## Existing stack

- Backend build file: `scolarmate-backend/pom.xml`
- Actual Spring Boot parent: `4.1.1` (the request says Spring Boot 3; this must not be downgraded without a compatibility decision).
- Java compiler level: `25`.
- Backend dependencies include Spring Web, Data JPA, Security, Validation, Actuator, PostgreSQL, Jsoup, PDFBox, Tess4J, JJWT and Spring test.
- Frontend: React `19.2.8`, React Router `7.18.3`, Vite `8.2.2`, Tailwind CSS `4.3.3`, Axios and Lucide React.
- ML: FastAPI/Uvicorn with PyTorch, scikit-learn, sentence-transformers and a deterministic fallback when semantic embeddings are disabled.

## Frontend routes and structure

The route table is in `SchlorMateFrontend/SchlorMate/src/App.jsx`.

Existing route groups:

- Authentication: `/login`, `/register`, `/onboarding/verify`
- Student shell: `/dashboard`, `/profile`, `/scholarships`, `/recommendations`, `/prediction`, `/improvements`, `/applications`, `/reports`
- Scholarship detail/application: `/scholarships/:id`, `/scholarships/:id/apply`, `/application-success`
- Profile builder: `/profile/education`, `/profile/builder`, `/profile/builder/review`
- Fallback: `*` to `NotFound`

The main authenticated layout and sidebar are in `src/components/layout/MainLayout.jsx` and `src/components/layout/Sidebar.jsx`. API calls are centralized in `src/lib/api.js`. Authentication state is in `src/context/AuthContext.jsx`.

Current known frontend concerns:

- `/` redirects directly to `/dashboard`; there is no public landing route matching the reference image.
- Profile builder routes are not consistently wrapped in the authenticated main layout; this needs verification during the profile phase.
- Several pages use `Promise.all` or request chains, so one unavailable optional service can make a whole page appear unavailable.
- Recommendation pages depend on `/api/ml/recommendations/me` and currently show an error when the ML service is unavailable.
- The frontend API client now normalizes a configured API URL to include exactly one `/api` suffix and uses a 60-second request timeout.

## Backend layers

Observed counts from the source tree:

- 120 Java files
- 17 controllers
- 24 services
- 14 repositories
- 16 entity classes
- 25 DTO classes
- 5 config classes
- 10 scraper classes

Important controllers include:

- `AuthController`: `/api/auth/register`, `/api/auth/login`
- `UserProfileController`: `/api/profiles/me` and user profile routes
- `ProfileDraftController`: `/api/profile/draft`
- `EducationRecordController`: `/api/profile/education`
- `ProfileChildDataController`: family, address and document routes
- `ScholarshipController`: catalog, search and detail routes
- `MlRecommendationController`: `/api/ml/recommendations/me`, `/status`
- `StudentDocumentReviewController`: OCR/document review routes
- `ScraperController`: admin scraper routes
- criteria, evidence, required-document, parser and source controllers

Security currently uses Spring Security HTTP Basic with a custom authentication entry point. JJWT dependencies and JWT-related properties exist, but the current login response and request interceptor must be checked against the phase-1 JWT requirement before changing the contract. Existing role checks include `STUDENT`/`ADMIN` role-based scraper protection.

## Existing data model

Existing JPA entities include:

- `User` -> `users`
- `UserProfile` -> `student_profiles`
- `EducationRecord` -> `education_records`
- `FamilyMember` -> `profile_family_members`
- `ProfileAddress` -> `profile_addresses`
- `ProfileDocument` -> `profile_documents`
- `ProfileDraft` -> `profile_draft`
- `Scholarship` -> `scholarships`
- `ScholarshipSource` -> `scholarship_sources`
- `ScholarshipEvidence` -> `scholarship_evidence`
- `EligibilityCriteria` -> `eligibility_criteria`
- `RequiredDocument` -> `required_documents`
- `DocumentReviewRecord` -> `document_review_records`
- `NoteEntity` -> `notes`

Relationships already support user-owned profiles and normalized education, family, address and document records. The implementation must extend these entities instead of adding duplicate profile APIs.

## Database and migrations

- PostgreSQL is the configured database.
- `spring.jpa.hibernate.ddl-auto=update` is currently enabled.
- Existing migration directory: `scolarmate-backend/src/main/resources/db/migration`, currently containing `V001__create_profile_draft.sql`.
- Flyway is not currently visible as a `pom.xml` dependency. Phase 7 must add and validate Flyway before disabling `ddl-auto=update`.

## Existing API client and service seam

The frontend uses Axios from `src/lib/api.js` with a `VITE_API_URL` environment variable. The backend calls the ML service through `MlRecommendationClient` using:

- health: `GET /health`
- ranking: `POST /api/v1/recommend`

The ML service health endpoint has been verified separately as available in the deployed environment. The backend still requires `SCHOLARMATE_ML_BASE_URL=https://scholarmate-ml.onrender.com` in Render.

## Configuration and secret findings

Existing external configuration includes database credentials, JWT secret, ML base URL, document storage directory, OCR tessdata path, scraper controls and frontend CORS origin. The backend `.env.example` exists under `scolarmate-backend/.env.example`; a root `.env.example` is not present.

No real secrets will be added to source files. The requested secret names that must be represented in `.env.example` are `SCHOLARMATE_DB_USERNAME`, `SCHOLARMATE_DB_PASSWORD`, `SCHOLARMATE_ML_BASE_URL`, `SCHOLARMATE_DOCUMENT_STORAGE_DIR`, `SCHOLARMATE_JWT_SECRET`, `SMTP_*` and `LLM_API_KEY`.

## Initial implementation risks and gates

1. Authentication contract: current deployed login works, but the requested JWT access/refresh lifecycle and ownership checks need a dedicated phase-1 inspection and tests.
2. Startup time: source initialization and JPA startup previously caused Render delays. Source initialization is now property-controlled and the deployed service reaches `Started ScholarmateBackendApplication`.
3. Scholarship volume: catalog and recommendation code must remain paginated/bounded at the HTTP and ML boundaries.
4. ML failure isolation: optional recommendation failures must not prevent dashboard/profile pages from loading.
5. Database migration: `ddl-auto=update` must remain until a complete Flyway baseline is created and verified.
6. Source scraping: robots/terms checks and source-health persistence are not yet evidenced for all required sources.

## Phase plan

- Phase 1: authentication, public pages, JWT lifecycle, ownership tests, and landing/auth UI.
- Phase 2: source pipeline schema, parser policy evidence, sync runs, deduplication and source health.
- Phase 3: eligibility contracts, details tabs, recommendation ranking and profile improvement.
- Phase 4: document/OCR confirmation and application state machine.
- Phase 5: transparent student features and real-data empty states.
- Phase 6: chatbot, i18n, guardian mode, admin authorization and accessibility.
- Phase 7: Flyway, deployment hardening, security audit and CI.

Each phase must pass backend build/tests, frontend lint/build, a smoke test, `STATUS.md` update, and a phase commit before the next phase begins.
