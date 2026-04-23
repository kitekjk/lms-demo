# LMS Web Admin Features Implementation Plan (Plan 3 of 4)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all 8 admin-facing pages (Dashboard, Leave approval, Store/Employee/PayrollPolicy CRUD, Schedule/Attendance management, Payroll batch) wired to the real Spring Boot backend. After this plan, MANAGER and SUPER_ADMIN accounts can fully operate the admin console.

**Architecture:** Extends Plan 2 patterns with admin-specific primitives: shadcn `Table` for list views, shadcn `Dialog` for create/edit modals, role-scoped actions (MANAGER vs SUPER_ADMIN). Each admin feature gets its own module under `features/admin/<entity>/` so the placeholder pages can be replaced in isolation. Reuses existing `features/leave/`, `features/schedule/`, etc. API hooks where possible, adding only admin-specific hooks (list by storeId, approve/reject, batch, etc.).

**Tech Stack (already installed):** React 19, TypeScript 6, Vite 8, Tailwind v3, shadcn/ui (button/input/label/card/form/separator/sonner/calendar/select), TanStack Query v5, Zustand v5, react-router-dom v6, axios, react-hook-form, zod, date-fns, lucide-react.

**New shadcn components needed:** `table`, `dialog`, `badge`, `dropdown-menu`, `textarea`.

