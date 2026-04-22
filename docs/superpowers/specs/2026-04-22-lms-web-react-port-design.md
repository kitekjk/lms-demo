# LMS Web (React) 포팅 설계 문서

- **작성일**: 2026-04-22
- **대상**: `lms-demo` 프로젝트 — 기존 Flutter 웹(`lms_mobile_web/`)을 React 기반으로 포팅
- **결과물 디렉토리**: `lms_web/` (프로젝트 루트 하위, Flutter 프로젝트와 공존)
- **목적**: 시연 품질의 React 웹앱 구축. 기존 Flutter web은 모바일 채널 전용으로 용도 재정의하여 유지.

---

## 1. 목표와 범위

### 1.1 목표

- 기존 Flutter 웹의 **전 18개 화면**(직원 8 + 관리자 10)을 React로 포팅하여 `lms_web/`에 배치한다.
- 백엔드(Spring Boot + Kotlin) REST API는 변경 없이 그대로 사용한다. 단, E2E 테스트용 `/test-only/reset` 엔드포인트(프로파일 가드)는 신규 추가한다.
- 직원은 모바일 스타일 UX, 관리자는 데스크탑 스타일 UX를 제공하는 **2-Shell 레이아웃**을 구축한다.
- Playwright 기반 **E2E(Critical 6 시나리오)** 를 격리된 DB 환경에서 실행 가능하게 한다.

### 1.2 범위 내

- React 19 + TypeScript + Vite 기반 SPA 구축
- shadcn/ui + Tailwind CSS 디자인 시스템
- TanStack Query + Zustand 상태/서버 상태 분리 관리
- React Router v6 라우팅 + Role 기반 Protected Route
- axios + Interceptor를 통한 JWT 자동 주입과 401 refresh 플로우
- React Hook Form + Zod 기반 폼·검증
- 한국어 UI 하드코딩 (Flutter 현 상태와 동일)
- Vitest + React Testing Library 유닛/컴포넌트 테스트 (핵심 경로)
- Playwright E2E 6개 Critical 시나리오 + 격리된 `docker-compose.e2e.yml` + e2e 프로파일 백엔드

### 1.3 범위 외 (명시적 제외)

- 프로덕션 배포(Nginx, CI/CD 파이프라인)
- 국제화(i18n) 및 다국어
- 다크모드 토글 UI
- E2E CI 자동화 (로컬 실행만 지원)
- Storybook
- React.memo/virtualization 등 성능 튜닝
- 접근성(A11y) 전면 감사
- Flutter web 삭제 (모바일 채널 전용으로 유지)

---

## 2. 기술 스택

| 항목 | 선택 | 비고 |
|------|------|------|
| 빌드 도구 | Vite | SPA, 빠른 dev server |
| 언어 | TypeScript (strict) | |
| UI 라이브러리 | shadcn/ui + Tailwind CSS | Radix UI 기반, 프로젝트 내 복사본 |
| 아이콘 | lucide-react | shadcn 기본 |
| 상태 관리 (client) | Zustand + persist 미들웨어 | 글로벌 상태 최소화(`auth`, `uiPrefs`) |
| 상태 관리 (server) | TanStack Query v5 | 쿼리키 팩토리 패턴 |
| 라우팅 | react-router-dom v6 (`createBrowserRouter`) | 중첩 라우트로 Shell 적용 |
| HTTP | axios + interceptor | JWT 자동 주입, 401 refresh |
| 폼 | react-hook-form + @hookform/resolvers | shadcn `<Form>` 통합 |
| 검증 | zod | 런타임 검증 + 타입 추출 |
| 날짜 | date-fns, react-day-picker | shadcn Calendar 기본 |
| 알림 | sonner (shadcn 권장) | 전역 토스트 |
| 유닛 테스트 | Vitest + jsdom | |
| 컴포넌트 테스트 | @testing-library/react | |
| API 모킹 | MSW (Mock Service Worker) | 필요 시 |
| E2E | Playwright | 격리 DB 환경 |

---

## 3. 아키텍처와 디렉토리 구조

### 3.1 최상위 레이아웃 (프로젝트 루트)

