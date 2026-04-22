# LMS Web (React)

React port of the LMS web UI. Coexists with Flutter app in `lms_mobile_web/` (mobile) and shares the Spring Boot backend.

## Prerequisites
- Node.js 20+
- Running backend at http://localhost:8080 (`./gradlew :interfaces:bootRun` with the `local` profile)
- Running MySQL (via root `docker-compose up -d`)

## Development
```
npm install
npm run dev         # http://localhost:5173
npm run test        # vitest watch
npm run test:run    # vitest single-run
npm run typecheck
npm run build
```

API calls go through Vite proxy: `/api/*` → `http://localhost:8080/*`.

## Structure
See `docs/superpowers/specs/2026-04-22-lms-web-react-port-design.md` for architecture, and `docs/superpowers/plans/2026-04-22-lms-web-foundation.md` for the implementation plan.
