# LMS Web Employee Features Implementation Plan (Plan 2 of 4)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all 8 employee-facing pages (Home dashboard, Attendance check-in/history, Schedule calendar, Leave list/request, Payroll list/detail) wired to the real Spring Boot backend. After this plan, an EMPLOYEE account can fully use the React web app for their own self-service workflows.

**Architecture:** Build on the foundation from Plan 1 — each feature gets its own `features/<name>/{api.ts,schema.ts,pages/*.tsx}` module following the patterns established in `features/auth/`. Use TanStack Query hooks with typed query keys, Zod schemas for form validation, shadcn/ui primitives for rendering, and the `<QueryBoundary>` helper for loading/error/empty states. API calls target the RAW (non-`ApiResponse`-wrapped) employee endpoints.

**Tech Stack (already installed):** React 19, TypeScript 6, Vite 8, Tailwind v3, shadcn/ui, TanStack Query v5, Zustand v5, react-router-dom v6, axios (with 401 refresh), react-hook-form, zod, date-fns, lucide-react, sonner, react-day-picker (installed with shadcn but unused so far — will install `@/components/ui/calendar` on-demand).

**Prior plan:** `docs/superpowers/plans/2026-04-22-lms-web-foundation.md` (merged in commit `fa185b9`).

---

## Source of Truth: Backend Contracts (verified 2026-04-23)

**ALL employee endpoints return RAW DTOs (no `ApiResponse<T>` wrapper).** Error responses from the `GlobalExceptionHandler` ARE wrapped in `ApiResponse<Unit>`. The axios `normalizeError` helper from Plan 1 already handles both shapes.

`userId` is extracted from the JWT server-side — never send it in the request body.

### Attendance — `/api/attendance`

| Method | Path | Request | 200 Response |
|--------|------|---------|--------------|
| POST | `/check-in` | `{ workScheduleId?: string }` | `AttendanceRecordResponse` |
| POST | `/check-out` | `{ note?: string }` | `AttendanceRecordResponse` |
| GET | `/my-records?startDate=&endDate=` | — | `{ records: AttendanceRecordResponse[] }` |

`AttendanceRecordResponse`:
```
{ id: string, employeeId: string, workScheduleId: string | null,
  attendanceDate: 'YYYY-MM-DD', checkInTime: 'HH:mm:ss', checkOutTime: 'HH:mm:ss' | null,
  actualWorkHours: number | null,
  status: 'NORMAL'|'LATE'|'EARLY_LEAVE'|'ABSENT'|'PENDING',
  note: string | null, createdAt: ISO-datetime }
```

### Schedule — `/api/schedules`

| Method | Path | Request | 200 Response |
|--------|------|---------|--------------|
| GET | `/my-schedule?startDate=&endDate=` | — | `{ schedules: WorkScheduleResponse[] }` |
| GET | `/{scheduleId}` | — | `WorkScheduleResponse` |

`WorkScheduleResponse`:
```
{ id: string, employeeId: string, storeId: string,
  workDate: 'YYYY-MM-DD', startTime: 'HH:mm:ss', endTime: 'HH:mm:ss',
  workHours: number, isConfirmed: boolean, isWeekendWork: boolean,
  createdAt: ISO-datetime }
```

### Leave — `/api/leaves`

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/` | `{ leaveType, startDate, endDate, reason? }` | `LeaveRequestResponse` |
| GET | `/my-leaves` | — | `{ requests: LeaveRequestResponse[] }` |
| DELETE | `/{leaveId}` | — | 204 (no content) |

`LeaveRequestResponse`:
```
{ id: string, employeeId: string,
  leaveType: 'ANNUAL'|'SICK'|'PERSONAL'|'MATERNITY'|'PATERNITY'|'BEREAVEMENT'|'UNPAID',
  startDate: 'YYYY-MM-DD', endDate: 'YYYY-MM-DD', requestedDays: number,
  reason: string | null,
  status: 'PENDING'|'APPROVED'|'REJECTED'|'CANCELLED',
  rejectionReason: string | null, approvedBy: string | null, approvedAt: ISO-datetime | null,
  createdAt: ISO-datetime }
```

### Payroll — `/api/payroll`

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/my-payroll` | — | `PayrollResponse[]` (plain array) |
| GET | `/{payrollId}` | — | `PayrollWithDetailsResponse` |

`PayrollResponse`:
```
{ id: string, employeeId: string, period: 'YYYY-MM',
  baseAmount: number, overtimeAmount: number, totalAmount: number,
  isPaid: boolean, paidAt: ISO-datetime | null,
  calculatedAt: ISO-datetime, createdAt: ISO-datetime }
```

`PayrollWithDetailsResponse`:
```
{ payroll: PayrollResponse,
  details: PayrollDetailResponse[] }
```

`PayrollDetailResponse`:
```
{ id: string, payrollId: string, workDate: 'YYYY-MM-DD',
  workType: string, hours: number, hourlyRate: number,
  multiplier: number, amount: number }
```

### Employee — `/api/employees/{employeeId}`

Used by HomePage to display employee name / remaining leave:
`EmployeeResponse`:
```
{ id: string, userId: string, name: string, employeeType: string,
  storeId: string | null, remainingLeave: number,
  isActive: boolean, createdAt: ISO-datetime }
```

**How to find the current employee's ID:** `AuthUser.userId` (already in Zustand `useAuthStore`) maps to `EmployeeResponse.userId`, but there's no direct "me" endpoint. Solution: call `GET /api/employees?userId={currentUser.userId}` is not a supported filter; instead, call `GET /api/employees/{id}` where `{id}` equals `employeeId`. Since the LoginResponse only returns `userId` (not `employeeId`), the HomePage will call the **list endpoint** `GET /api/employees?activeOnly=true` and find the one matching `userInfo.userId`. This is ugly but matches backend reality. If the list is too large for this filter, we'll revisit in a later plan.

Simpler alternative used in this plan: query `GET /api/employees?storeId=&activeOnly=true` is not feasible for employees whose `storeId` is null. We'll just query the unfiltered employee list and filter client-side by `userId`. The employee list is small in the demo (~10s of rows).

---

## File Structure — new files created in this plan

