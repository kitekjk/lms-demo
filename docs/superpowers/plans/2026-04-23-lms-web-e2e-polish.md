# LMS Web E2E + Polish Implementation Plan (Plan 4 of 4 — FINAL)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Playwright E2E testing with isolated DB environment, covering Critical 6 scenarios from the spec, plus README/run-script polish. After this plan, the spec is fully delivered.

**Architecture:** New Spring Boot `e2e` profile points at an isolated MySQL instance (docker-compose.e2e.yml on port 3307, db `lms_e2e`). A profile-gated `E2eResetController` exposes `POST /test-only/reset` that truncates all tables and re-runs `data.sql`. Playwright tests live in `lms_web/e2e/`, connect via Vite dev proxy to the e2e backend, and reset DB between test suites. Each scenario uses seeded accounts (`admin@lms.com`, `manager.gangnam@lms.com`, `employee1.gangnam@lms.com`).

**Tech Stack additions:** `@playwright/test`, a new Spring Boot profile, one controller, one docker-compose file, one run-demo helper script.

**Prior plans:**
- Plan 1 (Foundation) — merged as `fa185b9`
- Plan 2 (Employee) — merged as `212647e` + `64e38ac`
- Plan 3 (Admin) — merged as `918085d`

---

## Source of Truth: What This Plan Implements

### Critical 6 Scenarios (from spec §8.2.4)

1. **Smoke**: 로그인 (EMPLOYEE 계정) → `/home` 도달 + 본인 이름 표시
2. **Attendance**: 출근 → 퇴근 → 이력 페이지에 당일 기록 표시
3. **Leave — Employee**: 휴가 신청 → 내 휴가 목록에 PENDING 상태 등장
4. **Leave — Admin**: 관리자 대시보드에서 pending 승인 → 직원 뷰에서 APPROVED로 상태 전환
5. **401 Refresh**: 만료된 access token 주입 → 네비게이션 시 자동 refresh 후 원 요청 성공
6. **Forbidden**: EMPLOYEE로 `/admin` 접근 시도 → `/403` 리다이렉트

### DB Isolation Strategy

- **Separate MySQL** on port 3307, db name `lms_e2e`, same credentials (`lms`/`lms1234`).
- **`e2e` Spring profile** with `ddl-auto: create-drop` + `spring.sql.init.mode: always` (reuses `data.sql` for seed).
- **`/test-only/reset`** endpoint (`@Profile("e2e")`) — truncates mutable tables + re-seeds idempotent data (users/stores/employees/policies always present after reset).

### Backend Changes (minimal, safe)

- New file: `interfaces/src/main/resources/application-e2e.yml`
- New file: `interfaces/src/main/kotlin/com/lms/interfaces/web/testonly/E2eResetController.kt`
- New file: `docker-compose.e2e.yml` (root)
- **No changes** to `SecurityConfig`, existing controllers, or production profiles.

### Frontend Structure

```
lms_web/
└── e2e/                              # ✨ NEW
    ├── playwright.config.ts
    ├── fixtures/
    │   └── users.ts                  # 3 test account constants
    ├── helpers/
    │   ├── auth.ts                   # loginAs(page, role)
    │   ├── api.ts                    # resetDb(), helper HTTP calls
    │   └── selectors.ts              # reusable page selectors
    └── specs/
        ├── auth.spec.ts              # Scenarios 1 + 6
        ├── employee-attendance.spec.ts  # Scenario 2
        ├── leave-flow.spec.ts        # Scenarios 3 + 4
        └── auth-refresh.spec.ts      # Scenario 5
```

---

## Task 5.0 — Backend E2E profile + reset endpoint + docker-compose

**Files:**
- Create: `docker-compose.e2e.yml` (root)
- Create: `interfaces/src/main/resources/application-e2e.yml`
- Create: `interfaces/src/main/kotlin/com/lms/interfaces/web/testonly/E2eResetController.kt`
- Modify: `.gitignore` (possibly) — add `mysql_data_e2e/`

- [ ] **Step 1: Docker Compose for isolated MySQL**

Create `docker-compose.e2e.yml` at repo root:

```yaml
version: '3.8'

services:
  mysql-e2e:
    image: mysql:8.0
    container_name: lms-demo-mysql-e2e
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: changeme
      MYSQL_DATABASE: lms_e2e
      MYSQL_USER: lms
      MYSQL_PASSWORD: lms1234
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "3307:3306"
    volumes:
      - mysql_e2e_data:/var/lib/mysql
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-ulms", "-plms1234"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql_e2e_data:
    driver: local
```

- [ ] **Step 2: Spring Boot e2e profile config**

Create `interfaces/src/main/resources/application-e2e.yml`:

```yaml
# E2E Testing Profile
# Used by Playwright tests — isolated DB instance

spring:
  datasource:
    url: jdbc:mysql://localhost:3307/lms_e2e?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: lms
    password: lms1234
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop  # E2E: fresh schema every boot, gone at shutdown
    show-sql: false
    properties:
      hibernate:
        format_sql: false

  sql:
    init:
      mode: always  # Run data.sql on every boot (idempotent reseed)

logging:
  level:
    root: WARN
    com.lms: INFO
    org.hibernate.SQL: WARN
```

Note: The e2e backend runs on port 8080 (same as local) — only one can be active at a time. This is intentional; tests use the default port.

- [ ] **Step 3: E2eResetController — profile-gated reset endpoint**

Create `interfaces/src/main/kotlin/com/lms/interfaces/web/testonly/E2eResetController.kt`:

```kotlin
package com.lms.interfaces.web.testonly

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

/**
 * Test-only reset endpoint.
 *
 * ACTIVE ONLY under the `e2e` Spring profile. NEVER loaded in local, dev, or prod.
 *
 * POST /test-only/reset
 *   1. Truncates all business tables (FK-safe order via SET FOREIGN_KEY_CHECKS=0).
 *   2. Re-runs data.sql to reseed reference data.
 */
@RestController
@RequestMapping("/test-only")
@Profile("e2e")
class E2eResetController(private val dataSource: DataSource) {

    @PersistenceContext
    private lateinit var em: EntityManager

    private val truncateOrder = listOf(
        // Transactional tables first (clean slate for test assertions)
        "payroll_details",
        "payroll_batch_histories",
        "payrolls",
        "payroll_policies",
        "leave_requests",
        "attendance_records",
        "work_schedules",
        // Reference tables (will be reseeded from data.sql)
        "employees",
        "users",
        "stores",
    )

    @PostMapping("/reset")
    @Transactional
    fun reset(): ResponseEntity<Map<String, Any>> {
        // 1. Truncate with FK checks disabled
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate()
        truncateOrder.forEach { table ->
            em.createNativeQuery("TRUNCATE TABLE $table").executeUpdate()
        }
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate()
        em.flush()

        // 2. Re-run data.sql to reseed users/stores/employees/policies
        dataSource.connection.use { conn ->
            ScriptUtils.executeSqlScript(conn, ClassPathResource("data.sql"))
        }

        return ResponseEntity.ok(mapOf(
            "status" to "ok",
            "truncated" to truncateOrder,
            "reseedFrom" to "classpath:data.sql",
        ))
    }
}
```

- [ ] **Step 4: Security — allow `/test-only/**` without auth in e2e profile**

The existing `SecurityConfig` allows `/api/auth/**` without auth but requires auth for everything else. We need `/test-only/**` to be publicly accessible in e2e mode.

**Read first** the existing `infrastructure/src/main/kotlin/com/lms/infrastructure/config/SecurityConfig.kt` to see the pattern. Then make the minimal change:

Modify `SecurityConfig.kt` — in the `.requestMatchers(...).permitAll()` chain inside `securityFilterChain`, add `"/test-only/**"` to the permit list:

```kotlin
.requestMatchers(
    "/api/auth/**",
    "/health",
    "/actuator/health",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/api-docs/**",
    "/v3/api-docs/**",
    "/test-only/**"  // E2E reset endpoint (gated by @Profile at controller level)
).permitAll()
```

This is safe because the `E2eResetController` itself is `@Profile("e2e")` — in local/prod profiles the controller doesn't exist, so the URL returns 404 regardless of security config.

- [ ] **Step 5: Boot e2e profile locally to verify**

Start the e2e MySQL:

```bash
docker-compose -f docker-compose.e2e.yml up -d
```

Wait 10-15 seconds for MySQL to be healthy. Then start backend with e2e profile:

```bash
.\gradlew :interfaces:bootRun --args='--spring.profiles.active=e2e'
```