```
lms-demo/
├── lms_mobile_web/                # Flutter (모바일 전용, 유지)
├── lms_web/                       # ✨ NEW — React 웹
├── infrastructure/ application/ domain/ interfaces/   # Backend (기존)
├── docker-compose.yml             # 기존 MySQL (local 프로파일)
└── docker-compose.e2e.yml         # ✨ NEW — E2E용 격리 MySQL
```

### 3.2 `lms_web/` 내부 구조

```
lms_web/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.ts
├── components.json                # shadcn/ui 설정
├── .env                           # VITE_API_BASE_URL 등
├── .env.example
├── e2e/                           # Playwright (섹션 8 참조)
└── src/
    ├── main.tsx
    ├── App.tsx                    # QueryClientProvider + RouterProvider + Toaster
    ├── lib/
    │   ├── api/
    │   │   ├── client.ts          # axios + interceptor
    │   │   ├── endpoints.ts       # 경로 상수
    │   │   └── types.ts           # ApiResponse<T>, ApiError 등
    │   ├── auth/
    │   │   ├── token-storage.ts   # localStorage 래퍼
    │   │   └── use-auth.ts
    │   ├── router/
    │   │   ├── routes.tsx
    │   │   └── protected-route.tsx
    │   └── utils/                 # formatters, date helpers
    ├── components/
    │   ├── ui/                    # shadcn/ui 복사본
    │   └── layout/
    │       ├── EmployeeShell.tsx  # 상단 Header + 하단 TabBar
    │       ├── AdminShell.tsx     # 좌측 Sidebar + 상단 Header
    │       └── QueryBoundary.tsx  # 로딩/에러/빈상태 공용
    ├── features/                  # 🎯 Flutter features 미러링
    │   ├── auth/
    │   ├── attendance/
    │   ├── schedule/
    │   ├── leave/
    │   ├── payroll/
    │   ├── home/
    │   └── admin/
    └── test/
        └── setup.ts               # Vitest 설정
```

각 feature 폴더는 `api.ts | store.ts | schema.ts | pages/` 표준 구성.

### 3.3 핵심 설계 원칙

1. **Feature 단위 응집**: 기능 1개 = 폴더 1개. Flutter 구조와 1:1 매핑.
2. **레이어 분리**: `lib/`(횡단 관심사) vs `features/`(도메인) vs `components/`(재사용 UI).
3. **타입 안전 경계**: `schema.ts`(Zod)로 런타임 검증 + TS 타입 자동 추출.
4. **파일 500줄 제한**: Flutter 가이드라인과 동일. 큰 페이지는 `components/`로 분해.

---

## 4. 인증 플로우 & API 통합

### 4.1 토큰 저장

- `lib/auth/token-storage.ts`: `localStorage` 키 `lms.access_token`, `lms.refresh_token` 관리.
- 사용자 프로필(`currentUser`)은 Zustand `persist` 미들웨어로 별도 키(`lms.auth`)에 저장.
- 토큰과 사용자 정보는 관심사 분리 (토큰은 interceptor에서만, 사용자는 UI에서 사용).

### 4.2 axios Interceptor 동작

**요청 인터셉터**
- `accessToken` 존재 시 → `Authorization: Bearer <token>` 주입.

**응답 인터셉터 (401 처리)**
1. `status=401` & 해당 요청이 refresh 시도 이력 없음.
2. `POST /auth/refresh` 호출.
3. 성공 → 새 토큰 저장 → 원 요청 재시도.
4. 실패 → `tokenStorage.clear()` → `/login` 리다이렉트.
5. **동시 401 레이스 방지**: 진행 중인 refresh Promise를 싱글톤으로 공유(모든 병렬 401 요청이 동일 Promise를 await).

### 4.3 앱 부트스트랩

```
App mount
 ├─ 토큰 없음        → isInitialized=true → /login
 └─ 토큰 있음        → useMe() 실행
     ├─ 200        → currentUser 저장 → 원래 경로 진입
     ├─ 401        → interceptor가 refresh 시도
     │   ├─ 성공  → 재시도
     │   └─ 실패  → clear → /login
     └─ 기타 에러  → 에러 페이지
```