```
lms_web/src/
├── lib/
│   └── utils/
│       ├── date.ts                          # formatDate, formatTime, parseDate helpers
│       ├── number.ts                        # formatKRW currency formatter
│       └── labels.ts                        # Korean labels for enum values
├── features/
│   ├── attendance/
│   │   ├── api.ts                           # useCheckIn, useCheckOut, useMyAttendance
│   │   ├── schema.ts                        # Zod: CheckInInput, CheckOutInput, DateRange
│   │   ├── types.ts                         # AttendanceRecord, AttendanceStatus typing
│   │   └── pages/
│   │       ├── AttendancePage.tsx           # check-in/out action buttons + today status
│   │       └── AttendanceHistoryPage.tsx    # list with date range filter
│   ├── schedule/
│   │   ├── api.ts                           # useMySchedule, useScheduleById
│   │   ├── types.ts                         # WorkSchedule typing
│   │   └── pages/
│   │       └── SchedulePage.tsx             # calendar view + daily detail
│   ├── leave/
│   │   ├── api.ts                           # useMyLeaves, useCreateLeave, useCancelLeave
│   │   ├── schema.ts                        # Zod: LeaveRequestInput
│   │   ├── types.ts                         # LeaveRequest, LeaveType, LeaveStatus
│   │   └── pages/
│   │       ├── LeaveListPage.tsx
│   │       └── LeaveRequestPage.tsx
│   ├── payroll/
│   │   ├── api.ts                           # useMyPayrolls, usePayrollDetail
│   │   ├── types.ts                         # Payroll, PayrollDetail
│   │   └── pages/
│   │       ├── PayrollListPage.tsx
│   │       └── PayrollDetailPage.tsx
│   └── home/
│       ├── api.ts                           # useCurrentEmployee (list + filter by userId)
│       └── pages/
│           └── HomePage.tsx                 # aggregates today's schedule + recent attendance + employee profile
└── components/
    └── ui/
        └── calendar.tsx                     # shadcn calendar (installed on-demand in Task 3.4)
```

**Existing files modified:**
- `lms_web/src/lib/api/endpoints.ts` — add `attendance`, `schedules`, `leaves`, `payroll`, `employees` path constants
- `lms_web/src/features/*/pages/*.tsx` — 8 files replaced (were placeholders)

**Files by responsibility:**
- `api.ts` — all TanStack Query hooks for one feature, plus query-key factory
- `schema.ts` — Zod validation schemas
- `types.ts` — domain response types (from the API contract section above)
- `pages/*.tsx` — route-level components that compose hooks with UI

---

## Pattern Reference — Standard Feature Module Template

Every feature module follows this skeleton. Referenced by Task 3.1 onwards.

### `api.ts` template

```ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'

export const featureKeys = {
  all: ['feature'] as const,
  list: (filters: object) => [...featureKeys.all, 'list', filters] as const,
  detail: (id: string) => [...featureKeys.all, 'detail', id] as const,
}

export function useSomeList(filters: SomeFilters) {
  return useQuery({
    queryKey: featureKeys.list(filters),
    queryFn: async () => {
      const res = await api.get<SomeListResponse>(endpoints.x, { params: filters })
      return res.data
    },
  })
}

export function useSomeMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: SomeInput) => {
      const res = await api.post<SomeResponse>(endpoints.x, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: featureKeys.all }),
  })
}
```

### Page template

```tsx
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useSomeList } from '../api'

export default function SomePage() {
  const query = useSomeList({ /* filters */ })
  return (
    <QueryBoundary query={query} emptyMessage="데이터가 없습니다" isEmpty={(d) => d.items.length === 0}>
      {(data) => (
        <div className="space-y-3">
          {/* render data */}
        </div>
      )}
    </QueryBoundary>
  )
}
```

Use this template for tasks that don't spell out the full component code.

---

# Task 3.0 — Shared utilities (date/number formatters + Korean enum labels)

**Files:**
- Create: `lms_web/src/lib/utils/date.ts`
- Create: `lms_web/src/lib/utils/number.ts`
- Create: `lms_web/src/lib/utils/labels.ts`
- Test: `lms_web/src/lib/utils/date.test.ts`
- Test: `lms_web/src/lib/utils/number.test.ts`
- Modify: `lms_web/src/lib/api/endpoints.ts`

- [ ] **Step 1: Write failing tests for date helpers**

Create `lms_web/src/lib/utils/date.test.ts`:

```ts
import { describe, it, expect } from 'vitest'
import { formatDate, formatTime, parseIsoTime, toDateString } from './date'

describe('date helpers', () => {
  it('formatDate renders a Date as YYYY-MM-DD', () => {
    const d = new Date('2026-04-23T15:30:00Z')
    expect(formatDate(d)).toBe('2026-04-23')
  })

  it('toDateString handles string input unchanged', () => {
    expect(toDateString('2026-04-23')).toBe('2026-04-23')
  })

  it('formatTime renders HH:mm from HH:mm:ss', () => {
    expect(formatTime('09:00:00')).toBe('09:00')
    expect(formatTime('18:30:45')).toBe('18:30')
    expect(formatTime(null)).toBe('—')
  })

  it('parseIsoTime returns Date on today from HH:mm:ss', () => {
    const d = parseIsoTime('09:15:30')
    expect(d.getHours()).toBe(9)
    expect(d.getMinutes()).toBe(15)
  })
})
```

- [ ] **Step 2: Run — expect FAIL (module not found)**

```bash
cd lms_web && npx vitest run src/lib/utils/date.test.ts
```

- [ ] **Step 3: Implement date helpers**

Create `lms_web/src/lib/utils/date.ts`:

```ts
import { format, parse } from 'date-fns'

export function formatDate(d: Date): string {
  return format(d, 'yyyy-MM-dd')
}

export function toDateString(input: Date | string): string {
  return typeof input === 'string' ? input : formatDate(input)
}

export function formatTime(hms: string | null | undefined): string {
  if (!hms) return '—'
  return hms.slice(0, 5) // 'HH:mm:ss' → 'HH:mm'
}

export function parseIsoTime(hms: string): Date {
  return parse(hms, 'HH:mm:ss', new Date())
}

export function formatDateKorean(d: Date | string): string {
  const date = typeof d === 'string' ? parse(d, 'yyyy-MM-dd', new Date()) : d
  return format(date, 'yyyy년 M월 d일 (EEE)')
}
```

- [ ] **Step 4: Run — expect 4 PASS**

```bash
npx vitest run src/lib/utils/date.test.ts
```

- [ ] **Step 5: Write failing tests for number formatter**

Create `lms_web/src/lib/utils/number.test.ts`:

```ts
import { describe, it, expect } from 'vitest'
import { formatKRW, formatHours } from './number'

describe('number helpers', () => {
  it('formatKRW renders integers with ₩ and commas', () => {
    expect(formatKRW(1_234_567)).toBe('₩1,234,567')
    expect(formatKRW(0)).toBe('₩0')
  })

  it('formatHours renders with 1 decimal', () => {
    expect(formatHours(8)).toBe('8.0h')
    expect(formatHours(8.5)).toBe('8.5h')
    expect(formatHours(null)).toBe('—')
  })
})
```