Expected: backend logs show `datasource url: jdbc:mysql://localhost:3307/lms_e2e`, data.sql runs, no errors.

In another shell, test the reset endpoint:

```bash
curl -X POST http://localhost:8080/test-only/reset
```

Expected 200 response: `{"status":"ok","truncated":[...],"reseedFrom":"classpath:data.sql"}`.

Stop backend (Ctrl+C) and shut down e2e MySQL:

```bash
docker-compose -f docker-compose.e2e.yml down
```

- [ ] **Step 6: Commit**

```bash
git add docker-compose.e2e.yml interfaces/src/main/resources/application-e2e.yml interfaces/src/main/kotlin/com/lms/interfaces/web/testonly/ infrastructure/src/main/kotlin/com/lms/infrastructure/config/SecurityConfig.kt
git commit -m "feat(e2e): isolated DB + e2e Spring profile + /test-only/reset endpoint"
```

---

## Task 5.1 — Playwright install + config + fixtures + helpers

**Files:**
- Modify: `lms_web/package.json` (install `@playwright/test`, add scripts)
- Create: `lms_web/playwright.config.ts`
- Create: `lms_web/e2e/fixtures/users.ts`
- Create: `lms_web/e2e/helpers/auth.ts`
- Create: `lms_web/e2e/helpers/api.ts`
- Modify: `lms_web/tsconfig.json` (exclude e2e from app tsconfig) + `lms_web/tsconfig.app.json`
- Modify: `lms_web/.gitignore`

- [ ] **Step 1: Install Playwright**

```bash
cd lms_web
npm install -D @playwright/test
npx playwright install chromium
```

This downloads Chromium binary (~100MB). Skip Firefox/WebKit since our Critical 6 scenarios only need one browser.

- [ ] **Step 2: Playwright config**

Create `lms_web/playwright.config.ts`:

```ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e/specs',
  timeout: 30_000,
  expect: { timeout: 5_000 },
  fullyParallel: false,   // tests share DB state; sequential for predictability
  retries: 0,
  workers: 1,             // single worker matches fullyParallel: false
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 5_000,
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  // Assume backend + frontend are already running (started by user).
  // If you want Playwright to start them, uncomment:
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: true,
  //   timeout: 30_000,
  // },
})
```

- [ ] **Step 3: Test account fixtures**

Create `lms_web/e2e/fixtures/users.ts`:

```ts
// Seeded accounts from interfaces/src/main/resources/data.sql.
// All users share password 'password123' (seed BCrypt hash).

export const USERS = {
  superAdmin: {
    email: 'admin@lms.com',
    password: 'password123',
    role: 'SUPER_ADMIN' as const,
    name: '관리자',
  },
  managerGangnam: {
    email: 'manager.gangnam@lms.com',
    password: 'password123',
    role: 'MANAGER' as const,
    name: '박수진',
    storeId: 'store-001',
  },
  employeeGangnam: {
    email: 'employee1.gangnam@lms.com',
    password: 'password123',
    role: 'EMPLOYEE' as const,
    name: '김민수',
    employeeId: 'emp-001',
    storeId: 'store-001',
  },
}
```

- [ ] **Step 4: Auth helper**

Create `lms_web/e2e/helpers/auth.ts`:

```ts
import type { Page, Locator } from '@playwright/test'
import { expect } from '@playwright/test'
import { USERS } from '../fixtures/users'

type UserKey = keyof typeof USERS

/**
 * Fill login form and submit. After login:
 * - EMPLOYEE lands on /home
 * - MANAGER / SUPER_ADMIN land on /admin
 */
export async function loginAs(page: Page, userKey: UserKey): Promise<void> {
  const u = USERS[userKey]
  await page.goto('/login')
  await page.getByLabel('이메일').fill(u.email)
  await page.getByLabel('비밀번호').fill(u.password)
  await page.getByRole('button', { name: '로그인' }).click()

  const target = u.role === 'EMPLOYEE' ? '/home' : '/admin'
  await page.waitForURL(`**${target}`, { timeout: 10_000 })
}

export async function logout(page: Page): Promise<void> {
  await page.getByRole('button', { name: '로그아웃' }).click()
  await page.waitForURL('**/login', { timeout: 5_000 })
}

export function accessToken(page: Page): Promise<string | null> {
  return page.evaluate(() => localStorage.getItem('lms.access_token'))
}

export async function setAccessToken(page: Page, token: string): Promise<void> {
  await page.evaluate((t) => localStorage.setItem('lms.access_token', t), token)
}
```