### 4.4 Role 기반 Protected Route

- `<ProtectedRoute roles={['MANAGER','SUPER_ADMIN']}>` 래퍼.
- 역할 계층: `EMPLOYEE` ⊂ `MANAGER` ⊂ `SUPER_ADMIN`.
- 비인증 → `/login`, 인증됐으나 권한 없음 → `/403`.

### 4.5 백엔드 CORS

- **1차 전략**: Vite dev proxy로 `/api/*` → `http://localhost:8080`. 브라우저는 동일 오리진으로 인식, CORS 수정 불필요.
- **2차 전략**: 직접 호출 필요 시 Spring Boot CORS에 `http://localhost:5173` origin 추가 (allowCredentials=true, 와일드카드 금지).
- 구현 시 현 백엔드 CORS 설정(`WebMvcConfigurer`)을 먼저 확인 후 선택.

---

## 5. 라우팅 & 레이아웃

### 5.1 라우트 맵 (18 페이지 + 공용 4)

```
공용
/                     → 토큰 유무로 /home 또는 /login 리다이렉트
/login                → LoginPage
/403                  → ForbiddenPage
*                     → NotFoundPage

직원 영역 (role ≥ EMPLOYEE, <EmployeeShell>)
/home                 → HomePage
/attendance           → AttendancePage
/attendance/history   → AttendanceHistoryPage
/schedule             → SchedulePage
/leave                → LeaveListPage
/leave/request        → LeaveRequestPage
/payroll              → PayrollListPage
/payroll/:id          → PayrollDetailPage

관리자 영역 (role ∈ {MANAGER, SUPER_ADMIN}, <AdminShell>)
/admin                → AdminDashboardPage
/admin/employees      → EmployeeManagementPage
/admin/stores         → StoreManagementPage           (SUPER_ADMIN)
/admin/schedules      → ScheduleManagementPage
/admin/attendance     → AttendanceManagementPage
/admin/leaves         → LeaveManagementPage
/admin/payroll        → PayrollManagementPage
/admin/payroll/policies → PayrollPolicyPage           (SUPER_ADMIN)
```

### 5.2 2-Shell 레이아웃

- **EmployeeShell**: 상단 Header (로고, 사용자, 로그아웃) + 하단 TabBar (홈/출퇴근/일정/휴가/급여). 모바일 우선, 데스크탑에선 max-width 컨테이너로 중앙정렬.
- **AdminShell**: 좌측 Sidebar (전 관리 메뉴) + 상단 Header (Breadcrumb, 사용자). 데스크탑 우선, `md` 미만에선 Sidebar → Drawer 토글.

두 Shell 모두 동일 shadcn/ui 컴포넌트 기반 — UI 토큰은 공유, 네비게이션 패턴만 상이.

### 5.3 라우트 정의 방식

React Router v6 `createBrowserRouter` + 객체 스타일 + 중첩 라우트로 Shell 적용.

```tsx
createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <ProtectedRoute><EmployeeShell /></ProtectedRoute>,
    children: [ { path: '/home', element: <HomePage /> }, ... ],
  },
  {
    element: <ProtectedRoute roles={['MANAGER','SUPER_ADMIN']}><AdminShell /></ProtectedRoute>,
    children: [ { path: '/admin', element: <AdminDashboardPage /> }, ... ],
  },
])
```

---

## 6. 상태 관리 & API 레이어

### 6.1 책임 분리

| 상태 종류 | 보관 위치 | 예 |
|-----------|----------|-----|
| 서버 데이터 | TanStack Query | 휴가 목록, 근태 기록 |
| 전역 UI/인증 | Zustand | currentUser, sidebar toggle |
| 폼 로컬 | react-hook-form | 휴가 신청 입력값 |
| 공유 가능 (URL) | router searchParams | 필터, 페이지 번호 |

판단 기준: **새로고침하면 사라져도 괜찮은가?**

### 6.2 쿼리키 팩토리 패턴

```ts
export const leaveKeys = {
  all: ['leave'] as const,
  myList: () => [...leaveKeys.all, 'my'] as const,
  detail: (id: string) => [...leaveKeys.all, 'detail', id] as const,
  adminList: (filters: LeaveFilters) => [...leaveKeys.all, 'admin', filters] as const,
}
```