- [ ] **Step 6: Implement number helpers**

Create `lms_web/src/lib/utils/number.ts`:

```ts
export function formatKRW(amount: number): string {
  return '₩' + amount.toLocaleString('ko-KR')
}

export function formatHours(h: number | null | undefined): string {
  if (h === null || h === undefined) return '—'
  return h.toFixed(1) + 'h'
}
```

- [ ] **Step 7: Run both utility tests — expect 6 PASS combined**

```bash
npx vitest run src/lib/utils/date.test.ts src/lib/utils/number.test.ts
```

- [ ] **Step 8: Implement Korean enum labels (no tests — static map)**

Create `lms_web/src/lib/utils/labels.ts`:

```ts
export const leaveTypeLabels: Record<string, string> = {
  ANNUAL: '연차',
  SICK: '병가',
  PERSONAL: '개인 사유',
  MATERNITY: '출산 휴가',
  PATERNITY: '육아 휴가',
  BEREAVEMENT: '경조사',
  UNPAID: '무급 휴가',
}

export const leaveStatusLabels: Record<string, string> = {
  PENDING: '승인 대기',
  APPROVED: '승인됨',
  REJECTED: '거부됨',
  CANCELLED: '취소됨',
}

export const attendanceStatusLabels: Record<string, string> = {
  NORMAL: '정상 출근',
  LATE: '지각',
  EARLY_LEAVE: '조퇴',
  ABSENT: '결근',
  PENDING: '퇴근 대기 중',
}

export function leaveTypeLabel(v: string): string { return leaveTypeLabels[v] ?? v }
export function leaveStatusLabel(v: string): string { return leaveStatusLabels[v] ?? v }
export function attendanceStatusLabel(v: string): string { return attendanceStatusLabels[v] ?? v }
```

- [ ] **Step 9: Extend API endpoints constants**

Overwrite `lms_web/src/lib/api/endpoints.ts`:

```ts
export const endpoints = {
  auth: {
    login: '/auth/login',
    refresh: '/auth/refresh',
    logout: '/auth/logout',
  },
  attendance: {
    checkIn: '/attendance/check-in',
    checkOut: '/attendance/check-out',
    myRecords: '/attendance/my-records',
  },
  schedules: {
    mine: '/schedules/my-schedule',
    detail: (id: string) => `/schedules/${id}`,
  },
  leaves: {
    create: '/leaves',
    mine: '/leaves/my-leaves',
    cancel: (id: string) => `/leaves/${id}`,
  },
  payroll: {
    mine: '/payroll/my-payroll',
    detail: (id: string) => `/payroll/${id}`,
  },
  employees: {
    list: '/employees',
    detail: (id: string) => `/employees/${id}`,
  },
} as const
```

- [ ] **Step 10: Typecheck + full suite**

```bash
cd lms_web && npm run typecheck && npm run test:run
```

Expected: typecheck clean, 20/20 pass (prior 14 + 4 date + 2 number).

- [ ] **Step 11: Commit**

```bash
git add lms_web/src/lib/utils/ lms_web/src/lib/api/endpoints.ts
git commit -m "feat(lms_web): shared utilities (date/number formatters, Korean enum labels) + endpoint constants for employee APIs"
```

---

# Task 3.1 — Attendance API + types + schema (TDD for hooks)

**Files:**
- Create: `lms_web/src/features/attendance/types.ts`
- Create: `lms_web/src/features/attendance/schema.ts`
- Create: `lms_web/src/features/attendance/api.ts`

- [ ] **Step 1: Create types**

Create `lms_web/src/features/attendance/types.ts`:

```ts
export type AttendanceStatusValue = 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT' | 'PENDING'

export interface AttendanceRecord {
  id: string
  employeeId: string
  workScheduleId: string | null
  attendanceDate: string          // 'YYYY-MM-DD'
  checkInTime: string             // 'HH:mm:ss'
  checkOutTime: string | null     // 'HH:mm:ss' | null
  actualWorkHours: number | null
  status: AttendanceStatusValue
  note: string | null
  createdAt: string               // ISO-datetime
}

export interface AttendanceListResponse {
  records: AttendanceRecord[]
}
```

- [ ] **Step 2: Create schema**

Create `lms_web/src/features/attendance/schema.ts`:

```ts
import { z } from 'zod'

export const CheckInSchema = z.object({
  workScheduleId: z.string().optional(),
})
export type CheckInInput = z.infer<typeof CheckInSchema>

export const CheckOutSchema = z.object({
  note: z.string().max(500, '메모는 500자 이하로 입력해주세요.').optional(),
})
export type CheckOutInput = z.infer<typeof CheckOutSchema>

export const DateRangeSchema = z.object({
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
})
export type DateRangeInput = z.infer<typeof DateRangeSchema>
```

- [ ] **Step 3: Create API hooks**

Create `lms_web/src/features/attendance/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { AttendanceListResponse, AttendanceRecord } from './types'
import type { CheckInInput, CheckOutInput, DateRangeInput } from './schema'

export const attendanceKeys = {
  all: ['attendance'] as const,
  myRecords: (range: DateRangeInput) =>
    [...attendanceKeys.all, 'my-records', range] as const,
}

export function useMyAttendance(range: DateRangeInput) {
  return useQuery({
    queryKey: attendanceKeys.myRecords(range),
    queryFn: async () => {
      const res = await api.get<AttendanceListResponse>(endpoints.attendance.myRecords, {
        params: range,
      })
      return res.data
    },
  })
}

export function useCheckIn() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: CheckInInput) => {
      const res = await api.post<AttendanceRecord>(endpoints.attendance.checkIn, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: attendanceKeys.all }),
  })
}

export function useCheckOut() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: CheckOutInput) => {
      const res = await api.post<AttendanceRecord>(endpoints.attendance.checkOut, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: attendanceKeys.all }),
  })
}
```

- [ ] **Step 4: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 5: Commit**

```bash
git add lms_web/src/features/attendance/
git commit -m "feat(lms_web): attendance API hooks (useMyAttendance/useCheckIn/useCheckOut) + types + zod schemas"
```

---

# Task 3.2 — AttendancePage (check-in / check-out action page)

**Files:**
- Modify (replace placeholder): `lms_web/src/features/attendance/pages/AttendancePage.tsx`

Today-focused action page: shows today's attendance record (if any) and either a "출근" button (not checked in) or "퇴근" button (checked in, not checked out) or a completion summary (both done).

- [ ] **Step 1: Implement**