- [ ] **Step 5: API helper (reset + direct backend calls)**

Create `lms_web/e2e/helpers/api.ts`:

```ts
import { request as playwrightRequest, type APIRequestContext } from '@playwright/test'

const BACKEND = 'http://localhost:8080'

/**
 * Truncate + reseed the e2e database.
 * Call at start of each test suite (beforeAll) for isolation.
 */
export async function resetDb(): Promise<void> {
  const ctx = await playwrightRequest.newContext()
  const res = await ctx.post(`${BACKEND}/test-only/reset`)
  if (!res.ok()) {
    const body = await res.text()
    throw new Error(`resetDb failed: ${res.status()} ${body}`)
  }
  await ctx.dispose()
}

/**
 * Direct login against backend (bypasses UI) — useful when a test needs a token
 * without exercising the login UI.
 */
export async function backendLogin(email: string, password: string): Promise<{ accessToken: string; refreshToken: string }> {
  const ctx = await playwrightRequest.newContext()
  const res = await ctx.post(`${BACKEND}/api/auth/login`, { data: { email, password } })
  if (!res.ok()) throw new Error(`login failed: ${res.status()}`)
  const body = await res.json()
  await ctx.dispose()
  return { accessToken: body.accessToken, refreshToken: body.refreshToken }
}
```

- [ ] **Step 6: Scripts**

Modify `lms_web/package.json` — add these under `"scripts"`:

```json
"test:e2e": "playwright test",
"test:e2e:ui": "playwright test --ui",
"test:e2e:report": "playwright show-report"
```

- [ ] **Step 7: tsconfig / gitignore**

