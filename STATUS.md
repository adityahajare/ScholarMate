# ScholarMate implementation status

## Phase 1 — Auth and public pages

| Feature | State | Notes |
|---|---|---|
| Registration stores name, email, mobile and education selection | DONE | Existing profile is reused; schema is extended by Hibernate in the current deployment. |
| BCrypt password storage | DONE | Existing password encoder retained. |
| JWT access and refresh tokens | DONE | `POST /api/auth/refresh` added; refresh tokens are rejected by the request filter. |
| Frontend Bearer authentication | DONE | Password-derived Basic credentials are no longer stored or sent. |
| Duplicate-email error response | DONE | Returns a safe 400 response. |
| Profile ownership checks | DONE | Non-admin users can access only their own `/api/profiles/{id}` resources. |
| Forgot password | UNTESTED | UI control is informational until SMTP/reset-token flow is implemented. |
| Google sign-in | REMOVED | The previous decorative alert button was removed because no backend OAuth endpoint exists. |
| Backend full test suite | PARTIAL | Unit/service tests pass; Spring context tests require the local PostgreSQL credentials to match the running container. |
| Frontend lint/build | DONE | Re-run after the current changes. |
| Vercel frontend CORS default | DONE | `https://schlor-mate.vercel.app` is included; Render env should still explicitly set `SCHOLARMATE_FRONTEND_URL`. |
| Profile education/builder route protection | DONE | Both profile setup routes now require an authenticated session. |

Phases 2–7 remain pending until this phase passes its environment-backed test gate.