Overwrite `lms_web/src/features/attendance/pages/AttendancePage.tsx`:

```tsx
import { useState } from 'react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyAttendance, useCheckIn, useCheckOut } from '../api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { formatHours } from '@/lib/utils/number'
import { getErrorMessage } from '@/lib/utils/errors'

export default function AttendancePage() {
  const today = formatDate(new Date())
  const query = useMyAttendance({ startDate: today, endDate: today })
  const checkIn = useCheckIn()
  const checkOut = useCheckOut()
  const [note, setNote] = useState('')

  const handleCheckIn = () => {
    checkIn.mutate({}, {
      onSuccess: () => toast.success('출근이 기록되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  const handleCheckOut = () => {
    checkOut.mutate({ note: note || undefined }, {
      onSuccess: () => { toast.success('퇴근이 기록되었습니다.'); setNote('') },
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  return (
    <div className="space-y-4">
      <div className="space-y-1">
        <h1 className="text-xl font-semibold">출퇴근</h1>
        <p className="text-sm text-muted-foreground">{formatDateKorean(new Date())}</p>
      </div>

      <QueryBoundary query={query} loadingFallback="오늘 기록을 불러오는 중...">
        {(data) => {
          const record = data.records[0]
          const notCheckedIn = !record
          const onlyCheckedIn = record && !record.checkOutTime
          const done = record && record.checkOutTime

          return (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">오늘 근무</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {notCheckedIn && (
                  <div className="space-y-3">
                    <p className="text-sm text-muted-foreground">아직 출근 기록이 없습니다.</p>
                    <Button className="w-full" disabled={checkIn.isPending} onClick={handleCheckIn}>
                      {checkIn.isPending ? '기록 중...' : '출근하기'}
                    </Button>
                  </div>
                )}

                {onlyCheckedIn && (
                  <div className="space-y-3">
                    <div className="text-sm">
                      <div>출근: <span className="font-medium">{formatTime(record!.checkInTime)}</span></div>
                      <div className="text-muted-foreground">상태: {attendanceStatusLabel(record!.status)}</div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="note">메모 (선택)</Label>
                      <Input id="note" value={note} onChange={(e) => setNote(e.target.value)} placeholder="퇴근 메모" />
                    </div>
                    <Button className="w-full" disabled={checkOut.isPending} onClick={handleCheckOut}>
                      {checkOut.isPending ? '기록 중...' : '퇴근하기'}
                    </Button>
                  </div>
                )}

                {done && (
                  <div className="space-y-1 text-sm">
                    <div>출근: <span className="font-medium">{formatTime(record!.checkInTime)}</span></div>
                    <div>퇴근: <span className="font-medium">{formatTime(record!.checkOutTime)}</span></div>
                    <div>근무시간: <span className="font-medium">{formatHours(record!.actualWorkHours)}</span></div>
                    <div className="text-muted-foreground">상태: {attendanceStatusLabel(record!.status)}</div>
                    {record!.note && <div className="text-muted-foreground">메모: {record!.note}</div>}
                  </div>
                )}
              </CardContent>
            </Card>
          )
        }}
      </QueryBoundary>
    </div>
  )
}
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/features/attendance/pages/AttendancePage.tsx
git commit -m "feat(lms_web): AttendancePage with check-in/out actions and today's record display"
```

---

# Task 3.3 — AttendanceHistoryPage (date-range filter + list)

**Files:**
- Modify: `lms_web/src/features/attendance/pages/AttendanceHistoryPage.tsx`

- [ ] **Step 1: Implement**

Overwrite `lms_web/src/features/attendance/pages/AttendanceHistoryPage.tsx`:

```tsx
import { useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyAttendance } from '../api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { formatHours } from '@/lib/utils/number'

function firstOfMonth(d: Date): Date { const x = new Date(d); x.setDate(1); return x }

export default function AttendanceHistoryPage() {
  const today = new Date()
  const [startDate, setStartDate] = useState(formatDate(firstOfMonth(today)))
  const [endDate, setEndDate] = useState(formatDate(today))

  const query = useMyAttendance({ startDate, endDate })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">근태 이력</h1>

      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-1">
          <Label htmlFor="start">시작일</Label>
          <Input id="start" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="end">종료일</Label>
          <Input id="end" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="해당 기간에 기록이 없습니다."
        isEmpty={(d) => d.records.length === 0}
      >
        {(data) => (
          <ul className="space-y-2">
            {data.records.map((r) => (
              <li key={r.id}>
                <Card>
                  <CardContent className="flex items-center justify-between p-4 text-sm">
                    <div>
                      <div className="font-medium">{formatDateKorean(r.attendanceDate)}</div>
                      <div className="text-muted-foreground">
                        {formatTime(r.checkInTime)} ~ {formatTime(r.checkOutTime)} · {formatHours(r.actualWorkHours)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-muted-foreground">{attendanceStatusLabel(r.status)}</div>
                    </div>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
        )}
      </QueryBoundary>
    </div>
  )
}
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/features/attendance/pages/AttendanceHistoryPage.tsx
git commit -m "feat(lms_web): AttendanceHistoryPage with date-range filter"
```

---

# Task 3.4 — Schedule API + SchedulePage (calendar view)

**Files:**
- Create: `lms_web/src/features/schedule/types.ts`
- Create: `lms_web/src/features/schedule/api.ts`
- Create: `lms_web/src/components/ui/calendar.tsx` (via shadcn CLI)
- Modify: `lms_web/src/features/schedule/pages/SchedulePage.tsx`

- [ ] **Step 1: Install shadcn calendar**

```bash
cd lms_web
npx shadcn@latest add calendar --yes
```

Expected: `src/components/ui/calendar.tsx` appears. If the CLI writes to `@/components/ui/calendar.tsx` literal directory (the Plan 1 Task 0.4 quirk), move it to `src/components/ui/calendar.tsx` and clean up.

If the CLI fails, escalate (NEEDS_CONTEXT). Do not try to hand-write the full react-day-picker wrapper.

- [ ] **Step 2: Create types**

Create `lms_web/src/features/schedule/types.ts`:

```ts
export interface WorkSchedule {
  id: string
  employeeId: string
  storeId: string
  workDate: string             // 'YYYY-MM-DD'
  startTime: string            // 'HH:mm:ss'
  endTime: string              // 'HH:mm:ss'
  workHours: number
  isConfirmed: boolean
  isWeekendWork: boolean
  createdAt: string
}

export interface ScheduleListResponse {
  schedules: WorkSchedule[]
}
```

- [ ] **Step 3: Create API hooks**