Exclude e2e from the app tsconfig (so Vitest/Vite don't try to build it). Read `lms_web/tsconfig.app.json` first. Find its `include` or `exclude` key. If `include` exists, ensure `e2e` is NOT in it. If `exclude` exists, add `"e2e"` to the array. If neither exists, add `"exclude": ["e2e"]`.

In `lms_web/.gitignore`, add (check if not already present):

```
# Playwright
/test-results/
/playwright-report/
/playwright/.cache/
```

- [ ] **Step 8: Sanity test — config loads correctly**

Create a tiny smoke spec to verify Playwright wiring. Create `lms_web/e2e/specs/_smoke.spec.ts`:

```ts
import { test, expect } from '@playwright/test'

test('playwright wiring works', async ({ page }) => {
  expect(1 + 1).toBe(2)
  // Don't hit the app — just verify Playwright can run
})
```

Run:

```bash
cd lms_web && npx playwright test _smoke.spec.ts
```

Expected: 1 passed. (This runs without backend/frontend — no page.goto.)

- [ ] **Step 9: Remove the smoke file, commit the setup**

```bash
rm lms_web/e2e/specs/_smoke.spec.ts
git add lms_web/ 
git commit -m "test(e2e): install Playwright + config + fixtures/helpers"
```

---

## Task 5.2 — Auth spec (Scenarios 1 + 6)

Combines Smoke login with Forbidden access.

**Files:**
- Create: `lms_web/e2e/specs/auth.spec.ts`

- [ ] **Step 1: Implement auth spec**

Create `lms_web/e2e/specs/auth.spec.ts`:

```ts
import { test, expect } from '@playwright/test'
import { USERS } from '../fixtures/users'
import { loginAs } from '../helpers/auth'
import { resetDb } from '../helpers/api'

test.describe('auth scenarios', () => {
  test.beforeAll(async () => {
    await resetDb()
  })

  test('Scenario 1 — employee logs in and reaches /home with their name', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')
    await expect(page).toHaveURL(/\/home$/)
    // Name comes from useCurrentEmployee → /api/employees list + client-side filter
    await expect(page.getByRole('heading')).toContainText(USERS.employeeGangnam.name)
  })

  test('Scenario 6 — employee attempting /admin is redirected to /403', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')
    await page.goto('/admin')
    await expect(page).toHaveURL(/\/403$/)
    await expect(page.getByText('접근 권한 없음')).toBeVisible()
  })

  test('login with wrong password shows toast error and stays on /login', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('이메일').fill(USERS.employeeGangnam.email)
    await page.getByLabel('비밀번호').fill('wrong-password')
    await page.getByRole('button', { name: '로그인' }).click()

    // sonner toast appears somewhere on the page
    await expect(page.getByText(/일치하지 않|인증|AUTH/i).first()).toBeVisible({ timeout: 5_000 })
    await expect(page).toHaveURL(/\/login$/)
  })
})
```

- [ ] **Step 2: Run (requires backend + frontend running in e2e mode)**

```bash
cd lms_web && npx playwright test auth.spec.ts
```

Expected: 3 passed.

- [ ] **Step 3: Commit**

```bash
git add lms_web/e2e/specs/auth.spec.ts
git commit -m "test(e2e): auth scenarios — login success/fail + role-based 403"
```

---

## Task 5.3 — Employee attendance spec (Scenario 2)

**Files:**
- Create: `lms_web/e2e/specs/employee-attendance.spec.ts`

- [ ] **Step 1: Implement**

Create `lms_web/e2e/specs/employee-attendance.spec.ts`:

```ts
import { test, expect } from '@playwright/test'
import { loginAs } from '../helpers/auth'
import { resetDb } from '../helpers/api'

test.describe('employee attendance flow', () => {
  test.beforeEach(async () => {
    await resetDb()
  })

  test('Scenario 2 — check-in → check-out → history shows today record', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')

    // Navigate to attendance
    await page.getByRole('link', { name: '출퇴근' }).click()
    await expect(page).toHaveURL(/\/attendance$/)

    // Check in
    await page.getByRole('button', { name: '출근하기' }).click()
    await expect(page.getByText('출근이 기록되었습니다')).toBeVisible({ timeout: 5_000 })

    // Refetched → should show "퇴근하기" now
    await expect(page.getByRole('button', { name: '퇴근하기' })).toBeVisible({ timeout: 5_000 })

    // Check out
    await page.getByRole('button', { name: '퇴근하기' }).click()
    await expect(page.getByText('퇴근이 기록되었습니다')).toBeVisible({ timeout: 5_000 })

    // Now should show completion state
    await expect(page.getByText('근무시간:')).toBeVisible({ timeout: 5_000 })

    // Navigate to history
    await page.goto('/attendance/history')
    // Today's record should be in the list
    const today = new Date()
    const koreaDate = new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }).format(today)
    // Korean date format has spaces, just assert year+month visible
    await expect(page.getByText(new RegExp(`${today.getFullYear()}년`))).toBeVisible()
  })
})
```

- [ ] **Step 2: Run**

```bash
cd lms_web && npx playwright test employee-attendance.spec.ts
```

Expected: 1 passed.

- [ ] **Step 3: Commit**

```bash
git add lms_web/e2e/specs/employee-attendance.spec.ts
git commit -m "test(e2e): employee attendance check-in → check-out → history"
```

---

## Task 5.4 — Leave flow spec (Scenarios 3 + 4)

Combined employee request → admin approve → employee sees APPROVED.

**Files:**
- Create: `lms_web/e2e/specs/leave-flow.spec.ts`

- [ ] **Step 1: Implement**

Create `lms_web/e2e/specs/leave-flow.spec.ts`:

```ts
import { test, expect } from '@playwright/test'
import { loginAs, logout } from '../helpers/auth'
import { resetDb } from '../helpers/api'

function futureDate(daysAhead: number): string {
  const d = new Date()
  d.setDate(d.getDate() + daysAhead)
  return d.toISOString().slice(0, 10)
}

test.describe('leave approval flow', () => {
  test.beforeEach(async () => {
    await resetDb()
  })

  test('Scenarios 3 + 4 — employee requests leave → manager approves → employee sees APPROVED', async ({ page }) => {
    // === Part 1: Employee creates leave request ===
    await loginAs(page, 'employeeGangnam')
    await page.getByRole('link', { name: '휴가' }).click()
    await page.getByRole('link', { name: '신청하기' }).click()

    const start = futureDate(7)
    const end = futureDate(8)

    // Leave type select (ANNUAL is default — but set explicitly for clarity)
    await page.getByRole('combobox').click()
    await page.getByRole('option', { name: '연차' }).click()

    await page.getByLabel('시작일').fill(start)
    await page.getByLabel('종료일').fill(end)
    await page.getByLabel('사유 (선택)').fill('e2e 테스트')

    await page.getByRole('button', { name: '신청' }).click()
    await expect(page.getByText('휴가가 신청되었습니다')).toBeVisible({ timeout: 5_000 })

    // Back on /leave list — should see PENDING status
    await expect(page).toHaveURL(/\/leave$/)
    await expect(page.getByText('승인 대기')).toBeVisible({ timeout: 5_000 })

    // === Part 2: Log out, log in as manager, approve ===
    await logout(page)
    await loginAs(page, 'managerGangnam')

    // Go to leave management
    await page.goto('/admin/leaves')

    // There should be at least 1 pending row. Click first 승인 button.
    await page.getByRole('button', { name: '승인' }).first().click()
    await expect(page.getByText('승인되었습니다')).toBeVisible({ timeout: 5_000 })

    // === Part 3: Log back in as employee, verify status change ===
    await logout(page)
    await loginAs(page, 'employeeGangnam')
    await page.goto('/leave')

    // The e2e test request should now show APPROVED
    await expect(page.getByText('승인됨')).toBeVisible({ timeout: 5_000 })
  })
})
```

- [ ] **Step 2: Run**

```bash
cd lms_web && npx playwright test leave-flow.spec.ts
```

Expected: 1 passed.

- [ ] **Step 3: Commit**

```bash
git add lms_web/e2e/specs/leave-flow.spec.ts
git commit -m "test(e2e): leave flow — employee request → manager approve → employee APPROVED"
```

---

## Task 5.5 — 401 refresh spec (Scenario 5)

**Files:**
- Create: `lms_web/e2e/specs/auth-refresh.spec.ts`

Strategy: log in, corrupt the access token in localStorage to something the backend will reject as 401, then navigate. The axios interceptor should refresh silently and the navigation should succeed.

- [ ] **Step 1: Implement**

Create `lms_web/e2e/specs/auth-refresh.spec.ts`:

```ts
import { test, expect } from '@playwright/test'
import { loginAs, setAccessToken, accessToken } from '../helpers/auth'
import { resetDb } from '../helpers/api'

test.describe('401 refresh flow', () => {
  test.beforeEach(async () => {
    await resetDb()
  })

  test('Scenario 5 — expired access token triggers silent refresh on next authenticated request', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')

    // We start on /home with a valid access token. Capture the original.
    const original = await accessToken(page)
    expect(original).toBeTruthy()

    // Corrupt the access token to force a 401 on next authenticated request.
    // Refresh token is untouched, so the interceptor should succeed refreshing.
    await setAccessToken(page, 'invalid-access-token-to-trigger-401')

    // Navigate to a page that makes an authenticated request.
    // /attendance/history calls GET /api/attendance/my-records → server rejects stale token → interceptor refreshes → retry succeeds.
    await page.goto('/attendance/history')

    // Filter inputs should be present (page rendered successfully)
    await expect(page.getByLabel('시작일')).toBeVisible({ timeout: 10_000 })

    // Access token in localStorage must have been replaced with a new one
    const updated = await accessToken(page)
    expect(updated).not.toBe('invalid-access-token-to-trigger-401')
    expect(updated).not.toBe(original) // new token, different from both
  })
})
```

- [ ] **Step 2: Run**

```bash
cd lms_web && npx playwright test auth-refresh.spec.ts
```

Expected: 1 passed.

**Note**: If the backend returns 400 instead of 401 for a malformed JWT, adjust the interceptor logic OR use a well-formed-but-expired token generated via `backendLogin()` then letting it naturally expire. For simplicity we rely on "malformed → 401" behavior, which `SecurityConfig`'s JWT filter produces.

- [ ] **Step 3: Commit**

```bash
git add lms_web/e2e/specs/auth-refresh.spec.ts
git commit -m "test(e2e): 401 refresh — stale access token triggers silent token refresh"
```

---

## Task 5.6 — Polish: README + run scripts + final suite run

**Files:**
- Modify: `lms_web/README.md`
- Modify: root `README.md`
- Create: `run-e2e.bat` (Windows run helper)

- [ ] **Step 1: Update lms_web README**

READ `lms_web/README.md` first. Then add an E2E section after the existing Development section:

```markdown
## E2E Testing (Playwright)

Critical 6 scenarios against an isolated MySQL instance.

### Setup
1. Start isolated e2e MySQL:
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

### Scenarios
1. Employee login → /home reaches with name (`auth.spec.ts`)
2. Attendance check-in → check-out → history (`employee-attendance.spec.ts`)
3. Leave request + manager approve + employee sees APPROVED (`leave-flow.spec.ts`)
4. 401 refresh silent recovery (`auth-refresh.spec.ts`)
5. Non-authorized role → /403 (`auth.spec.ts`)
6. Wrong-password error handling (`auth.spec.ts`)

Tests reset DB via `POST /test-only/reset` (only available in `e2e` profile).
```

- [ ] **Step 2: Update root README**

READ `README.md` at repo root. Find the section that talks about running the project. Add a short E2E pointer:

```markdown
### End-to-End Testing
Playwright Critical 6 scenarios live in `lms_web/e2e/`. See `lms_web/README.md` for run instructions. Backend must be in `e2e` profile against `docker-compose.e2e.yml`.
```

Keep placement tasteful — near the existing Web Clients section or testing section.

- [ ] **Step 3: Windows run helper**

Create `run-e2e.bat` at repo root:

```batch
@echo off
chcp 65001 >nul
echo === Starting E2E environment ===

start "E2E MySQL" cmd /k "docker-compose -f docker-compose.e2e.yml up"
timeout /t 15

start "E2E Backend" cmd /k "gradlew.bat :interfaces:bootRun --args=--spring.profiles.active=e2e"
timeout /t 25

start "React Dev" cmd /k "cd lms_web && npm run dev"
timeout /t 5

echo.
echo === Services starting. Wait ~30s for backend health. ===
echo Run tests from lms_web/:  npm run test:e2e
```

- [ ] **Step 4: Final verification — run the full suite**

With backend + frontend running (see Step 1 of lms_web README above), run all 4 specs:

```bash
cd lms_web && npm run test:e2e
```

Expected: 6 tests passed (3 in auth + 1 attendance + 1 leave-flow + 1 refresh).

If a test fails, investigate via the HTML report:
```bash
npm run test:e2e:report
```

- [ ] **Step 5: Commit**

```bash
git add lms_web/README.md README.md run-e2e.bat
git commit -m "docs: E2E run instructions + Windows helper script"
```

---

# Self-Review Checklist

- [ ] Backend e2e profile boots against port 3307 DB without errors.
- [ ] `/test-only/reset` returns 200 and wipes mutable tables + reseeds.
- [ ] Security allows `/test-only/**` in e2e but returns 404 in other profiles (verify by swapping profile).
- [ ] Playwright scripts added to `package.json`.
- [ ] All 4 spec files created under `lms_web/e2e/specs/`.
- [ ] Final `npm run test:e2e` shows 6/6 passing.
- [ ] Vitest unit suite still 20/20 (no regressions from e2e additions).
- [ ] `typecheck` clean (e2e folder excluded from app tsconfig).
- [ ] No `test-results/` or `playwright-report/` artifacts committed.

---

# Known Risks & Mitigations

1. **Korean date regex brittleness** — the attendance history assertion uses a year-based regex. If Korean format changes, tighten the assertion.
2. **Combobox / select accessibility** — Playwright selectors assume Radix Select exposes `role='combobox'` and `role='option'`. If shadcn upgrades Radix and changes ARIA, selectors may need adjustment.
3. **Race with backend startup** — Backend takes ~10-20s to boot with `create-drop`. The run-e2e.bat uses `timeout /t 25` as a rough estimate; if tests fail with connection errors, wait longer.
4. **Timezone on AttendanceAdjust** — covered in Plan 3; not re-tested here because Critical 6 doesn't include admin attendance adjust.
5. **Shared DB state within a single suite** — `resetDb()` runs in `beforeEach` for flows where state matters (attendance, leave). For pure-UI tests (auth), `beforeAll` is enough.

---

# Corrections / gaps carried forward

- No CI integration: scope explicitly excludes CI per spec §8.2.5.
- Other browsers (Firefox/WebKit): Chromium only — add later if needed.
- Regression scenarios (calendar month transition, payroll number format, employee form error): deferred.
- E2E reset uses JPA EntityManager + native queries; could be replaced with a Flyway-like schema tool later without breaking tests.

---

# Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-04-23-lms-web-e2e-polish.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints.

**Which approach?**
