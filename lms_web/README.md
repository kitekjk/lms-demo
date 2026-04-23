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

## E2E Testing (Playwright)

Critical 6 scenarios against an isolated MySQL instance.

### Setup
1. Start isolated e2e MySQL (from repo root):
   ```
   docker-compose -f ../docker-compose.e2e.yml up -d
   ```
2. Start backend in `e2e` profile (in a separate terminal, from repo root):
   ```
   .\gradlew :interfaces:bootRun --args='--spring.profiles.active=e2e'
   ```
3. Start Vite dev server (in another terminal, in `lms_web/`):
   ```
   npm run dev
   ```
4. Run the tests (in yet another terminal, in `lms_web/`):
   ```
   npm run test:e2e
   ```

### Scripts
- `npm run test:e2e` — run all specs headless
- `npm run test:e2e:ui` — interactive test runner
- `npm run test:e2e:report` — open last HTML report

### Scenarios (Critical 6)
1. Employee login → /home reaches with name (`auth.spec.ts`)
2. Attendance check-in → check-out → history (`employee-attendance.spec.ts`)
3. Leave request + manager approve + employee sees APPROVED (`leave-flow.spec.ts`)
4. 401 refresh silent recovery (`auth-refresh.spec.ts`)
5. Non-authorized role → /403 (`auth.spec.ts`)
6. Wrong-password error handling (`auth.spec.ts`)

Tests reset DB via `POST /test-only/reset` (only available in `e2e` profile).