Create `lms_web/src/features/schedule/api.ts`:

```ts
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { ScheduleListResponse, WorkSchedule } from './types'

export const scheduleKeys = {
  all: ['schedule'] as const,
  mine: (range: { startDate: string; endDate: string }) =>
    [...scheduleKeys.all, 'mine', range] as const,
  detail: (id: string) => [...scheduleKeys.all, 'detail', id] as const,
}

export function useMySchedule(range: { startDate: string; endDate: string }) {
  return useQuery({
    queryKey: scheduleKeys.mine(range),
    queryFn: async () => {
      const res = await api.get<ScheduleListResponse>(endpoints.schedules.mine, { params: range })
      return res.data
    },
  })
}

export function useScheduleById(id: string | undefined) {
  return useQuery({
    queryKey: scheduleKeys.detail(id ?? ''),
    queryFn: async () => {
      const res = await api.get<WorkSchedule>(endpoints.schedules.detail(id!))
      return res.data
    },
    enabled: !!id,
  })
}
```

- [ ] **Step 4: Implement SchedulePage**

Overwrite `lms_web/src/features/schedule/pages/SchedulePage.tsx`:

```tsx
import { useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Calendar } from '@/components/ui/calendar'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMySchedule } from '../api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { formatHours } from '@/lib/utils/number'

function monthRange(d: Date) {
  const start = new Date(d.getFullYear(), d.getMonth(), 1)
  const end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
  return { startDate: formatDate(start), endDate: formatDate(end) }
}

export default function SchedulePage() {
  const [month, setMonth] = useState(new Date())
  const [selected, setSelected] = useState<Date | undefined>(new Date())
  const range = monthRange(month)
  const query = useMySchedule(range)

  const selectedSchedule = query.data?.schedules.find(
    (s) => selected && s.workDate === formatDate(selected),
  )

  const scheduledDates = (query.data?.schedules ?? []).map((s) => {
    const [y, m, d] = s.workDate.split('-').map(Number)
    return new Date(y, m - 1, d)
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">근무 일정</h1>

      <Card>
        <CardContent className="flex justify-center p-3">
          <Calendar
            mode="single"
            month={month}
            onMonthChange={setMonth}
            selected={selected}
            onSelect={setSelected}
            modifiers={{ scheduled: scheduledDates }}
            modifiersClassNames={{ scheduled: 'bg-primary/15 font-semibold' }}
          />
        </CardContent>
      </Card>

      <QueryBoundary query={query} loadingFallback="일정을 불러오는 중...">
        {() => (
          selectedSchedule ? (
            <Card>
              <CardContent className="p-4 text-sm space-y-1">
                <div className="font-medium">{formatDateKorean(selectedSchedule.workDate)}</div>
                <div>{formatTime(selectedSchedule.startTime)} ~ {formatTime(selectedSchedule.endTime)} · {formatHours(selectedSchedule.workHours)}</div>
                <div className="text-muted-foreground">
                  {selectedSchedule.isConfirmed ? '확정됨' : '미확정'}
                  {selectedSchedule.isWeekendWork && ' · 주말 근무'}
                </div>
              </CardContent>
            </Card>
          ) : (
            <p className="text-center text-sm text-muted-foreground">선택한 날짜에 일정이 없습니다.</p>
          )
        )}
      </QueryBoundary>
    </div>
  )
}
```

- [ ] **Step 5: Typecheck + build**

```bash
cd lms_web && npm run typecheck && npm run build
```

Build must succeed (the calendar component adds `react-day-picker` deps).

- [ ] **Step 6: Commit**

```bash
git add lms_web/
git commit -m "feat(lms_web): schedule API + SchedulePage with month calendar (shadcn Calendar + day-picker)"
```

---

# Task 3.5 — Leave API + types + schema

**Files:**
- Create: `lms_web/src/features/leave/types.ts`
- Create: `lms_web/src/features/leave/schema.ts`
- Create: `lms_web/src/features/leave/api.ts`

- [ ] **Step 1: Create types**

Create `lms_web/src/features/leave/types.ts`:

```ts
export type LeaveTypeValue =
  | 'ANNUAL' | 'SICK' | 'PERSONAL' | 'MATERNITY' | 'PATERNITY' | 'BEREAVEMENT' | 'UNPAID'

export type LeaveStatusValue = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface LeaveRequest {
  id: string
  employeeId: string
  leaveType: LeaveTypeValue
  startDate: string
  endDate: string
  requestedDays: number
  reason: string | null
  status: LeaveStatusValue
  rejectionReason: string | null
  approvedBy: string | null
  approvedAt: string | null
  createdAt: string
}

export interface LeaveListResponse {
  requests: LeaveRequest[]
}
```

- [ ] **Step 2: Create schema**

Create `lms_web/src/features/leave/schema.ts`:

```ts
import { z } from 'zod'

const LEAVE_TYPES = ['ANNUAL', 'SICK', 'PERSONAL', 'MATERNITY', 'PATERNITY', 'BEREAVEMENT', 'UNPAID'] as const

export const LeaveRequestSchema = z.object({
  leaveType: z.enum(LEAVE_TYPES),
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '시작일을 선택해주세요.'),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '종료일을 선택해주세요.'),
  reason: z.string().max(500, '사유는 500자 이하').optional(),
}).refine((v) => v.endDate >= v.startDate, {
  message: '종료일은 시작일 이후여야 합니다.',
  path: ['endDate'],
})

export type LeaveRequestInput = z.infer<typeof LeaveRequestSchema>
```

- [ ] **Step 3: Create API hooks**

Create `lms_web/src/features/leave/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { LeaveListResponse, LeaveRequest } from './types'
import type { LeaveRequestInput } from './schema'

export const leaveKeys = {
  all: ['leave'] as const,
  mine: () => [...leaveKeys.all, 'mine'] as const,
}

export function useMyLeaves() {
  return useQuery({
    queryKey: leaveKeys.mine(),
    queryFn: async () => {
      const res = await api.get<LeaveListResponse>(endpoints.leaves.mine)
      return res.data
    },
  })
}

export function useCreateLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: LeaveRequestInput) => {
      const res = await api.post<LeaveRequest>(endpoints.leaves.create, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveKeys.all }),
  })
}

export function useCancelLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(endpoints.leaves.cancel(id))
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveKeys.all }),
  })
}
```

- [ ] **Step 4: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 5: Commit**

```bash
git add lms_web/src/features/leave/
git commit -m "feat(lms_web): leave API hooks (useMyLeaves/useCreateLeave/useCancelLeave) + types + zod schema"
```

---

# Task 3.6 — LeaveListPage

