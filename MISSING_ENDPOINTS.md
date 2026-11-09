# Missing Endpoints Registry

This document tracks any backend endpoints required by the frontend that are not yet natively implemented in the Spring Boot backend, and describes the labeled mock/shim layer in use until the endpoint is delivered.

| Endpoint | Method | Phase Required | Current Status | Mock / Real Implementation Notes |
|---|---|---|---|---|
| `/api/auth/refresh` | POST | Phase 1 | Implemented | Exchange refresh token for new access token |
| `/api/auth/forgot-password` | POST | Phase 1 | Implemented | Initiates password reset flow |
| `/api/auth/reset-password` | POST | Phase 1 | Implemented | Completes password reset with token |
| `/api/admin/sources/health` | GET | Phase 2 | Planned (Phase 2) | Admin scraper health & telemetry |
| `/api/scholarships/{id}/eligibility` | GET | Phase 3 | Planned (Phase 3) | Returns MET / NOT_MET / UNKNOWN per criterion |
| `/api/profile/improvement` | GET | Phase 3 | Planned (Phase 3) | Returns missing fields deep-linked to profile |
| `/api/notifications` | GET/POST/PATCH/DELETE | Phase 5 | Implemented | Notifications API with unread tracking, summary & batch read |

*Rule 4: Do not invent endpoints. Build the backend endpoint, or list it in MISSING_ENDPOINTS.md and use a labelled mock layer.*