**Prior plans:**
- `docs/superpowers/plans/2026-04-22-lms-web-foundation.md` (Plan 1, merged `fa185b9` via PR #1-precursor, then `212647e`)
- `docs/superpowers/plans/2026-04-23-lms-web-employee-features.md` (Plan 2, merged `212647e` and `64e38ac` via PRs #1 and #2)

---

## Source of Truth: Admin Backend Contracts

**All admin endpoints return RAW DTOs** (no `ApiResponse<T>` wrapper). Collections use explicit wrappers like `{ employees: [], totalCount }`. `userId` in mutations is derived server-side from JWT, never in request body.

### Employee — `/api/employees` (MANAGER+ for write, any-auth for read)

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/` | MANAGER+ | `EmployeeCreateRequest` | `EmployeeResponse` |
| GET | `/?storeId=&activeOnly=` | Auth | — | `EmployeeListResponse` |
| GET | `/{employeeId}` | Auth | — | `EmployeeResponse` |
| PUT | `/{employeeId}` | MANAGER+ | `EmployeeUpdateRequest` | `EmployeeResponse` |
| PATCH | `/{employeeId}/deactivate` | MANAGER+ | — | `EmployeeResponse` |

- `EmployeeCreateRequest`: `{ userId: string, name: string, employeeType: 'REGULAR'|'IRREGULAR'|'PART_TIME', storeId?: string }`
- `EmployeeUpdateRequest`: `{ name: string, employeeType: EmployeeType, storeId?: string }`
- `EmployeeResponse`: `{ id, userId, name, employeeType, storeId?, remainingLeave (number), isActive, createdAt }`
- `EmployeeListResponse`: `{ employees: EmployeeResponse[], totalCount: number }`

### Store — `/api/stores` (SUPER_ADMIN)

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/` | SUPER_ADMIN | `StoreCreateRequest` | `StoreResponse` |
| GET | `/` | SUPER_ADMIN | — | `StoreListResponse` |
| GET | `/{storeId}` | Auth | — | `StoreResponse` |
| PUT | `/{storeId}` | SUPER_ADMIN | `StoreUpdateRequest` | `StoreResponse` |
| DELETE | `/{storeId}` | SUPER_ADMIN | — | 204 |

- `StoreCreateRequest` / `StoreUpdateRequest`: `{ name: string, location: string }`
- `StoreResponse`: `{ id, name, location, createdAt }`
- `StoreListResponse`: `{ stores: StoreResponse[], totalCount: number }`

### WorkSchedule — `/api/schedules` (MANAGER+ for write)

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/` | MANAGER+ | `WorkScheduleCreateRequest` | `WorkScheduleResponse` |
| GET | `/?employeeId=&storeId=&startDate=&endDate=` | Auth | — | `WorkScheduleListResponse` |
| PUT | `/{scheduleId}` | MANAGER+ | `WorkScheduleUpdateRequest` | `WorkScheduleResponse` |
| DELETE | `/{scheduleId}` | MANAGER+ | — | 204 |

- `WorkScheduleCreateRequest`: `{ employeeId, storeId, workDate: 'YYYY-MM-DD', startTime: 'HH:mm:ss', endTime: 'HH:mm:ss' }`
- `WorkScheduleUpdateRequest`: `{ workDate?, startTime?, endTime? }` (partial)
- Response types already declared in Plan 2 `features/schedule/types.ts`.

### Attendance (admin) — `/api/attendance`

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| GET | `/records?storeId=&startDate=&endDate=` | MANAGER+ | storeId **required** | `AttendanceListResponse` |
| PUT | `/records/{recordId}` | MANAGER+ | `AttendanceAdjustRequest` | `AttendanceRecordResponse` |

- `AttendanceAdjustRequest`: `{ adjustedCheckInTime: ISO-datetime, adjustedCheckOutTime?: ISO-datetime, reason: string }`

### Leave (admin) — `/api/leaves`

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| GET | `/?storeId=` | MANAGER+ | storeId **required** | `LeaveListResponse` |
| GET | `/pending` | MANAGER+ | — | `LeaveListResponse` |
| PATCH | `/{leaveId}/approve` | MANAGER+ | — (empty) | `LeaveRequest` |
| PATCH | `/{leaveId}/reject` | MANAGER+ | `LeaveRejectionRequest` | `LeaveRequest` |

- `LeaveRejectionRequest`: `{ rejectionReason: string }`

### Payroll (admin) — `/api/payroll`

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/calculate` | MANAGER+ | `PayrollCalculateRequest` | `PayrollResponse` |
| POST | `/batch` | SUPER_ADMIN | `PayrollBatchExecuteRequest` | `PayrollBatchHistoryResponse` |
| GET | `/?period=YYYY-MM` | MANAGER+ | period required | `PayrollResponse[]` (plain array) |
| GET | `/batch-history?startDate=&endDate=` | MANAGER+ | — | `PayrollBatchHistoryResponse[]` (plain array) |

- `PayrollCalculateRequest`: `{ employeeId, period: 'YYYY-MM', hourlyRate (number) }`
- `PayrollBatchExecuteRequest`: `{ period: 'YYYY-MM', storeId? }`
- `PayrollBatchHistoryResponse`: `{ id, period, storeId?, status: 'RUNNING'|'COMPLETED'|'PARTIAL_SUCCESS'|'FAILED', totalCount, successCount, failureCount, startedAt, completedAt?, errorMessage?, createdAt }`

### PayrollPolicy — `/api/payroll-policies`

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/` | SUPER_ADMIN | `PayrollPolicyCreateRequest` | `PayrollPolicyResponse` |
| GET | `/` | MANAGER+ | `?policyType=<enum>` | `PayrollPolicyListResponse` |
| GET | `/active` | Auth | — | `PayrollPolicyListResponse` |
| PUT | `/{policyId}` | SUPER_ADMIN | `PayrollPolicyUpdateRequest` | `PayrollPolicyResponse` |
| DELETE | `/{policyId}` | SUPER_ADMIN | — | 204 |

- `PayrollPolicyCreateRequest`: `{ policyType, multiplier (number, 0–10), effectiveFrom: 'YYYY-MM-DD', effectiveTo?: 'YYYY-MM-DD', description? }`
- `PayrollPolicyResponse`: `{ id, policyType, policyTypeDescription, multiplier, effectiveFrom, effectiveTo?, description?, isCurrentlyEffective, createdAt }`
- `PolicyType` values: `OVERTIME_WEEKDAY | OVERTIME_WEEKEND | OVERTIME_HOLIDAY | NIGHT_SHIFT | HOLIDAY_WORK | BONUS | ALLOWANCE`

### Additional Enums (for label maps)

- `EmployeeType`: `REGULAR` (정규), `IRREGULAR` (비정규), `PART_TIME` (파트타임)
- `WorkType` (payroll detail): `WEEKDAY`, `NIGHT`, `WEEKEND`, `HOLIDAY`
- `BatchStatus`: `RUNNING` (실행 중), `COMPLETED` (완료), `PARTIAL_SUCCESS` (부분 성공), `FAILED` (실패)

---

## File Structure — new files in this plan

```
lms_web/src/
├── components/
│   └── ui/
│       ├── table.tsx                      # shadcn table (Task 4.0)
│       ├── dialog.tsx                     # shadcn dialog (Task 4.0)
│       ├── badge.tsx                      # shadcn badge (Task 4.0)
│       ├── dropdown-menu.tsx              # shadcn dropdown (Task 4.0)
│       └── textarea.tsx                   # shadcn textarea (Task 4.0)
├── lib/
│   ├── utils/
│   │   └── labels.ts                      # extend with admin enum labels
│   └── api/
│       └── endpoints.ts                   # extend with admin endpoints
└── features/
    └── admin/
        ├── dashboard/
        │   └── pages/AdminDashboardPage.tsx  # replaces placeholder
        ├── leaves/
        │   ├── api.ts                     # usePendingLeaves, useStoreLeaves, useApprove, useReject
        │   ├── schema.ts                  # Zod RejectSchema
        │   └── pages/LeaveManagementPage.tsx
        ├── stores/
        │   ├── api.ts                     # CRUD hooks
        │   ├── schema.ts
        │   ├── types.ts
        │   └── pages/StoreManagementPage.tsx
        ├── employees/
        │   ├── api.ts
        │   ├── schema.ts
        │   ├── types.ts                   # EmployeeType, extended shape
        │   └── pages/EmployeeManagementPage.tsx
        ├── policies/                       # PayrollPolicy
        │   ├── api.ts
        │   ├── schema.ts
        │   ├── types.ts
        │   └── pages/PayrollPolicyPage.tsx
        ├── schedules/
        │   ├── api.ts                      # admin CRUD hooks (list with filters)
        │   ├── schema.ts
        │   └── pages/ScheduleManagementPage.tsx
        ├── attendance/
        │   ├── api.ts                      # list + adjust
        │   ├── schema.ts                   # AdjustAttendanceSchema
        │   └── pages/AttendanceManagementPage.tsx
        └── payroll/
            ├── api.ts                      # list by period, batch, batch-history
            ├── schema.ts                   # BatchExecuteSchema, CalculateSchema
            └── pages/PayrollManagementPage.tsx
```

**Existing placeholder pages replaced** (already wired in Plan 1 routes.tsx):
- `src/features/admin/pages/{AdminDashboardPage, LeaveManagementPage, StoreManagementPage, EmployeeManagementPage, PayrollPolicyPage, ScheduleManagementPage, AttendanceManagementPage, PayrollManagementPage}.tsx`

**Strategy for placeholder replacement**: Routes point to the existing paths (e.g., `@/features/admin/pages/EmployeeManagementPage`). We keep the route imports as-is but change each file to be a thin re-export of the new feature-module page:

```tsx
// src/features/admin/pages/EmployeeManagementPage.tsx
export { default } from '@/features/admin/employees/pages/EmployeeManagementPage'
```

This keeps routes.tsx untouched while enabling modular feature folders.

---

## Pattern References (established in Plan 1/2 — don't re-derive)

- **Feature module layout**: `api.ts` + `schema.ts` + `types.ts` + `pages/*.tsx`. See `features/attendance/` for canonical example.
- **Query keys factory**: `export const xKeys = { all, list(filters), detail(id) }`. See `features/leave/api.ts`.
- **Page skeleton with QueryBoundary**:

```tsx
<QueryBoundary query={q} isEmpty={(d) => d.items.length === 0} emptyMessage="...">
  {(data) => <Table>...</Table>}
</QueryBoundary>
```

- **Form skeleton with RHF + Zod + shadcn Form**: See `features/leave/pages/LeaveRequestPage.tsx` for the Select+Input+DateInput combo.
- **Toast on mutation result**: `onSuccess: () => toast.success(...)`, `onError: (e) => toast.error(getErrorMessage(e))`.

### New Admin Pattern: `AdminList` with Dialog

Used by Store, Employee, PayrollPolicy pages. Structure:

```tsx
<div className="space-y-4">
  <div className="flex items-center justify-between">
    <h1 className="text-xl font-semibold">{title}</h1>
    <Dialog open={createOpen} onOpenChange={setCreateOpen}>
      <DialogTrigger asChild><Button>추가</Button></DialogTrigger>
      <DialogContent><DialogHeader><DialogTitle>신규</DialogTitle></DialogHeader>{/* form */}</DialogContent>
    </Dialog>
  </div>
  <QueryBoundary query={listQuery}>{(data) => (
    <Table>
      <TableHeader>...</TableHeader>
      <TableBody>
        {data.items.map(item => (
          <TableRow key={item.id}>
            <TableCell>...</TableCell>
            <TableCell className="text-right">
              <DropdownMenu>
                <DropdownMenuTrigger asChild><Button variant="ghost" size="icon"><MoreHorizontal/></Button></DropdownMenuTrigger>
                <DropdownMenuContent>
                  <DropdownMenuItem onClick={() => setEditItem(item)}>수정</DropdownMenuItem>
                  <DropdownMenuItem onClick={() => onDelete(item.id)} className="text-destructive">삭제</DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )}</QueryBoundary>
  {editItem && <EditDialog item={editItem} onClose={() => setEditItem(null)} />}
</div>
```

This template is referenced by Tasks 4.3, 4.4, 4.5. Details per task show only the variations (schema fields, table columns, action handlers).

---

# Phase M4.A — Foundation + Simple Admin Pages

## Task 4.0 — Shared admin UI (shadcn components + label extensions)

**Files:**
- Create (via shadcn): `lms_web/src/components/ui/{table,dialog,badge,dropdown-menu,textarea}.tsx`
- Modify: `lms_web/src/lib/utils/labels.ts`
- Modify: `lms_web/src/lib/api/endpoints.ts`

- [ ] **Step 1: Install shadcn components**

```bash
cd lms_web
npx shadcn@latest add table dialog badge dropdown-menu textarea --yes
```

Verify all 5 files in `src/components/ui/` after install. If spurious `lms_web/@/` directory appears (known quirk from earlier tasks), move files to `src/components/ui/` and `rm -rf '@'`.

- [ ] **Step 2: Extend Korean labels**

Append to `lms_web/src/lib/utils/labels.ts` (keep all existing exports):

```ts
export const employeeTypeLabels: Record<string, string> = {
  REGULAR: '정규',
  IRREGULAR: '비정규',
  PART_TIME: '파트타임',
}
export function employeeTypeLabel(v: string): string { return employeeTypeLabels[v] ?? v }

export const workTypeLabels: Record<string, string> = {
  WEEKDAY: '평일',
  NIGHT: '야간',
  WEEKEND: '주말',
  HOLIDAY: '공휴일',
}
export function workTypeLabel(v: string): string { return workTypeLabels[v] ?? v }

export const batchStatusLabels: Record<string, string> = {
  RUNNING: '실행 중',
  COMPLETED: '완료',
  PARTIAL_SUCCESS: '부분 성공',
  FAILED: '실패',
}
export function batchStatusLabel(v: string): string { return batchStatusLabels[v] ?? v }

export const policyTypeLabels: Record<string, string> = {
  OVERTIME_WEEKDAY: '평일 연장',
  OVERTIME_WEEKEND: '주말 연장',
  OVERTIME_HOLIDAY: '공휴일 연장',
  NIGHT_SHIFT: '야간',
  HOLIDAY_WORK: '공휴일 근무',
  BONUS: '보너스',
  ALLOWANCE: '수당',
}
export function policyTypeLabel(v: string): string { return policyTypeLabels[v] ?? v }
```

- [ ] **Step 3: Extend API endpoints**

Replace `lms_web/src/lib/api/endpoints.ts` with:

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
    records: '/attendance/records',
    adjust: (id: string) => `/attendance/records/${id}`,
  },
  schedules: {
    mine: '/schedules/my-schedule',
    list: '/schedules',
    detail: (id: string) => `/schedules/${id}`,
  },
  leaves: {
    create: '/leaves',
    mine: '/leaves/my-leaves',
    list: '/leaves',
    pending: '/leaves/pending',
    cancel: (id: string) => `/leaves/${id}`,
    approve: (id: string) => `/leaves/${id}/approve`,
    reject: (id: string) => `/leaves/${id}/reject`,
  },
  payroll: {
    mine: '/payroll/my-payroll',
    list: '/payroll',
    detail: (id: string) => `/payroll/${id}`,
    calculate: '/payroll/calculate',
    batch: '/payroll/batch',
    batchHistory: '/payroll/batch-history',
  },
  employees: {
    list: '/employees',
    detail: (id: string) => `/employees/${id}`,
    create: '/employees',
    update: (id: string) => `/employees/${id}`,
    deactivate: (id: string) => `/employees/${id}/deactivate`,
  },
  stores: {
    list: '/stores',
    detail: (id: string) => `/stores/${id}`,
    create: '/stores',
    update: (id: string) => `/stores/${id}`,
    delete: (id: string) => `/stores/${id}`,
  },
  policies: {
    list: '/payroll-policies',
    active: '/payroll-policies/active',
    create: '/payroll-policies',
    update: (id: string) => `/payroll-policies/${id}`,
    delete: (id: string) => `/payroll-policies/${id}`,
  },
} as const
```

- [ ] **Step 4: Typecheck + tests**

```bash
cd lms_web && npm run typecheck && npm run test:run
```

Expected: typecheck clean, 20/20 tests pass.

- [ ] **Step 5: Commit**

```bash
git add lms_web/
git commit -m "feat(lms_web): install admin shadcn primitives (table/dialog/badge/dropdown-menu/textarea) + admin enum labels + admin endpoints"
```

---

## Task 4.1 — AdminDashboardPage

Simple dashboard showing counts: total active employees, pending leaves, total stores, today's attendance records. Admin entry point.

**Files:**
- Create: `lms_web/src/features/admin/dashboard/pages/AdminDashboardPage.tsx`
- Modify: `lms_web/src/features/admin/pages/AdminDashboardPage.tsx` (convert to re-export)

- [ ] **Step 1: Implement dashboard page**

Create `lms_web/src/features/admin/dashboard/pages/AdminDashboardPage.tsx`:

```tsx
import { Card, CardContent } from '@/components/ui/card'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { LeaveListResponse } from '@/features/leave/types'
import type { AttendanceListResponse } from '@/features/attendance/types'
import { formatDate, formatDateKorean } from '@/lib/utils/date'
import { useAuthStore } from '@/features/auth/store'

interface EmployeeListResponse { employees: { id: string; isActive: boolean; storeId?: string }[]; totalCount: number }
interface StoreListResponse { stores: unknown[]; totalCount: number }

export default function AdminDashboardPage() {
  const role = useAuthStore((s) => s.currentUser?.role)

  const pendingLeaves = useQuery({
    queryKey: ['admin', 'dashboard', 'pending-leaves'],
    queryFn: async () => {
      const res = await api.get<LeaveListResponse>(endpoints.leaves.pending)
      return res.data
    },
  })

  const employees = useQuery({
    queryKey: ['admin', 'dashboard', 'employees'],
    queryFn: async () => {
      const res = await api.get<EmployeeListResponse>(endpoints.employees.list, { params: { activeOnly: true } })
      return res.data
    },
  })

  const stores = useQuery({
    queryKey: ['admin', 'dashboard', 'stores'],
    queryFn: async () => {
      const res = await api.get<StoreListResponse>(endpoints.stores.list)
      return res.data
    },
    enabled: role === 'SUPER_ADMIN',
  })

  const today = formatDate(new Date())
  const activeCount = employees.data?.employees.filter((e) => e.isActive).length ?? 0
  const pendingCount = pendingLeaves.data?.requests.length ?? 0
  const storeCount = stores.data?.totalCount ?? 0

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">관리자 대시보드</h1>
        <p className="text-sm text-muted-foreground">{formatDateKorean(new Date())}</p>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="활성 직원" value={activeCount} loading={employees.isLoading} />
        <StatCard title="대기 휴가" value={pendingCount} loading={pendingLeaves.isLoading} link="/admin/leaves" />
        {role === 'SUPER_ADMIN' && <StatCard title="매장 수" value={storeCount} loading={stores.isLoading} link="/admin/stores" />}
        <StatCard title="오늘" value={today} loading={false} small />
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <QuickLink to="/admin/employees" label="직원 관리" />
        <QuickLink to="/admin/schedules" label="근무 일정 관리" />
        <QuickLink to="/admin/attendance" label="근태 관리" />
        <QuickLink to="/admin/leaves" label="휴가 승인" />
        <QuickLink to="/admin/payroll" label="급여 관리" />
        {role === 'SUPER_ADMIN' && <QuickLink to="/admin/payroll/policies" label="급여 정책" />}
      </div>
    </div>
  )
}

interface StatCardProps { title: string; value: string | number; loading: boolean; link?: string; small?: boolean }
function StatCard({ title, value, loading, link, small }: StatCardProps) {
  const inner = (
    <Card className={link ? 'transition-colors hover:bg-accent' : ''}>
      <CardContent className="p-4">
        <div className="text-xs text-muted-foreground">{title}</div>
        <div className={small ? 'mt-1 text-base font-semibold' : 'mt-1 text-2xl font-semibold'}>
          {loading ? '—' : value}
        </div>
      </CardContent>
    </Card>
  )
  return link ? <Link to={link}>{inner}</Link> : inner
}

function QuickLink({ to, label }: { to: string; label: string }) {
  return (
    <Link to={to} className="rounded-md border p-4 text-sm transition-colors hover:bg-accent">
      → {label}
    </Link>
  )
}
```

- [ ] **Step 2: Convert existing placeholder to re-export**

Overwrite `lms_web/src/features/admin/pages/AdminDashboardPage.tsx`:

```tsx
export { default } from '@/features/admin/dashboard/pages/AdminDashboardPage'
```

- [ ] **Step 3: Typecheck**

```bash
cd lms_web && npm run typecheck
```

- [ ] **Step 4: Commit**

```bash
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): AdminDashboardPage with stat cards (employees/pending leaves/stores) and quick links"
```

---

## Task 4.2 — LeaveManagementPage (pending approve/reject)

Pending leaves list with per-row approve / reject actions. Reject opens a dialog to capture rejection reason.

**Files:**
- Create: `lms_web/src/features/admin/leaves/api.ts`
- Create: `lms_web/src/features/admin/leaves/schema.ts`
- Create: `lms_web/src/features/admin/leaves/pages/LeaveManagementPage.tsx`
- Modify: `lms_web/src/features/admin/pages/LeaveManagementPage.tsx` (re-export)

- [ ] **Step 1: Admin leave API**

Create `lms_web/src/features/admin/leaves/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { LeaveListResponse, LeaveRequest } from '@/features/leave/types'

export const adminLeaveKeys = {
  all: ['admin-leaves'] as const,
  pending: () => [...adminLeaveKeys.all, 'pending'] as const,
}

export function usePendingLeaves() {
  return useQuery({
    queryKey: adminLeaveKeys.pending(),
    queryFn: async () => {
      const res = await api.get<LeaveListResponse>(endpoints.leaves.pending)
      return res.data
    },
  })
}

export function useApproveLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await api.patch<LeaveRequest>(endpoints.leaves.approve(id))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminLeaveKeys.all }),
  })
}

export function useRejectLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; rejectionReason: string }) => {
      const res = await api.patch<LeaveRequest>(endpoints.leaves.reject(args.id), {
        rejectionReason: args.rejectionReason,
      })
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminLeaveKeys.all }),
  })
}
```

- [ ] **Step 2: Rejection schema**

Create `lms_web/src/features/admin/leaves/schema.ts`:

```ts
import { z } from 'zod'

export const LeaveRejectionSchema = z.object({
  rejectionReason: z.string().min(1, '반려 사유를 입력해주세요.').max(500, '500자 이하'),
})

export type LeaveRejectionInput = z.infer<typeof LeaveRejectionSchema>
```

- [ ] **Step 3: LeaveManagementPage**

Create `lms_web/src/features/admin/leaves/pages/LeaveManagementPage.tsx`:

```tsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,
} from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { usePendingLeaves, useApproveLeave, useRejectLeave } from '../api'
import { LeaveRejectionSchema, type LeaveRejectionInput } from '../schema'
import { leaveTypeLabel } from '@/lib/utils/labels'
import { formatDateKorean } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'
import type { LeaveRequest } from '@/features/leave/types'

export default function LeaveManagementPage() {
  const query = usePendingLeaves()
  const approve = useApproveLeave()
  const reject = useRejectLeave()
  const [rejecting, setRejecting] = useState<LeaveRequest | null>(null)

  const form = useForm<LeaveRejectionInput>({
    resolver: zodResolver(LeaveRejectionSchema),
    defaultValues: { rejectionReason: '' },
  })

  const onApprove = (id: string) => {
    approve.mutate(id, {
      onSuccess: () => toast.success('승인되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  const onReject = form.handleSubmit((values) => {
    if (!rejecting) return
    reject.mutate({ id: rejecting.id, rejectionReason: values.rejectionReason }, {
      onSuccess: () => {
        toast.success('반려되었습니다.')
        setRejecting(null)
        form.reset()
      },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">휴가 승인 대기</h1>

      <QueryBoundary
        query={query}
        emptyMessage="승인 대기 중인 휴가가 없습니다."
        isEmpty={(d) => d.requests.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>직원</TableHead>
                <TableHead>유형</TableHead>
                <TableHead>기간</TableHead>
                <TableHead>일수</TableHead>
                <TableHead>사유</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.requests.map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="font-mono text-xs">{r.employeeId}</TableCell>
                  <TableCell>{leaveTypeLabel(r.leaveType)}</TableCell>
                  <TableCell className="text-xs">{formatDateKorean(r.startDate)} ~ {formatDateKorean(r.endDate)}</TableCell>
                  <TableCell>{r.requestedDays}</TableCell>
                  <TableCell className="max-w-[200px] truncate text-muted-foreground">{r.reason ?? '—'}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button size="sm" variant="outline" disabled={approve.isPending} onClick={() => onApprove(r.id)}>
                        승인
                      </Button>
                      <Button size="sm" variant="destructive" onClick={() => setRejecting(r)}>
                        반려
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!rejecting} onOpenChange={(open) => !open && setRejecting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>휴가 반려</DialogTitle>
          </DialogHeader>
          <Form {...form}>
            <form onSubmit={onReject} className="space-y-4">
              <FormField
                control={form.control}
                name="rejectionReason"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>반려 사유</FormLabel>
                    <FormControl><Textarea rows={4} {...field} /></FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {form.formState.errors.root && (
                <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>
              )}
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setRejecting(null)}>취소</Button>
                <Button type="submit" variant="destructive" disabled={reject.isPending}>
                  {reject.isPending ? '반려 중...' : '반려'}
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
```

- [ ] **Step 4: Convert placeholder**

Overwrite `lms_web/src/features/admin/pages/LeaveManagementPage.tsx`:

```tsx
export { default } from '@/features/admin/leaves/pages/LeaveManagementPage'
```

- [ ] **Step 5: Typecheck + build**

```bash
cd lms_web && npm run typecheck && npm run build
```

- [ ] **Step 6: Commit**

```bash
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): LeaveManagementPage with pending list + approve + reject dialog"
```

---

# Phase M4.B — CRUD admin pages (Store, Employee, PayrollPolicy)

These three pages share the AdminList + Dialog pattern referenced above. Each has its own schema/types/api then the list page uses the same skeleton.

## Task 4.3 — StoreManagementPage (SUPER_ADMIN)

**Files:**
- Create: `lms_web/src/features/admin/stores/{types.ts, schema.ts, api.ts, pages/StoreManagementPage.tsx}`
- Modify: `lms_web/src/features/admin/pages/StoreManagementPage.tsx` (re-export)

- [ ] **Step 1: Create types**

Create `lms_web/src/features/admin/stores/types.ts`:

```ts
export interface Store {
  id: string
  name: string
  location: string
  createdAt: string
}

export interface StoreListResponse {
  stores: Store[]
  totalCount: number
}
```

- [ ] **Step 2: Create schema**

Create `lms_web/src/features/admin/stores/schema.ts`:

```ts
import { z } from 'zod'

export const StoreSchema = z.object({
  name: z.string().min(1, '매장 이름을 입력해주세요.').max(100),
  location: z.string().min(1, '위치를 입력해주세요.').max(200),
})

export type StoreInput = z.infer<typeof StoreSchema>
```

- [ ] **Step 3: Create API hooks**

Create `lms_web/src/features/admin/stores/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { Store, StoreListResponse } from './types'
import type { StoreInput } from './schema'

export const storeKeys = {
  all: ['stores'] as const,
  list: () => [...storeKeys.all, 'list'] as const,
}

export function useStores() {
  return useQuery({
    queryKey: storeKeys.list(),
    queryFn: async () => {
      const res = await api.get<StoreListResponse>(endpoints.stores.list)
      return res.data
    },
  })
}

export function useCreateStore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: StoreInput) => {
      const res = await api.post<Store>(endpoints.stores.create, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: storeKeys.all }),
  })
}

export function useUpdateStore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: StoreInput }) => {
      const res = await api.put<Store>(endpoints.stores.update(args.id), args.input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: storeKeys.all }),
  })
}

export function useDeleteStore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(endpoints.stores.delete(id))
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: storeKeys.all }),
  })
}
```

- [ ] **Step 4: StoreManagementPage**

Create `lms_web/src/features/admin/stores/pages/StoreManagementPage.tsx`:

```tsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { MoreHorizontal } from 'lucide-react'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useStores, useCreateStore, useUpdateStore, useDeleteStore } from '../api'
import { StoreSchema, type StoreInput } from '../schema'
import type { Store } from '../types'
import { getErrorMessage } from '@/lib/utils/errors'

export default function StoreManagementPage() {
  const query = useStores()
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<Store | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">매장 관리</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button>+ 매장 추가</Button>
          </DialogTrigger>
          <StoreFormDialog title="신규 매장" mode="create" onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="등록된 매장이 없습니다."
        isEmpty={(d) => d.stores.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>이름</TableHead>
                <TableHead>위치</TableHead>
                <TableHead className="w-[80px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.stores.map((s) => (
                <TableRow key={s.id}>
                  <TableCell className="font-medium">{s.name}</TableCell>
                  <TableCell className="text-muted-foreground">{s.location}</TableCell>
                  <TableCell>
                    <RowActions store={s} onEdit={() => setEditItem(s)} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <StoreFormDialog title="매장 수정" mode="edit" initial={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ store, onEdit }: { store: Store; onEdit: () => void }) {
  const del = useDeleteStore()
  const onDelete = () => {
    if (!confirm(`${store.name} 매장을 삭제하시겠습니까?`)) return
    del.mutate(store.id, {
      onSuccess: () => toast.success('삭제되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon"><MoreHorizontal className="h-4 w-4" /></Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDelete} className="text-destructive">삭제</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

interface FormDialogProps {
  title: string
  mode: 'create' | 'edit'
  initial?: Store
  onDone: () => void
}
function StoreFormDialog({ title, mode, initial, onDone }: FormDialogProps) {
  const create = useCreateStore()
  const update = useUpdateStore()

  const form = useForm<StoreInput>({
    resolver: zodResolver(StoreSchema),
    defaultValues: { name: initial?.name ?? '', location: initial?.location ?? '' },
  })

  const onSubmit = form.handleSubmit((values) => {
    const mutation = mode === 'create' ? create : update
    const payload = mode === 'create' ? values : { id: initial!.id, input: values }
    mutation.mutate(payload as never, {
      onSuccess: () => { toast.success(mode === 'create' ? '매장이 추가되었습니다.' : '매장이 수정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })

  const busy = mode === 'create' ? create.isPending : update.isPending

  return (
    <DialogContent>
      <DialogHeader><DialogTitle>{title}</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="name" render={({ field }) => (
            <FormItem>
              <FormLabel>매장 이름</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="location" render={({ field }) => (
            <FormItem>
              <FormLabel>위치</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={busy}>{busy ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
```

- [ ] **Step 5: Convert placeholder**

Overwrite `lms_web/src/features/admin/pages/StoreManagementPage.tsx`:

```tsx
export { default } from '@/features/admin/stores/pages/StoreManagementPage'
```

- [ ] **Step 6: Typecheck + commit**

```bash
cd lms_web && npm run typecheck
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): StoreManagementPage with CRUD dialog (SUPER_ADMIN)"
```

---

## Task 4.4 — EmployeeManagementPage

Similar pattern to Store but with more fields: name, employeeType (Select), storeId (Select loaded from stores). Deactivate replaces delete. Create requires `userId` input.

**Files:**
- Create: `lms_web/src/features/admin/employees/{types.ts, schema.ts, api.ts, pages/EmployeeManagementPage.tsx}`
- Modify: `lms_web/src/features/admin/pages/EmployeeManagementPage.tsx` (re-export)

- [ ] **Step 1: Create types**

Create `lms_web/src/features/admin/employees/types.ts`:

```ts
export type EmployeeTypeValue = 'REGULAR' | 'IRREGULAR' | 'PART_TIME'

export interface AdminEmployee {
  id: string
  userId: string
  name: string
  employeeType: EmployeeTypeValue
  storeId: string | null
  remainingLeave: number
  isActive: boolean
  createdAt: string
}

export interface AdminEmployeeListResponse {
  employees: AdminEmployee[]
  totalCount: number
}
```

- [ ] **Step 2: Create schema**

Create `lms_web/src/features/admin/employees/schema.ts`:

```ts
import { z } from 'zod'

const EMPLOYEE_TYPES = ['REGULAR', 'IRREGULAR', 'PART_TIME'] as const

export const EmployeeCreateSchema = z.object({
  userId: z.string().min(1, 'userId를 입력해주세요.').max(100),
  name: z.string().min(1, '이름을 입력해주세요.').max(100),
  employeeType: z.enum(EMPLOYEE_TYPES),
  storeId: z.string().optional(),
})

export const EmployeeUpdateSchema = EmployeeCreateSchema.omit({ userId: true })

export type EmployeeCreateInput = z.infer<typeof EmployeeCreateSchema>
export type EmployeeUpdateInput = z.infer<typeof EmployeeUpdateSchema>
```

- [ ] **Step 3: Create API hooks**

Create `lms_web/src/features/admin/employees/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { AdminEmployee, AdminEmployeeListResponse } from './types'
import type { EmployeeCreateInput, EmployeeUpdateInput } from './schema'

export const employeeKeys = {
  all: ['employees'] as const,
  list: (filters: { storeId?: string; activeOnly?: boolean }) =>
    [...employeeKeys.all, 'list', filters] as const,
}

export function useEmployees(filters: { storeId?: string; activeOnly?: boolean } = {}) {
  return useQuery({
    queryKey: employeeKeys.list(filters),
    queryFn: async () => {
      const res = await api.get<AdminEmployeeListResponse>(endpoints.employees.list, { params: filters })
      return res.data
    },
  })
}

export function useCreateEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: EmployeeCreateInput) => {
      const res = await api.post<AdminEmployee>(endpoints.employees.create, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}

export function useUpdateEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: EmployeeUpdateInput }) => {
      const res = await api.put<AdminEmployee>(endpoints.employees.update(args.id), args.input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}

export function useDeactivateEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await api.patch<AdminEmployee>(endpoints.employees.deactivate(id))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}
```

- [ ] **Step 4: EmployeeManagementPage**

Create `lms_web/src/features/admin/employees/pages/EmployeeManagementPage.tsx`:

```tsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { MoreHorizontal } from 'lucide-react'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import {
  useEmployees, useCreateEmployee, useUpdateEmployee, useDeactivateEmployee,
} from '../api'
import {
  EmployeeCreateSchema, EmployeeUpdateSchema,
  type EmployeeCreateInput, type EmployeeUpdateInput,
} from '../schema'
import type { AdminEmployee } from '../types'
import { useStores } from '@/features/admin/stores/api'
import { employeeTypeLabel, employeeTypeLabels } from '@/lib/utils/labels'
import { getErrorMessage } from '@/lib/utils/errors'

const employeeTypes = Object.keys(employeeTypeLabels) as Array<keyof typeof employeeTypeLabels>

export default function EmployeeManagementPage() {
  const query = useEmployees()
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<AdminEmployee | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">직원 관리</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild><Button>+ 직원 추가</Button></DialogTrigger>
          <CreateDialog onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="등록된 직원이 없습니다."
        isEmpty={(d) => d.employees.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>이름</TableHead>
                <TableHead>유형</TableHead>
                <TableHead>매장</TableHead>
                <TableHead>남은 연차</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="w-[80px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.employees.map((e) => (
                <TableRow key={e.id}>
                  <TableCell className="font-medium">{e.name}</TableCell>
                  <TableCell>{employeeTypeLabel(e.employeeType)}</TableCell>
                  <TableCell className="font-mono text-xs">{e.storeId ?? '—'}</TableCell>
                  <TableCell>{e.remainingLeave}</TableCell>
                  <TableCell>
                    {e.isActive
                      ? <Badge variant="outline">활성</Badge>
                      : <Badge variant="secondary">비활성</Badge>}
                  </TableCell>
                  <TableCell>
                    {e.isActive && <RowActions emp={e} onEdit={() => setEditItem(e)} />}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <EditDialog emp={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ emp, onEdit }: { emp: AdminEmployee; onEdit: () => void }) {
  const deact = useDeactivateEmployee()
  const onDeactivate = () => {
    if (!confirm(`${emp.name}을(를) 비활성화하시겠습니까?`)) return
    deact.mutate(emp.id, {
      onSuccess: () => toast.success('비활성화되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon"><MoreHorizontal className="h-4 w-4" /></Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDeactivate} className="text-destructive">비활성화</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

function StoreSelect({ value, onChange }: { value?: string; onChange: (v: string | undefined) => void }) {
  const stores = useStores()
  return (
    <Select value={value ?? '__none'} onValueChange={(v) => onChange(v === '__none' ? undefined : v)}>
      <SelectTrigger><SelectValue placeholder="매장 선택" /></SelectTrigger>
      <SelectContent>
        <SelectItem value="__none">(없음)</SelectItem>
        {stores.data?.stores.map((s) => (
          <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}

function CreateDialog({ onDone }: { onDone: () => void }) {
  const create = useCreateEmployee()
  const form = useForm<EmployeeCreateInput>({
    resolver: zodResolver(EmployeeCreateSchema),
    defaultValues: { userId: '', name: '', employeeType: 'REGULAR', storeId: undefined },
  })
  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('직원이 등록되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>신규 직원</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="userId" render={({ field }) => (
            <FormItem>
              <FormLabel>사용자 ID</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="name" render={({ field }) => (
            <FormItem>
              <FormLabel>이름</FormLabel>
              <FormControl><Input {...field} /></FormControl>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="employeeType" render={({ field }) => (
            <FormItem>
              <FormLabel>유형</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                <SelectContent>
                  {employeeTypes.map((t) => (
                    <SelectItem key={t} value={t}>{employeeTypeLabel(t)}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="storeId" render={({ field }) => (
            <FormItem>
              <FormLabel>매장</FormLabel>
              <StoreSelect value={field.value} onChange={field.onChange} />
              <FormMessage />
            </FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={create.isPending}>{create.isPending ? '등록 중...' : '등록'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}

function EditDialog({ emp, onDone }: { emp: AdminEmployee; onDone: () => void }) {
  const update = useUpdateEmployee()
  const form = useForm<EmployeeUpdateInput>({
    resolver: zodResolver(EmployeeUpdateSchema),
    defaultValues: { name: emp.name, employeeType: emp.employeeType, storeId: emp.storeId ?? undefined },
  })
  const onSubmit = form.handleSubmit((values) => {
    update.mutate({ id: emp.id, input: values }, {
      onSuccess: () => { toast.success('직원 정보가 수정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>직원 수정</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="name" render={({ field }) => (
            <FormItem><FormLabel>이름</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="employeeType" render={({ field }) => (
            <FormItem>
              <FormLabel>유형</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                <SelectContent>
                  {employeeTypes.map((t) => (<SelectItem key={t} value={t}>{employeeTypeLabel(t)}</SelectItem>))}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="storeId" render={({ field }) => (
            <FormItem><FormLabel>매장</FormLabel><StoreSelect value={field.value} onChange={field.onChange} /><FormMessage /></FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={update.isPending}>{update.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
```

- [ ] **Step 5: Convert placeholder**

Overwrite `lms_web/src/features/admin/pages/EmployeeManagementPage.tsx`:

```tsx
export { default } from '@/features/admin/employees/pages/EmployeeManagementPage'
```

- [ ] **Step 6: Typecheck + commit**

```bash
cd lms_web && npm run typecheck
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): EmployeeManagementPage with create/edit/deactivate + active badge"
```

---

## Task 4.5 — PayrollPolicyPage (SUPER_ADMIN)

Similar CRUD pattern. Multiplier input (number, 0–10), policyType Select, effectiveFrom required / effectiveTo optional.

**Files:**
- Create: `lms_web/src/features/admin/policies/{types.ts, schema.ts, api.ts, pages/PayrollPolicyPage.tsx}`
- Modify: `lms_web/src/features/admin/pages/PayrollPolicyPage.tsx` (re-export)

- [ ] **Step 1: Types**

Create `lms_web/src/features/admin/policies/types.ts`:

```ts
export type PolicyTypeValue =
  | 'OVERTIME_WEEKDAY' | 'OVERTIME_WEEKEND' | 'OVERTIME_HOLIDAY'
  | 'NIGHT_SHIFT' | 'HOLIDAY_WORK' | 'BONUS' | 'ALLOWANCE'

export interface PayrollPolicy {
  id: string
  policyType: PolicyTypeValue
  policyTypeDescription: string
  multiplier: number
  effectiveFrom: string
  effectiveTo: string | null
  description: string | null
  isCurrentlyEffective: boolean
  createdAt: string
}

export interface PayrollPolicyListResponse {
  policies: PayrollPolicy[]
  totalCount: number
}
```

- [ ] **Step 2: Schema**

Create `lms_web/src/features/admin/policies/schema.ts`:

```ts
import { z } from 'zod'

const POLICY_TYPES = ['OVERTIME_WEEKDAY','OVERTIME_WEEKEND','OVERTIME_HOLIDAY','NIGHT_SHIFT','HOLIDAY_WORK','BONUS','ALLOWANCE'] as const

export const PolicyCreateSchema = z.object({
  policyType: z.enum(POLICY_TYPES),
  multiplier: z.coerce.number().min(0).max(10),
  effectiveFrom: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '시작일을 선택하세요.'),
  effectiveTo: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional().or(z.literal('')),
  description: z.string().max(500).optional(),
})

export const PolicyUpdateSchema = z.object({
  multiplier: z.coerce.number().min(0).max(10).optional(),
  effectiveTo: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional().or(z.literal('')),
  description: z.string().max(500).optional(),
})

export type PolicyCreateInput = z.infer<typeof PolicyCreateSchema>
export type PolicyUpdateInput = z.infer<typeof PolicyUpdateSchema>
```

- [ ] **Step 3: API hooks**

Create `lms_web/src/features/admin/policies/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { PayrollPolicy, PayrollPolicyListResponse } from './types'
import type { PolicyCreateInput, PolicyUpdateInput } from './schema'

export const policyKeys = {
  all: ['policies'] as const,
  list: () => [...policyKeys.all, 'list'] as const,
}

function normalize(input: PolicyCreateInput | PolicyUpdateInput) {
  const payload: Record<string, unknown> = { ...input }
  if (payload.effectiveTo === '') delete payload.effectiveTo
  if (payload.description === '') delete payload.description
  return payload
}

export function usePolicies() {
  return useQuery({
    queryKey: policyKeys.list(),
    queryFn: async () => {
      const res = await api.get<PayrollPolicyListResponse>(endpoints.policies.list)
      return res.data
    },
  })
}

export function useCreatePolicy() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: PolicyCreateInput) => {
      const res = await api.post<PayrollPolicy>(endpoints.policies.create, normalize(input))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: policyKeys.all }),
  })
}

export function useUpdatePolicy() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: PolicyUpdateInput }) => {
      const res = await api.put<PayrollPolicy>(endpoints.policies.update(args.id), normalize(args.input))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: policyKeys.all }),
  })
}

export function useDeletePolicy() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => { await api.delete(endpoints.policies.delete(id)) },
    onSuccess: () => qc.invalidateQueries({ queryKey: policyKeys.all }),
  })
}
```

- [ ] **Step 4: PayrollPolicyPage**

Create `lms_web/src/features/admin/policies/pages/PayrollPolicyPage.tsx`:

```tsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { MoreHorizontal } from 'lucide-react'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import {
  usePolicies, useCreatePolicy, useUpdatePolicy, useDeletePolicy,
} from '../api'
import {
  PolicyCreateSchema, PolicyUpdateSchema,
  type PolicyCreateInput, type PolicyUpdateInput,
} from '../schema'
import type { PayrollPolicy } from '../types'
import { policyTypeLabel, policyTypeLabels } from '@/lib/utils/labels'
import { formatDateKorean } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'

const policyTypes = Object.keys(policyTypeLabels) as Array<keyof typeof policyTypeLabels>

export default function PayrollPolicyPage() {
  const query = usePolicies()
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<PayrollPolicy | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">급여 정책</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild><Button>+ 정책 추가</Button></DialogTrigger>
          <CreateDialog onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <QueryBoundary
        query={query}
        emptyMessage="등록된 정책이 없습니다."
        isEmpty={(d) => d.policies.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>유형</TableHead>
                <TableHead>배수</TableHead>
                <TableHead>효력 기간</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="w-[80px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.policies.map((p) => (
                <TableRow key={p.id}>
                  <TableCell>{policyTypeLabel(p.policyType)}</TableCell>
                  <TableCell>× {p.multiplier}</TableCell>
                  <TableCell className="text-xs">
                    {formatDateKorean(p.effectiveFrom)} ~ {p.effectiveTo ? formatDateKorean(p.effectiveTo) : '(무기한)'}
                  </TableCell>
                  <TableCell>
                    {p.isCurrentlyEffective
                      ? <Badge variant="outline">적용 중</Badge>
                      : <Badge variant="secondary">비적용</Badge>}
                  </TableCell>
                  <TableCell>
                    <RowActions policy={p} onEdit={() => setEditItem(p)} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <EditDialog policy={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ policy, onEdit }: { policy: PayrollPolicy; onEdit: () => void }) {
  const del = useDeletePolicy()
  const onDelete = () => {
    if (!confirm('이 정책을 삭제하시겠습니까?')) return
    del.mutate(policy.id, {
      onSuccess: () => toast.success('삭제되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild><Button variant="ghost" size="icon"><MoreHorizontal className="h-4 w-4" /></Button></DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDelete} className="text-destructive">삭제</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

function CreateDialog({ onDone }: { onDone: () => void }) {
  const create = useCreatePolicy()
  const form = useForm<PolicyCreateInput>({
    resolver: zodResolver(PolicyCreateSchema),
    defaultValues: { policyType: 'OVERTIME_WEEKDAY', multiplier: 1.5, effectiveFrom: '', effectiveTo: '', description: '' },
  })
  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('정책이 추가되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>신규 정책</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="policyType" render={({ field }) => (
            <FormItem>
              <FormLabel>정책 유형</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                <SelectContent>
                  {policyTypes.map((t) => <SelectItem key={t} value={t}>{policyTypeLabel(t)}</SelectItem>)}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="multiplier" render={({ field }) => (
            <FormItem><FormLabel>배수 (0.0 ~ 10.0)</FormLabel><FormControl><Input type="number" step="0.1" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="effectiveFrom" render={({ field }) => (
            <FormItem><FormLabel>시작일</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="effectiveTo" render={({ field }) => (
            <FormItem><FormLabel>종료일 (선택)</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="description" render={({ field }) => (
            <FormItem><FormLabel>설명 (선택)</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={create.isPending}>{create.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}

function EditDialog({ policy, onDone }: { policy: PayrollPolicy; onDone: () => void }) {
  const update = useUpdatePolicy()
  const form = useForm<PolicyUpdateInput>({
    resolver: zodResolver(PolicyUpdateSchema),
    defaultValues: { multiplier: policy.multiplier, effectiveTo: policy.effectiveTo ?? '', description: policy.description ?? '' },
  })
  const onSubmit = form.handleSubmit((values) => {
    update.mutate({ id: policy.id, input: values }, {
      onSuccess: () => { toast.success('정책이 수정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>정책 수정</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="text-sm text-muted-foreground">유형: {policyTypeLabel(policy.policyType)} (변경 불가)</div>
          <FormField control={form.control} name="multiplier" render={({ field }) => (
            <FormItem><FormLabel>배수</FormLabel><FormControl><Input type="number" step="0.1" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="effectiveTo" render={({ field }) => (
            <FormItem><FormLabel>종료일</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <FormField control={form.control} name="description" render={({ field }) => (
            <FormItem><FormLabel>설명</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={update.isPending}>{update.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
```

- [ ] **Step 5: Convert placeholder + commit**

Overwrite `lms_web/src/features/admin/pages/PayrollPolicyPage.tsx`:

```tsx
export { default } from '@/features/admin/policies/pages/PayrollPolicyPage'
```

```bash
cd lms_web && npm run typecheck
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): PayrollPolicyPage with CRUD dialogs (SUPER_ADMIN)"
```

---

# Phase M4.C — Operational admin pages

## Task 4.6 — ScheduleManagementPage

Filter by store + date range. Create/edit/delete schedules. Uses existing `features/schedule/types.ts` for `WorkSchedule`.

**Files:**
- Create: `lms_web/src/features/admin/schedules/{api.ts, schema.ts, pages/ScheduleManagementPage.tsx}`
- Modify: `lms_web/src/features/admin/pages/ScheduleManagementPage.tsx` (re-export)

- [ ] **Step 1: Schema**

Create `lms_web/src/features/admin/schedules/schema.ts`:

```ts
import { z } from 'zod'

export const ScheduleCreateSchema = z.object({
  employeeId: z.string().min(1, '직원을 선택하세요.'),
  storeId: z.string().min(1, '매장을 선택하세요.'),
  workDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '날짜를 선택하세요.'),
  startTime: z.string().regex(/^\d{2}:\d{2}$/, 'HH:mm 형식'),
  endTime: z.string().regex(/^\d{2}:\d{2}$/, 'HH:mm 형식'),
})

export const ScheduleUpdateSchema = z.object({
  workDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
  startTime: z.string().regex(/^\d{2}:\d{2}$/).optional(),
  endTime: z.string().regex(/^\d{2}:\d{2}$/).optional(),
})

export type ScheduleCreateInput = z.infer<typeof ScheduleCreateSchema>
export type ScheduleUpdateInput = z.infer<typeof ScheduleUpdateSchema>

// Convert 'HH:mm' to 'HH:mm:ss' for backend
export function toBackendTime(v: string): string { return v.length === 5 ? v + ':00' : v }
```

- [ ] **Step 2: API hooks**

Create `lms_web/src/features/admin/schedules/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { WorkSchedule, ScheduleListResponse } from '@/features/schedule/types'
import { toBackendTime, type ScheduleCreateInput, type ScheduleUpdateInput } from './schema'

export const adminScheduleKeys = {
  all: ['admin-schedules'] as const,
  list: (filters: object) => [...adminScheduleKeys.all, 'list', filters] as const,
}

export function useAdminSchedules(filters: { storeId?: string; employeeId?: string; startDate?: string; endDate?: string }) {
  return useQuery({
    queryKey: adminScheduleKeys.list(filters),
    queryFn: async () => {
      const res = await api.get<ScheduleListResponse>(endpoints.schedules.list, { params: filters })
      return res.data
    },
    enabled: !!(filters.storeId || filters.employeeId),
  })
}

export function useCreateSchedule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: ScheduleCreateInput) => {
      const payload = { ...input, startTime: toBackendTime(input.startTime), endTime: toBackendTime(input.endTime) }
      const res = await api.post<WorkSchedule>(endpoints.schedules.list, payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminScheduleKeys.all }),
  })
}

export function useUpdateSchedule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: ScheduleUpdateInput }) => {
      const payload: Record<string, unknown> = { ...args.input }
      if (payload.startTime) payload.startTime = toBackendTime(payload.startTime as string)
      if (payload.endTime) payload.endTime = toBackendTime(payload.endTime as string)
      const res = await api.put<WorkSchedule>(endpoints.schedules.detail(args.id), payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminScheduleKeys.all }),
  })
}

export function useDeleteSchedule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => { await api.delete(endpoints.schedules.detail(id)) },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminScheduleKeys.all }),
  })
}
```

- [ ] **Step 3: ScheduleManagementPage**

Create `lms_web/src/features/admin/schedules/pages/ScheduleManagementPage.tsx`:

```tsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { MoreHorizontal } from 'lucide-react'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import {
  useAdminSchedules, useCreateSchedule, useUpdateSchedule, useDeleteSchedule,
} from '../api'
import {
  ScheduleCreateSchema, ScheduleUpdateSchema,
  type ScheduleCreateInput, type ScheduleUpdateInput,
} from '../schema'
import { useStores } from '@/features/admin/stores/api'
import { useEmployees } from '@/features/admin/employees/api'
import type { WorkSchedule } from '@/features/schedule/types'
import { formatDate, formatDateKorean, formatTime } from '@/lib/utils/date'
import { getErrorMessage } from '@/lib/utils/errors'

function firstOfMonth(d: Date): Date { const x = new Date(d); x.setDate(1); return x }

export default function ScheduleManagementPage() {
  const [storeId, setStoreId] = useState<string | undefined>(undefined)
  const today = new Date()
  const [startDate, setStartDate] = useState(formatDate(firstOfMonth(today)))
  const [endDate, setEndDate] = useState(formatDate(new Date(today.getFullYear(), today.getMonth() + 1, 0)))
  const stores = useStores()
  const query = useAdminSchedules({ storeId, startDate, endDate })
  const [createOpen, setCreateOpen] = useState(false)
  const [editItem, setEditItem] = useState<WorkSchedule | null>(null)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">근무 일정 관리</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild><Button>+ 일정 추가</Button></DialogTrigger>
          <CreateDialog onDone={() => setCreateOpen(false)} />
        </Dialog>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="space-y-1">
          <Label>매장</Label>
          <Select value={storeId ?? '__all'} onValueChange={(v) => setStoreId(v === '__all' ? undefined : v)}>
            <SelectTrigger><SelectValue placeholder="매장 선택" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="__all">(전체)</SelectItem>
              {stores.data?.stores.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1">
          <Label>시작일</Label>
          <Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="space-y-1">
          <Label>종료일</Label>
          <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
      </div>

      {!storeId ? (
        <p className="text-center text-sm text-muted-foreground">매장을 선택해주세요.</p>
      ) : (
        <QueryBoundary
          query={query}
          emptyMessage="해당 기간 일정이 없습니다."
          isEmpty={(d) => d.schedules.length === 0}
        >
          {(data) => (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>직원</TableHead>
                  <TableHead>날짜</TableHead>
                  <TableHead>시간</TableHead>
                  <TableHead>시수</TableHead>
                  <TableHead className="w-[80px]"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.schedules.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell className="font-mono text-xs">{s.employeeId}</TableCell>
                    <TableCell>{formatDateKorean(s.workDate)}</TableCell>
                    <TableCell>{formatTime(s.startTime)} ~ {formatTime(s.endTime)}</TableCell>
                    <TableCell>{s.workHours}h</TableCell>
                    <TableCell>
                      <RowActions schedule={s} onEdit={() => setEditItem(s)} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </QueryBoundary>
      )}

      <Dialog open={!!editItem} onOpenChange={(o) => !o && setEditItem(null)}>
        {editItem && <EditDialog schedule={editItem} onDone={() => setEditItem(null)} />}
      </Dialog>
    </div>
  )
}

function RowActions({ schedule, onEdit }: { schedule: WorkSchedule; onEdit: () => void }) {
  const del = useDeleteSchedule()
  const onDelete = () => {
    if (!confirm(`${schedule.workDate} 일정을 삭제하시겠습니까?`)) return
    del.mutate(schedule.id, {
      onSuccess: () => toast.success('삭제되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild><Button variant="ghost" size="icon"><MoreHorizontal className="h-4 w-4" /></Button></DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={onEdit}>수정</DropdownMenuItem>
        <DropdownMenuItem onClick={onDelete} className="text-destructive">삭제</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

function CreateDialog({ onDone }: { onDone: () => void }) {
  const create = useCreateSchedule()
  const stores = useStores()
  const employees = useEmployees({ activeOnly: true })
  const form = useForm<ScheduleCreateInput>({
    resolver: zodResolver(ScheduleCreateSchema),
    defaultValues: { employeeId: '', storeId: '', workDate: '', startTime: '09:00', endTime: '18:00' },
  })
  const onSubmit = form.handleSubmit((values) => {
    create.mutate(values, {
      onSuccess: () => { toast.success('일정이 추가되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>신규 일정</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="employeeId" render={({ field }) => (
            <FormItem>
              <FormLabel>직원</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue placeholder="직원 선택" /></SelectTrigger></FormControl>
                <SelectContent>
                  {employees.data?.employees.map((e) => <SelectItem key={e.id} value={e.id}>{e.name}</SelectItem>)}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="storeId" render={({ field }) => (
            <FormItem>
              <FormLabel>매장</FormLabel>
              <Select value={field.value} onValueChange={field.onChange}>
                <FormControl><SelectTrigger><SelectValue placeholder="매장 선택" /></SelectTrigger></FormControl>
                <SelectContent>
                  {stores.data?.stores.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )} />
          <FormField control={form.control} name="workDate" render={({ field }) => (
            <FormItem><FormLabel>날짜</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <div className="grid grid-cols-2 gap-3">
            <FormField control={form.control} name="startTime" render={({ field }) => (
              <FormItem><FormLabel>시작시간</FormLabel><FormControl><Input type="time" {...field} /></FormControl><FormMessage /></FormItem>
            )} />
            <FormField control={form.control} name="endTime" render={({ field }) => (
              <FormItem><FormLabel>종료시간</FormLabel><FormControl><Input type="time" {...field} /></FormControl><FormMessage /></FormItem>
            )} />
          </div>
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={create.isPending}>{create.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}

function EditDialog({ schedule, onDone }: { schedule: WorkSchedule; onDone: () => void }) {
  const update = useUpdateSchedule()
  const form = useForm<ScheduleUpdateInput>({
    resolver: zodResolver(ScheduleUpdateSchema),
    defaultValues: {
      workDate: schedule.workDate,
      startTime: schedule.startTime.slice(0, 5),
      endTime: schedule.endTime.slice(0, 5),
    },
  })
  const onSubmit = form.handleSubmit((values) => {
    update.mutate({ id: schedule.id, input: values }, {
      onSuccess: () => { toast.success('일정이 수정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>일정 수정</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <FormField control={form.control} name="workDate" render={({ field }) => (
            <FormItem><FormLabel>날짜</FormLabel><FormControl><Input type="date" {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          <div className="grid grid-cols-2 gap-3">
            <FormField control={form.control} name="startTime" render={({ field }) => (
              <FormItem><FormLabel>시작시간</FormLabel><FormControl><Input type="time" {...field} /></FormControl><FormMessage /></FormItem>
            )} />
            <FormField control={form.control} name="endTime" render={({ field }) => (
              <FormItem><FormLabel>종료시간</FormLabel><FormControl><Input type="time" {...field} /></FormControl><FormMessage /></FormItem>
            )} />
          </div>
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={update.isPending}>{update.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
```

- [ ] **Step 4: Re-export + commit**

Overwrite `lms_web/src/features/admin/pages/ScheduleManagementPage.tsx`:

```tsx
export { default } from '@/features/admin/schedules/pages/ScheduleManagementPage'
```

```bash
cd lms_web && npm run typecheck
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): ScheduleManagementPage with store/date filter + CRUD dialogs"
```

---

## Task 4.7 — AttendanceManagementPage (list + adjust)

Filter by store + date range. Adjust modal lets admin overwrite check-in/out time with a reason.

**Files:**
- Create: `lms_web/src/features/admin/attendance/{api.ts, schema.ts, pages/AttendanceManagementPage.tsx}`
- Modify: `lms_web/src/features/admin/pages/AttendanceManagementPage.tsx` (re-export)

- [ ] **Step 1: Schema**

Create `lms_web/src/features/admin/attendance/schema.ts`:

```ts
import { z } from 'zod'

// Backend accepts ISO-datetime (Instant). We combine attendanceDate + time (HH:mm) on submit.
export const AttendanceAdjustSchema = z.object({
  adjustedCheckInTime: z.string().regex(/^\d{2}:\d{2}$/, '출근 시간을 입력하세요.'),
  adjustedCheckOutTime: z.string().regex(/^\d{2}:\d{2}$/).optional().or(z.literal('')),
  reason: z.string().min(1, '조정 사유를 입력해주세요.').max(500),
})

export type AttendanceAdjustInput = z.infer<typeof AttendanceAdjustSchema>
```

- [ ] **Step 2: API hooks**

Create `lms_web/src/features/admin/attendance/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { AttendanceListResponse, AttendanceRecord } from '@/features/attendance/types'
import type { AttendanceAdjustInput } from './schema'

export const adminAttendanceKeys = {
  all: ['admin-attendance'] as const,
  records: (filters: object) => [...adminAttendanceKeys.all, 'records', filters] as const,
}

export function useStoreAttendance(filters: { storeId?: string; startDate?: string; endDate?: string }) {
  return useQuery({
    queryKey: adminAttendanceKeys.records(filters),
    queryFn: async () => {
      const res = await api.get<AttendanceListResponse>(endpoints.attendance.records, { params: filters })
      return res.data
    },
    enabled: !!filters.storeId,
  })
}

// Convert "YYYY-MM-DD" + "HH:mm" to ISO. Uses user local tz — backend accepts Instant.
function combineIso(date: string, time: string): string {
  const [y, m, d] = date.split('-').map(Number)
  const [hh, mm] = time.split(':').map(Number)
  return new Date(y, m - 1, d, hh, mm, 0).toISOString()
}

export function useAdjustAttendance() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { recordId: string; attendanceDate: string; input: AttendanceAdjustInput }) => {
      const payload: Record<string, unknown> = {
        adjustedCheckInTime: combineIso(args.attendanceDate, args.input.adjustedCheckInTime),
        reason: args.input.reason,
      }
      if (args.input.adjustedCheckOutTime) {
        payload.adjustedCheckOutTime = combineIso(args.attendanceDate, args.input.adjustedCheckOutTime)
      }
      const res = await api.put<AttendanceRecord>(endpoints.attendance.adjust(args.recordId), payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminAttendanceKeys.all }),
  })
}
```

- [ ] **Step 3: AttendanceManagementPage**

Create `lms_web/src/features/admin/attendance/pages/AttendanceManagementPage.tsx`:

```tsx
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,
} from '@/components/ui/dialog'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { useStoreAttendance, useAdjustAttendance } from '../api'
import { AttendanceAdjustSchema, type AttendanceAdjustInput } from '../schema'
import { useStores } from '@/features/admin/stores/api'
import type { AttendanceRecord } from '@/features/attendance/types'
import { formatDate, formatDateKorean, formatTime } from '@/lib/utils/date'
import { attendanceStatusLabel } from '@/lib/utils/labels'
import { formatHours } from '@/lib/utils/number'
import { getErrorMessage } from '@/lib/utils/errors'

function firstOfMonth(d: Date): Date { const x = new Date(d); x.setDate(1); return x }

export default function AttendanceManagementPage() {
  const [storeId, setStoreId] = useState<string | undefined>(undefined)
  const today = new Date()
  const [startDate, setStartDate] = useState(formatDate(firstOfMonth(today)))
  const [endDate, setEndDate] = useState(formatDate(today))
  const stores = useStores()
  const query = useStoreAttendance({ storeId, startDate, endDate })
  const [adjusting, setAdjusting] = useState<AttendanceRecord | null>(null)

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">근태 관리</h1>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="space-y-1">
          <Label>매장</Label>
          <Select value={storeId ?? '__all'} onValueChange={(v) => setStoreId(v === '__all' ? undefined : v)}>
            <SelectTrigger><SelectValue placeholder="매장 선택 (필수)" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="__all">(선택)</SelectItem>
              {stores.data?.stores.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-1"><Label>시작일</Label><Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} /></div>
        <div className="space-y-1"><Label>종료일</Label><Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} /></div>
      </div>

      {!storeId ? (
        <p className="text-center text-sm text-muted-foreground">매장을 선택해주세요.</p>
      ) : (
        <QueryBoundary
          query={query}
          emptyMessage="해당 기간 기록이 없습니다."
          isEmpty={(d) => d.records.length === 0}
        >
          {(data) => (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>직원</TableHead>
                  <TableHead>날짜</TableHead>
                  <TableHead>출근</TableHead>
                  <TableHead>퇴근</TableHead>
                  <TableHead>시수</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">작업</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.records.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className="font-mono text-xs">{r.employeeId}</TableCell>
                    <TableCell>{formatDateKorean(r.attendanceDate)}</TableCell>
                    <TableCell>{formatTime(r.checkInTime)}</TableCell>
                    <TableCell>{formatTime(r.checkOutTime)}</TableCell>
                    <TableCell>{formatHours(r.actualWorkHours)}</TableCell>
                    <TableCell className="text-xs">{attendanceStatusLabel(r.status)}</TableCell>
                    <TableCell className="text-right">
                      <Button size="sm" variant="outline" onClick={() => setAdjusting(r)}>조정</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </QueryBoundary>
      )}

      <Dialog open={!!adjusting} onOpenChange={(o) => !o && setAdjusting(null)}>
        {adjusting && <AdjustDialog record={adjusting} onDone={() => setAdjusting(null)} />}
      </Dialog>
    </div>
  )
}

function AdjustDialog({ record, onDone }: { record: AttendanceRecord; onDone: () => void }) {
  const adjust = useAdjustAttendance()
  const form = useForm<AttendanceAdjustInput>({
    resolver: zodResolver(AttendanceAdjustSchema),
    defaultValues: {
      adjustedCheckInTime: record.checkInTime.slice(0, 5),
      adjustedCheckOutTime: record.checkOutTime?.slice(0, 5) ?? '',
      reason: '',
    },
  })
  const onSubmit = form.handleSubmit((values) => {
    adjust.mutate({ recordId: record.id, attendanceDate: record.attendanceDate, input: values }, {
      onSuccess: () => { toast.success('조정되었습니다.'); onDone() },
      onError: (e) => form.setError('root', { message: getErrorMessage(e) }),
    })
  })
  return (
    <DialogContent>
      <DialogHeader><DialogTitle>근태 조정 — {formatDateKorean(record.attendanceDate)}</DialogTitle></DialogHeader>
      <Form {...form}>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <FormField control={form.control} name="adjustedCheckInTime" render={({ field }) => (
              <FormItem><FormLabel>출근</FormLabel><FormControl><Input type="time" {...field} /></FormControl><FormMessage /></FormItem>
            )} />
            <FormField control={form.control} name="adjustedCheckOutTime" render={({ field }) => (
              <FormItem><FormLabel>퇴근 (선택)</FormLabel><FormControl><Input type="time" {...field} /></FormControl><FormMessage /></FormItem>
            )} />
          </div>
          <FormField control={form.control} name="reason" render={({ field }) => (
            <FormItem><FormLabel>조정 사유</FormLabel><FormControl><Textarea rows={3} {...field} /></FormControl><FormMessage /></FormItem>
          )} />
          {form.formState.errors.root && <p className="text-sm text-destructive">{form.formState.errors.root.message}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onDone}>취소</Button>
            <Button type="submit" disabled={adjust.isPending}>{adjust.isPending ? '저장 중...' : '저장'}</Button>
          </DialogFooter>
        </form>
      </Form>
    </DialogContent>
  )
}
```

- [ ] **Step 4: Re-export + commit**

Overwrite `lms_web/src/features/admin/pages/AttendanceManagementPage.tsx`:

```tsx
export { default } from '@/features/admin/attendance/pages/AttendanceManagementPage'
```

```bash
cd lms_web && npm run typecheck
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): AttendanceManagementPage with store/date filter + adjust dialog"
```

---

## Task 4.8 — PayrollManagementPage (period + batch)

Period picker (YYYY-MM) + store filter (optional for SUPER_ADMIN). Shows list for period, plus "배치 실행" button (SUPER_ADMIN only) to trigger batch calculation.

**Files:**
- Create: `lms_web/src/features/admin/payroll/{api.ts, schema.ts, pages/PayrollManagementPage.tsx}`
- Modify: `lms_web/src/features/admin/pages/PayrollManagementPage.tsx` (re-export)

- [ ] **Step 1: Schema**

Create `lms_web/src/features/admin/payroll/schema.ts`:

```ts
import { z } from 'zod'

export const BatchExecuteSchema = z.object({
  period: z.string().regex(/^\d{4}-\d{2}$/, 'YYYY-MM 형식'),
  storeId: z.string().optional(),
})

export type BatchExecuteInput = z.infer<typeof BatchExecuteSchema>
```

- [ ] **Step 2: API hooks**

Create `lms_web/src/features/admin/payroll/api.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { Payroll } from '@/features/payroll/types'
import type { BatchExecuteInput } from './schema'

interface BatchHistoryResponse {
  id: string
  period: string
  storeId: string | null
  status: 'RUNNING' | 'COMPLETED' | 'PARTIAL_SUCCESS' | 'FAILED'
  totalCount: number
  successCount: number
  failureCount: number
  startedAt: string
  completedAt: string | null
  errorMessage: string | null
  createdAt: string
}

export const adminPayrollKeys = {
  all: ['admin-payroll'] as const,
  period: (period: string) => [...adminPayrollKeys.all, 'period', period] as const,
  batchHistory: () => [...adminPayrollKeys.all, 'batch-history'] as const,
}

export function usePayrollsByPeriod(period: string) {
  return useQuery({
    queryKey: adminPayrollKeys.period(period),
    queryFn: async () => {
      const res = await api.get<Payroll[]>(endpoints.payroll.list, { params: { period } })
      return res.data
    },
    enabled: /^\d{4}-\d{2}$/.test(period),
  })
}

export function useBatchHistory() {
  return useQuery({
    queryKey: adminPayrollKeys.batchHistory(),
    queryFn: async () => {
      const res = await api.get<BatchHistoryResponse[]>(endpoints.payroll.batchHistory)
      return res.data
    },
  })
}

export function useExecuteBatch() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: BatchExecuteInput) => {
      const payload = { period: input.period, ...(input.storeId ? { storeId: input.storeId } : {}) }
      const res = await api.post<BatchHistoryResponse>(endpoints.payroll.batch, payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminPayrollKeys.all }),
  })
}
```

- [ ] **Step 3: PayrollManagementPage**

Create `lms_web/src/features/admin/payroll/pages/PayrollManagementPage.tsx`:

```tsx
import { useState } from 'react'
import { toast } from 'sonner'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { QueryBoundary } from '@/components/layout/QueryBoundary'
import { usePayrollsByPeriod, useBatchHistory, useExecuteBatch } from '../api'
import { useStores } from '@/features/admin/stores/api'
import { useAuthStore } from '@/features/auth/store'
import { formatKRW } from '@/lib/utils/number'
import { batchStatusLabel } from '@/lib/utils/labels'
import { getErrorMessage } from '@/lib/utils/errors'

function currentPeriod(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

export default function PayrollManagementPage() {
  const role = useAuthStore((s) => s.currentUser?.role)
  const [period, setPeriod] = useState(currentPeriod())
  const [batchStoreId, setBatchStoreId] = useState<string | undefined>(undefined)
  const stores = useStores()
  const payrollQuery = usePayrollsByPeriod(period)
  const historyQuery = useBatchHistory()
  const execBatch = useExecuteBatch()

  const onExecute = () => {
    if (!confirm(`${period} 배치를 실행하시겠습니까?`)) return
    execBatch.mutate({ period, storeId: batchStoreId }, {
      onSuccess: () => toast.success('배치가 시작되었습니다.'),
      onError: (e) => toast.error(getErrorMessage(e)),
    })
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">급여 관리</h1>

      <Card>
        <CardHeader><CardTitle className="text-base">조회</CardTitle></CardHeader>
        <CardContent>
          <div className="flex items-end gap-3">
            <div className="space-y-1">
              <Label>기간 (YYYY-MM)</Label>
              <Input value={period} onChange={(e) => setPeriod(e.target.value)} placeholder="2026-04" className="w-32" />
            </div>
          </div>
        </CardContent>
      </Card>

      {role === 'SUPER_ADMIN' && (
        <Card>
          <CardHeader><CardTitle className="text-base">배치 실행</CardTitle></CardHeader>
          <CardContent>
            <div className="flex items-end gap-3">
              <div className="space-y-1">
                <Label>매장 (선택, 전체면 비움)</Label>
                <Select value={batchStoreId ?? '__all'} onValueChange={(v) => setBatchStoreId(v === '__all' ? undefined : v)}>
                  <SelectTrigger className="w-[200px]"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__all">(전체)</SelectItem>
                    {stores.data?.stores.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <Button onClick={onExecute} disabled={execBatch.isPending}>
                {execBatch.isPending ? '실행 중...' : '배치 실행'}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <QueryBoundary
        query={payrollQuery}
        emptyMessage="해당 기간 급여가 없습니다."
        isEmpty={(d) => d.length === 0}
      >
        {(data) => (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>직원</TableHead>
                <TableHead>기본급</TableHead>
                <TableHead>연장근무</TableHead>
                <TableHead>합계</TableHead>
                <TableHead>지급 상태</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-mono text-xs">{p.employeeId}</TableCell>
                  <TableCell>{formatKRW(p.baseAmount)}</TableCell>
                  <TableCell>{formatKRW(p.overtimeAmount)}</TableCell>
                  <TableCell className="font-medium">{formatKRW(p.totalAmount)}</TableCell>
                  <TableCell>
                    {p.isPaid ? <Badge variant="outline">지급 완료</Badge> : <Badge variant="secondary">미지급</Badge>}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </QueryBoundary>

      <Card>
        <CardHeader><CardTitle className="text-base">최근 배치 이력</CardTitle></CardHeader>
        <CardContent>
          <QueryBoundary
            query={historyQuery}
            emptyMessage="배치 이력이 없습니다."
            isEmpty={(d) => d.length === 0}
          >
            {(data) => (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>기간</TableHead>
                    <TableHead>매장</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>성공/실패</TableHead>
                    <TableHead>시작</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.slice(0, 10).map((h) => (
                    <TableRow key={h.id}>
                      <TableCell>{h.period}</TableCell>
                      <TableCell className="font-mono text-xs">{h.storeId ?? '(전체)'}</TableCell>
                      <TableCell>{batchStatusLabel(h.status)}</TableCell>
                      <TableCell>{h.successCount} / {h.failureCount}</TableCell>
                      <TableCell className="text-xs">{new Date(h.startedAt).toLocaleString('ko-KR')}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </QueryBoundary>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 4: Re-export + final commit**

Overwrite `lms_web/src/features/admin/pages/PayrollManagementPage.tsx`:

```tsx
export { default } from '@/features/admin/payroll/pages/PayrollManagementPage'
```

```bash
cd lms_web && npm run typecheck && npm run test:run && npm run build
git add lms_web/src/features/admin/
git commit -m "feat(lms_web): PayrollManagementPage with period query + batch execution (SUPER_ADMIN) + batch history"
```

---

## Task 4.9 — User-driven smoke test

Analogous to Plan 2's smoke. User executes against the live backend.

- [ ] **Step 1: Start services (PowerShell-friendly)**

```
T1: docker-compose up -d
T2: .\gradlew :interfaces:bootRun
T3:
  cd lms_web
  npm run dev
```

- [ ] **Step 2: MANAGER role checks**

Log in as MANAGER.

- [ ] `/admin` dashboard shows stat cards (employees, pending leaves). Store card hidden.
- [ ] `/admin/leaves` — pending list. Approve one → status updates, row disappears from pending. Reject one with reason → same.
- [ ] `/admin/employees` — list shows all active employees. Can create/edit/deactivate.
- [ ] `/admin/schedules` — requires store selection; once selected, schedules in range appear. Can create/edit/delete.
- [ ] `/admin/attendance` — requires store; records in range appear. Adjust with reason → row updates.
- [ ] `/admin/payroll` — period picker. Batch execution card is **hidden** (SUPER_ADMIN-only).
- [ ] `/admin/stores` → redirects to `/403` (SUPER_ADMIN-only route).
- [ ] `/admin/payroll/policies` → redirects to `/403`.

- [ ] **Step 3: SUPER_ADMIN role checks**

Log out, log in as SUPER_ADMIN.

- [ ] Dashboard includes 매장 수 stat card.
- [ ] `/admin/stores` — CRUD table works. Create + edit + delete a dummy store (then delete it to avoid data pollution).
- [ ] `/admin/payroll/policies` — CRUD works. Create a dummy policy and remove it.
- [ ] `/admin/payroll` — batch execution card visible. Trigger a batch (optional; creates a history record).

- [ ] **Step 4: Note any bugs**

Report via `fix(lms_web): ...` commits. Common culprits:
- Response shape mismatch (wrapped vs plain array)
- `/api/employees` query filter ignored (backend may not support all filter combos)
- Time string formatting edge cases (timezones)

---

# Self-Review Checklist

- [ ] Typecheck: `cd lms_web && npm run typecheck` → 0 errors after each task.
- [ ] Tests: `npm run test:run` → 20/20 (no new tests added; all prior still pass).
- [ ] Build: `npm run build` succeeds.
- [ ] 9 tasks = 9 commits (1 per task).
- [ ] All 8 admin pages replace their placeholders via re-export shims (so routes.tsx doesn't change).
- [ ] Role-based UI:
  - MANAGER sees: dashboard, employees, schedules, attendance, leaves (approve), payroll (no batch card)
  - SUPER_ADMIN sees: above + stores + policies + payroll batch card
  - Route-level role guards remain intact from Plan 1 (ProtectedRoute).
- [ ] No placeholder text in admin pages; every page fully implemented.
- [ ] Backend contracts honored: storeId required on admin attendance/leaves list; period required on payroll list; CRUD on stores/policies is SUPER_ADMIN only.

---

# Corrections / gaps carried forward

- `/api/employees` query with `storeId` filter support is assumed. If runtime shows ignored param, add a client-side filter.
- `AttendanceAdjustRequest` takes ISO datetime. The local-tz `toISOString()` conversion assumes server and client timezone alignment; if the user's browser is not in Asia/Seoul, consider adjusting in a fix commit.
- No paging for any list (trust demo dataset size). Plan 4 can add cursor/offset paging if needed.
- No bulk actions (batch approve, bulk deactivate). Add if needed.

---

# Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-04-23-lms-web-admin-features.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints.

**Which approach?**