**Files:**
- Modify: `lms_web/src/features/leave/pages/LeaveListPage.tsx`

- [ ] **Step 1: Implement**

Overwrite `lms_web/src/features/leave/pages/LeaveListPage.tsx`:

```tsx
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyLeaves, useCancelLeave } from '../api'
import { leaveTypeLabel, leaveStatusLabel } from '@/lib/utils/labels'
import { formatDateKorean } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'
import { cn } from '@/lib/utils'

const statusTone: Record<string, string> = {
  PENDING: 'text-yellow-600',
  APPROVED: 'text-green-700',
  REJECTED: 'text-destructive',
  CANCELLED: 'text-muted-foreground line-through',
}

export default function LeaveListPage() {
  const query = useMyLeaves()
  const cancel = useCancelLeave()

  const onCancel = (id: string) => {
    cancel.mutate(id, {
      onSuccess: () => toast.success('휴가 신청이 취소되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">내 휴가</h1>
        <Link to="/leave/request"><Button size="sm">신청하기</Button></Link>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="신청한 휴가가 없습니다."
        isEmpty={(d) => d.requests.length === 0}
      >
        {(data) => (
          <ul className="space-y-2">
            {data.requests.map((r) => (
              <li key={r.id}>
                <Card>
                  <CardContent className="p-4 text-sm space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="font-medium">{leaveTypeLabel(r.leaveType)} · {r.requestedDays}일</div>
                      <span className={cn('text-xs', statusTone[r.status] ?? '')}>{leaveStatusLabel(r.status)}</span>
                    </div>
                    <div className="text-muted-foreground">
                      {formatDateKorean(r.startDate)} ~ {formatDateKorean(r.endDate)}
                    </div>
                    {r.reason && <div className="text-muted-foreground">사유: {r.reason}</div>}
                    {r.rejectionReason && <div className="text-destructive">반려 사유: {r.rejectionReason}</div>}
                    {r.status === 'PENDING' && (
                      <div className="flex justify-end">
                        <Button variant="outline" size="sm" disabled={cancel.isPending} onClick={() => onCancel(r.id)}>
                          취소
                        </Button>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
        )}
      </QueryBoundary>
    </div>
  )
}
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/features/leave/pages/LeaveListPage.tsx
git commit -m "feat(lms_web): LeaveListPage with cancel action for pending requests"
```

---

# Task 3.7 — LeaveRequestPage (form with RHF + Zod)

**Files:**
- Modify: `lms_web/src/features/leave/pages/LeaveRequestPage.tsx`

- [ ] **Step 1: Ensure select component available**

```bash
cd lms_web && npx shadcn@latest add select --yes
```

(If the CLI misroutes to `@/components/ui/select.tsx` literal directory, move to `src/components/ui/select.tsx`.)

- [ ] **Step 2: Implement**

Overwrite `lms_web/src/features/leave/pages/LeaveRequestPage.tsx`:

```tsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Form, FormControl, FormField, FormItem, FormLabel, FormMessage,
} from '@/components/ui/form'
import { Card, CardContent } from '@/components/ui/card'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { LeaveRequestSchema, type LeaveRequestInput } from '../schema'
import { useCreateLeave } from '../api'
import { leaveTypeLabel, leaveTypeLabels } from '@/lib/utils/labels'
import { getErrorMessage } from '@/lib/utils/errors'

const leaveTypes = Object.keys(leaveTypeLabels) as Array<keyof typeof leaveTypeLabels>

export default function LeaveRequestPage() {
  const navigate = useNavigate()
  const create = useCreateLeave()

  const form = useForm<LeaveRequestInput>({
    resolver: zodResolver(LeaveRequestSchema),
    defaultValues: { leaveType: 'ANNUAL', startDate: '', endDate: '', reason: '' },
  })

  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('휴가가 신청되었습니다.'); navigate('/leave') },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">휴가 신청</h1>
      <Card>
        <CardContent className="pt-6">
          <Form {...form}>
            <form onSubmit={onSubmit} className="space-y-4">
              <FormField
                control={form.control}
                name="leaveType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>유형</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger><SelectValue /></SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {leaveTypes.map((t) => (
                          <SelectItem key={t} value={t}>{leaveTypeLabel(t)}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="startDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>시작일</FormLabel>
                    <FormControl><Input type="date" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="endDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>종료일</FormLabel>
                    <FormControl><Input type="date" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="reason"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>사유 (선택)</FormLabel>
                    <FormControl><Input placeholder="예: 병원 방문" {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {form.formState.errors.root && (
                <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
              )}

              <div className="flex gap-2">
                <Button type="button" variant="outline" className="flex-1" onClick={() => navigate(-1)}>취소</Button>
                <Button type="submit" className="flex-1" disabled={create.isPending}>
                  {create.isPending ? '신청 중...' : '신청'}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 3: Typecheck + build**

```bash
cd lms_web && npm run typecheck && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add lms_web/
git commit -m "feat(lms_web): LeaveRequestPage form with leave-type select and date validation"
```

---

# Task 3.8 — Payroll API + PayrollListPage

**Files:**
- Create: `lms_web/src/features/payroll/types.ts`
- Create: `lms_web/src/features/payroll/api.ts`
- Modify: `lms_web/src/features/payroll/pages/PayrollListPage.tsx`

- [ ] **Step 1: Create types**

Create `lms_web/src/features/payroll/types.ts`:

```ts
export interface Payroll {
  id: string
  employeeId: string
  period: string              // 'YYYY-MM'
  baseAmount: number
  overtimeAmount: number
  totalAmount: number
  isPaid: boolean
  paidAt: string | null
  calculatedAt: string
  createdAt: string
}

export interface PayrollDetail {
  id: string
  payrollId: string
  workDate: string
  workType: string
  hours: number
  hourlyRate: number
  multiplier: number
  amount: number
}

export interface PayrollWithDetails {
  payroll: Payroll
  details: PayrollDetail[]
}
```

- [ ] **Step 2: Create API hooks**

Create `lms_web/src/features/payroll/api.ts`:

```ts
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { Payroll, PayrollWithDetails } from './types'

export const payrollKeys = {
  all: ['payroll'] as const,
  mine: () => [...payrollKeys.all, 'mine'] as const,
  detail: (id: string) => [...payrollKeys.all, 'detail', id] as const,
}

export function useMyPayrolls() {
  return useQuery({
    queryKey: payrollKeys.mine(),
    queryFn: async () => {
      const res = await api.get<Payroll[]>(endpoints.payroll.mine)
      return res.data
    },
  })
}