각 feature의 `api.ts`에 쿼리키 + TanStack Query 훅(`useLeaves`, `useCreateLeaveRequest` 등)을 모은다.

### 6.3 QueryClient 전역 설정

```ts
{
  queries: {
    staleTime: 30_000,
    retry: (count, err) => err?.status === 401 || err?.status === 403 ? false : count < 2,
    refetchOnWindowFocus: false,
  },
  mutations: {
    onError: (err) => toast.error(getErrorMessage(err)),
  },
}
```

### 6.4 에러 처리 파이프라인

```
Backend DomainException  → @RestControllerAdvice → { code, message, timestamp } envelope
axios interceptor        → 정규화된 ApiError 객체로 변환
 ├─ 401 → refresh 자동
 ├─ 403 → /403 리다이렉트
 └─ 나머지 → 컴포넌트 수준 처리
              ├─ mutation → 전역 onError(토스트)
              ├─ query    → error 상태 UI
              └─ form     → setError('root', ...)
```

### 6.5 `<QueryBoundary>` 공용 컴포넌트

```tsx
<QueryBoundary query={schedulesQuery} emptyMessage="근무 일정이 없습니다">
  {(data) => <ScheduleList schedules={data} />}
</QueryBoundary>
```

내부에서 `isLoading` → Skeleton, `isError` → ErrorAlert + retry, 데이터 있음 → children(data).

### 6.6 폼 표준 패턴

**RHF(상태) + Zod(검증) + shadcn `<Form>`(렌더)** 3종 세트를 모든 폼에서 동일하게 사용.

```tsx
const schema = z.object({
  startDate: z.date(),
  endDate: z.date(),
  reason: z.string().min(1, '사유를 입력해주세요').max(500),
}).refine(v => v.endDate >= v.startDate, { message: '종료일은 시작일 이후', path: ['endDate'] })
```

---

## 7. 개발 워크플로우

### 7.1 로컬 실행 (3 프로세스)

```
T1: docker-compose up -d                        # MySQL :3306
T2: ./gradlew :interfaces:bootRun               # Spring Boot :8080 (profile=local)
T3: cd lms_web && npm run dev                   # Vite :5173
```

필요 시 T4: `cd lms_mobile_web && flutter run -d chrome --web-port=8081`.

선택 편의: `run-demo.bat` (Windows 다중 터미널 기동).

### 7.2 Vite 설정 (proxy)

```ts
server: {
  port: 5173,
  proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } },
}
```

### 7.3 환경 변수

`.env.example`:
```
VITE_API_BASE_URL=/api
VITE_APP_ENV=local
```

### 7.4 package.json 스크립트

```
dev | build | preview | lint | format | test | test:ui | test:e2e | typecheck
```

### 7.5 Git 전략

- `lms_web/` 전체를 저장소에 포함.
- `.gitignore`에 `lms_web/node_modules`, `lms_web/dist`, `lms_web/.env` 추가.
- 초기 커밋 단위는 마일스톤 기준(M0, M1, ...).

---

## 8. 테스트 전략

### 8.1 유닛/컴포넌트 (Vitest + RTL)

**우선순위**:
1. 인증 플로우 (로그인 성공/실패, 401 refresh, 로그아웃)
2. 폼 Zod 스키마 (휴가 신청, 직원 등록 등)
3. `ProtectedRoute` 역할 체크
4. 핵심 mutation 훅 (출퇴근 체크, 휴가 신청/승인)
5. 나머지는 스모크 수준

구조: Given-When-Then, 엣지 케이스 포함 (Flutter 가이드라인과 동일).

### 8.2 E2E (Playwright, 격리 DB 환경)

#### 8.2.1 환경 구성

```
docker-compose.e2e.yml
 └─ mysql:  포트 3307, DB lms_e2e

application-e2e.yml (backend)
 ├─ datasource URL → jdbc:mysql://localhost:3307/lms_e2e
 └─ jpa.hibernate.ddl-auto: create-drop  (기존 local 프로파일과 동일 전략)

E2eResetController (@Profile("e2e"))
 └─ POST /test-only/reset : 테이블 TRUNCATE + 시드 재주입
    (부팅 시점엔 ddl-auto가 스키마 생성, 각 스위트 시작 시 데이터만 리셋)
```