export function usePayrollDetail(id: string | undefined) {
  return useQuery({
    queryKey: payrollKeys.detail(id ?? ''),
    queryFn: async () => {
      const res = await api.get<PayrollWithDetails>(endpoints.payroll.detail(id!))
      return res.data
    },
    enabled: !!id,
  })
}
```

- [ ] **Step 3: Implement PayrollListPage**

Overwrite `lms_web/src/features/payroll/pages/PayrollListPage.tsx`:

```tsx
import { Link } from 'react-router-dom'
import { Card, CardContent } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useMyPayrolls } from '../api'
import { formatKRW } from '@/lib/utils/number'

export default function PayrollListPage() {
  const query = useMyPayrolls()

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">내 급여</h1>
      <QueryBoundary
        query={query}
        emptyMessage="급여 내역이 없습니다."
        isEmpty={(d) => d.length === 0}
      >
        {(data) => (
          <ul className="space-y-2">
            {data.map((p) => (
              <li key={p.id}>
                <Link to={`/payroll/${p.id}`}>
                  <Card className="transition-colors hover:bg-accent">
                    <CardContent className="flex items-center justify-between p-4">
                      <div>
                        <div className="font-medium">{p.period}</div>
                        <div className="text-xs text-muted-foreground">
                          {p.isPaid ? '지급 완료' : '미지급'}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-semibold">{formatKRW(p.totalAmount)}</div>
                      </div>
                    </CardContent>
                  </Card>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </QueryBoundary>
    </div>
  )
}
```

- [ ] **Step 4: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 5: Commit**

```bash
git add lms_web/src/features/payroll/
git commit -m "feat(lms_web): payroll API + PayrollListPage with KRW totals and paid status"
```

---

# Task 3.9 — PayrollDetailPage (with daily breakdown)

**Files:**
- Modify: `lms_web/src/features/payroll/pages/PayrollDetailPage.tsx`

- [ ] **Step 1: Implement**

Overwrite `lms_web/src/features/payroll/pages/PayrollDetailPage.tsx`:

```tsx
import { useParams } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { usePayrollDetail } from '../api'
import { formatKRW, formatHours } from '@/lib/utils/number'
import { formatDateKorean } from '@/lib/utils/date'

export default function PayrollDetailPage() {
  const { id } = useParams<{ id: string }>()
  const query = usePayrollDetail(id)

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold">급여 상세</h1>

      <QueryBoundary query={query}>
        {(data) => (
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">{data.payroll.period}</CardTitle>
              </CardHeader>
              <CardContent className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">기본급</span>
                  <span>{formatKRW(data.payroll.baseAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">연장근무</span>
                  <span>{formatKRW(data.payroll.overtimeAmount)}</span>
                </div>
                <div className="flex justify-between border-t pt-2 font-semibold">
                  <span>합계</span>
                  <span>{formatKRW(data.payroll.totalAmount)}</span>
                </div>
                <div className="pt-2 text-xs text-muted-foreground">
                  {data.payroll.isPaid ? '지급 완료' : '미지급'}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">일자별 내역</CardTitle>
              </CardHeader>
              <CardContent>
                {data.details.length === 0 ? (
                  <p className="text-sm text-muted-foreground">상세 내역이 없습니다.</p>
                ) : (
                  <ul className="divide-y text-sm">
                    {data.details.map((d) => (
                      <li key={d.id} className="flex items-center justify-between py-2">
                        <div>
                          <div>{formatDateKorean(d.workDate)}</div>
                          <div className="text-xs text-muted-foreground">
                            {d.workType} · {formatHours(d.hours)} · {formatKRW(d.hourlyRate)}/h × {d.multiplier}
                          </div>
                        </div>
                        <div className="font-medium">{formatKRW(d.amount)}</div>
                      </li>
                    ))}
                  </ul>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </QueryBoundary>
    </div>
  )
}
```

- [ ] **Step 2: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 3: Commit**

```bash
git add lms_web/src/features/payroll/pages/PayrollDetailPage.tsx
git commit -m "feat(lms_web): PayrollDetailPage with summary card and daily breakdown"
```

---

# Task 3.10 — HomePage (dashboard)

Aggregates today's schedule + today's attendance + pending leaves count + employee profile (name, remaining leave).

**Files:**
- Create: `lms_web/src/features/home/api.ts`
- Modify: `lms_web/src/features/home/pages/HomePage.tsx`

- [ ] **Step 1: Create home API**

Create `lms_web/src/features/home/api.ts`:

```ts
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import { useAuthStore } from '@/features/auth/store'

interface EmployeeResponse {
  id: string
  userId: string
  name: string
  employeeType: string
  storeId: string | null
  remainingLeave: number
  isActive: boolean
  createdAt: string
}

interface EmployeeListResponse {
  employees: EmployeeResponse[]
}

export const homeKeys = {
  all: ['home'] as const,
  currentEmployee: (userId: string) => [...homeKeys.all, 'current-employee', userId] as const,
}

export function useCurrentEmployee() {
  const userId = useAuthStore((s) => s.currentUser?.userId) ?? ''
  return useQuery({
    queryKey: homeKeys.currentEmployee(userId),
    queryFn: async () => {
      const res = await api.get<EmployeeListResponse | EmployeeResponse[]>(endpoints.employees.list, {
        params: { activeOnly: true },
      })
      // Handle both possible shapes (wrapped list or plain array)
      const list = Array.isArray(res.data) ? res.data : res.data.employees
      const match = list.find((e) => e.userId === userId)
      if (!match) throw new Error('내 직원 정보를 찾을 수 없습니다.')
      return match
    },
    enabled: !!userId,
  })
}
```

Note: The backend `EmployeeListResponse` wrapper shape vs. plain array — we handle both defensively because the Explore report wasn't 100% certain. If tests/runtime show only one shape is used, the dead branch can be removed later.

- [ ] **Step 2: Implement HomePage**

Overwrite `lms_web/src/features/home/pages/HomePage.tsx`:

```tsx
import { Link } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useCurrentEmployee } from '../api'
import { useMyAttendance } from '@/features/attendance/api'
import { useMySchedule } from '@/features/schedule/api'
import { useMyLeaves } from '@/features/leave/api'
import { formatDate, formatTime, formatDateKorean } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { useAuthStore } from '@/features/auth/store'

export default function HomePage() {
  const today = formatDate(new Date())
  const employeeQuery = useCurrentEmployee()
  const attendanceQuery = useMyAttendance({ startDate: today, endDate: today })
  const scheduleQuery = useMySchedule({ startDate: today, endDate: today })
  const leavesQuery = useMyLeaves()
  const user = useAuthStore((s) => s.currentUser)

  const pendingCount = leavesQuery.data?.requests.filter((r) => r.status === 'PENDING').length ?? 0
  const todaySchedule = scheduleQuery.data?.schedules[0]
  const todayAttendance = attendanceQuery.data?.records[0]

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-semibold">
          안녕하세요{employeeQuery.data ? `, ${employeeQuery.data.name}` : ''}님
        </h1>
        <p className="text-sm text-muted-foreground">{formatDateKorean(new Date())}</p>
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base">오늘 근무</CardTitle></CardHeader>
        <CardContent className="text-sm space-y-1">
          {todaySchedule ? (
            <div>{formatTime(todaySchedule.startTime)} ~ {formatTime(todaySchedule.endTime)}</div>
          ) : (
            <div className="text-muted-foreground">오늘 예정된 근무가 없습니다.</div>
          )}
          {todayAttendance ? (
            <div className="text-muted-foreground">
              출근 {formatTime(todayAttendance.checkInTime)}
              {todayAttendance.checkOutTime && ` · 퇴근 ${formatTime(todayAttendance.checkOutTime)}`}
              {` · ${attendanceStatusLabel(todayAttendance.status)}`}
            </div>
          ) : (
            <Link to="/attendance" className="text-sm text-primary">→ 출근하기</Link>
          )}
        </CardContent>
      </Card>

      <div className="grid grid-cols-2 gap-3">
        <Link to="/leave">
          <Card className="h-full transition-colors hover:bg-accent">
            <CardContent className="p-4">
              <div className="text-xs text-muted-foreground">대기 중인 휴가</div>
              <div className="text-2xl font-semibold">{pendingCount}</div>
            </CardContent>
          </Card>
        </Link>

        <QueryBoundary
          query={employeeQuery}
          loadingFallback={<div className="h-full" />}
        >
          {(emp) => (
            <Card className="h-full">
              <CardContent className="p-4">
                <div className="text-xs text-muted-foreground">남은 연차</div>
                <div className="text-2xl font-semibold">{emp.remainingLeave}일</div>
              </CardContent>
            </Card>
          )}
        </QueryBoundary>
      </div>

      <div className="space-y-2 pt-2">
        <Link to="/payroll" className="block text-sm text-primary">→ 내 급여 보기</Link>
        <Link to="/schedule" className="block text-sm text-primary">→ 근무 일정 전체 보기</Link>
      </div>
    </div>
  )
}
```

- [ ] **Step 3: Typecheck + build + test suite**

```bash
cd lms_web && npm run typecheck && npm run test:run && npm run build
```

Expected: all pass. 20/20 tests (no new tests added in this task).

- [ ] **Step 4: Commit**

```bash
git add lms_web/src/features/home/
git commit -m "feat(lms_web): HomePage dashboard aggregating today's schedule/attendance + pending leaves + remaining annual leave"
```

---

# Task 3.11 — End-to-end smoke + fix issues

This is a user-driven smoke test against the live backend, analogous to Task 2.8 in Plan 1.

- [ ] **Step 1: Start services**

Three terminals (PowerShell-friendly — one command per line):

```
Terminal 1: docker-compose up -d
Terminal 2: .\gradlew :interfaces:bootRun
Terminal 3:
  cd lms_web
  npm run dev
```

- [ ] **Step 2: EMPLOYEE account — verify all 8 pages**

Log in at http://localhost:5173/login with an EMPLOYEE-role account. Confirm each:

- [ ] `/home` — dashboard renders name, today's schedule (if seeded), pending leave count, remaining annual leave.
- [ ] `/attendance` — button changes based on today's record (출근 / 퇴근 / 완료 상태).
- [ ] `/attendance/history` — date-range filter works, changing dates triggers a refetch.
- [ ] `/schedule` — calendar shows month, scheduled dates highlighted, clicking a scheduled date shows detail.
- [ ] `/leave` — list of my requests, with correct status badges; cancel button only on PENDING items.
- [ ] `/leave/request` — form validates (start/end date, leave type select), submit creates a new request and navigates back to /leave.
- [ ] `/payroll` — list of monthly totals, clickable.
- [ ] `/payroll/:id` — shows baseline + overtime + total + daily breakdown.

If any page errors out, inspect browser DevTools Network tab for the failing request and compare the response shape to what `types.ts` expects. Fix in-place, commit separately with message `fix(lms_web): ...`.

- [ ] **Step 3: MANAGER/SUPER_ADMIN check (no regression)**

Log in with MANAGER or SUPER_ADMIN role. `/admin` should still work (placeholder dashboard), and admin sidebar renders. The 8 employee pages are still reachable via manual URL entry and should also work for MANAGER/SUPER_ADMIN accounts since the backend endpoints are `EMPLOYEE+` (i.e., any authenticated user can use them).

- [ ] **Step 4: Log findings**

Any bugs or surprises — list them. For each, either patch now (with a separate `fix` commit) or record in the summary for Plan 3.

---

# Self-Review Checklist

- [ ] Typecheck: `cd lms_web && npm run typecheck` → 0 errors.
- [ ] Tests: `npm run test:run` → 20/20 (prior 14 + 4 date + 2 number).
- [ ] Build: `npm run build` → succeeds.
- [ ] No placeholder content (no `Placeholder` component imports remain in employee `pages/*.tsx` — every page is fully implemented).
- [ ] Enum values match backend: verified LeaveType (7), AttendanceStatus (5), LeaveStatus (4).
- [ ] All 5 feature modules (`attendance`, `schedule`, `leave`, `payroll`, `home`) have their own `api.ts` with query-key factory.
- [ ] Role-aware routing: only EMPLOYEE-reachable URLs affected; admin routes untouched.

---

# Corrections / gaps carried forward

**Addressed in this plan:**
- Plan 1 corrections (CORS open, no /auth/me, refresh returns access only) — all honored.

**Deferred to Plan 3 (Admin) or Plan 4 (E2E/Polish):**
- HomePage's `/api/employees` scan-and-filter is ugly — if backend later exposes a `/api/employees/me` endpoint, swap it in.
- Leave request calendar picker (currently plain `<input type="date">`) can be upgraded to a `<Calendar>` popover like in SchedulePage. Acceptable for demo; nicer UX is a post-plan polish.
- Error toasts for queries (not mutations) — currently silent; `<QueryBoundary>` shows the error inline. Acceptable.
- No Zustand / API tests in this plan — the interesting logic is mostly in HomePage's aggregation and the AttendancePage state machine. If a regression bites later, add targeted tests.

---

# Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-04-23-lms-web-employee-features.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints.

**Which approach?**