**중요**: `@Profile("e2e")` 가드로 로컬/프로덕션 프로파일에 절대 노출되지 않도록 격리. 실수로라도 dev DB를 날리지 않게 한다.

#### 8.2.2 테스트 격리 전략

- 스위트(describe) 시작 시 `/test-only/reset` 호출 → 동일한 시드 상태로 복원.
- 테스트 내에서는 고유 식별자(타임스탬프/UUID)로 데이터 생성해 충돌 회피.

#### 8.2.3 디렉토리

```
lms_web/e2e/
├── fixtures/
│   ├── users.ts            # employee / manager / admin 테스트 계정
│   └── seed-data.ts
├── helpers/
│   ├── auth.ts             # loginAs(role)
│   └── api.ts              # resetDb()
├── specs/
│   ├── auth.spec.ts
│   ├── employee/
│   │   ├── attendance.spec.ts
│   │   ├── schedule.spec.ts
│   │   ├── leave.spec.ts
│   │   └── payroll.spec.ts
│   └── admin/
│       ├── employee-mgmt.spec.ts
│       ├── schedule-mgmt.spec.ts
│       └── leave-approval.spec.ts
└── playwright.config.ts
```

#### 8.2.4 Critical 6 시나리오 (필수)

1. 직원 로그인 → /home 도달 (Smoke 포함).
2. 직원: 출근 → 퇴근 → 이력에 오늘 기록 노출.
3. 직원: 휴가 신청 → 내 목록에 '대기' 상태 확인.
4. 관리자: 대기 휴가 승인 → 직원 뷰에서 '승인' 확인.
5. 401 만료 → 자동 refresh → 원 요청 성공 복구.
6. 무권한 접근 → `/403` 이동.

Regression 시나리오(달력 월 전환, 급여 숫자 포맷, 직원 등록 폼 검증 에러 등)는 여유 시.

#### 8.2.5 CI 통합

- **이번 범위 외**. 로컬 `npm run test:e2e` 실행만 지원.
- 후속 작업으로 GitHub Actions 도입 시 별도 설계.

---

## 9. 백엔드 변경 사항 (최소)

1. **(조건부)** `WebMvcConfigurer` CORS에 `http://localhost:5173` origin 추가 — Vite proxy만으로 해결 안 될 경우.
2. **E2E 전용 프로파일 `e2e`** 추가.
   - `application-e2e.yml`: 데이터소스 URL `jdbc:mysql://localhost:3307/lms_e2e`, `jpa.hibernate.ddl-auto: create-drop` (기존 local 프로파일 전략 재사용).
   - `E2eResetController` (`@Profile("e2e")`) — `POST /test-only/reset` 제공 (테이블 TRUNCATE + 시드 재주입).
3. `docker-compose.e2e.yml` 루트에 추가 (별도 MySQL 컨테이너).

기존 도메인/애플리케이션/인터페이스 레이어 로직은 변경 없음.

---

## 10. 구현 마일스톤

| ID | 내용 | 예상 공수 |
|----|------|----------|
| M0 | Bootstrap: `lms_web/` 생성, Vite+React+TS+Tailwind+shadcn init, 디렉토리 구조, Hello World | 1–2h |
| M1 | 인증 기반: axios+interceptor, tokenStorage, useAuthStore, LoginPage, ProtectedRoute, useMe 부트스트랩 | 2–3h |
| M2 | 라우팅·레이아웃: 18 라우트 정의(placeholder), EmployeeShell/AdminShell, QueryBoundary, Skeleton | 1–2h |
| M3 | 직원 기능: Home, Attendance(2), Schedule, Leave(2), Payroll(2) 전 페이지 구현 | 4–6h |
| M4 | 관리자 기능: Dashboard, Employee/Store/Schedule/Attendance/Leave/Payroll/PayrollPolicy 관리 8 페이지 | 5–7h |
| M5 | 마감: 유닛·컴포넌트 핵심 테스트, README 업데이트, `run-demo.bat`, 빌드 확인 | 1–2h |
| M6 | E2E: Playwright 설치, `docker-compose.e2e.yml`, `application-e2e.yml`, `E2eResetController`, Critical 6 작성 | 3–5h |

**총 예상 공수**: 17–27시간 (복잡 페이지 깊이에 따라 변동).

---

## 11. 리스크 & 선행 확인 사항

1. **백엔드 API 응답 envelope 형태** — Flutter가 사용하는 `ApiResponse<T>` 래퍼 여부 확인 후 axios response 변환 설계에 반영.
2. **백엔드 CORS 현재 설정** — 기존이 `localhost:*` 허용하는지. M0 precheck에서 확인.
3. **달력 컴포넌트 복잡도** — Flutter `table_calendar`의 커스터마이징 수준에 따라 shadcn Calendar 재현 비용 증감.
4. **Payroll 계산 UI** — 일괄 처리가 장시간 작업이면 progress 표시 전략 필요(폴링 vs 짧은 요청 반복).

→ M0 직후 30분 수준의 precheck로 4개 모두 판단 가능.

---

## 12. 의사결정 로그

| # | 결정 | 선택 | 근거 |
|---|------|------|------|
| 1 | 범위 | 전체 포팅 (18 페이지) | 사용자 지정 |
| 2 | 빌드 도구 | Vite + React SPA | 사내용, SEO 불필요, dev server 가볍고 빠름 |
| 3 | UI 라이브러리 | shadcn/ui + Tailwind | 모던, 접근성, 커스터마이징 자유 |
| 4 | 상태 관리 | TanStack Query + Zustand | 서버/클라이언트 상태 분리, 보일러플레이트 최소 |
| 5 | Flutter web 공존 | 유지 (모바일 전용으로 용도 변경) | 기존 작업 보존, 데모 채널 다양화 |
| 6 | 토큰 저장 | localStorage | 데모/내부용, UX 우선 (XSS 위험은 수용) |
| 7 | 라우팅 | React Router v6 `createBrowserRouter` | 중첩 라우트로 Shell 적용 자연스러움 |
| 8 | HTTP | axios + interceptor | Flutter Dio 패턴과 1:1 대응 |
| 9 | 폼 | RHF + Zod + shadcn Form | 생태계 표준 |
| 10 | i18n | 한국어 하드코딩 | Flutter 현 상태와 동일 |
| 11 | Dev 서버 포트 | 5173 (Vite 기본) | Flutter web :8081과 충돌 없음 |
| 12 | CORS 해결 | Vite proxy (1차) / CORS origin 추가 (2차) | 개발 편의, prod는 reverse proxy 전제 |
| 13 | 레이아웃 | 2-Shell (Employee 모바일 / Admin 데스크탑) | 사용자 유형별 최적 UX |
| 14 | E2E 도구 | Playwright | 다중 브라우저, codegen, trace viewer |
| 15 | E2E 격리 | 실제 백엔드 + 격리 DB (:3307 / lms_e2e) | 진짜 E2E 보장, 계약 검증 |
| 16 | E2E 범위 | Critical 6 우선 (Smoke 포함, Regression 후순위) | 리소스 대비 가치 최대화 |
| 17 | CI 통합 | 범위 외, 로컬 실행만 | 후속 작업으로 분리 |

---

## 13. 성공 기준

- [ ] `lms_web/`에서 `npm run dev` 실행 시 로그인 → 직원/관리자 전 18 페이지가 에러 없이 렌더.
- [ ] Flutter web과 동일한 백엔드 API로 동작 (데이터 불일치 없음).
- [ ] 직원/관리자 역할 가드가 모든 관리자 라우트에서 정상 작동.
- [ ] 401 만료 시 사용자 체감 끊김 없이 refresh 후 재시도.
- [ ] Playwright Critical 6 시나리오 전부 통과.
- [ ] `npm run build` 성공, 번들 사이즈 점검(극단적 비대화 없음).
- [ ] 기존 Flutter web(`lms_mobile_web/`) 동작에 영향 없음.
