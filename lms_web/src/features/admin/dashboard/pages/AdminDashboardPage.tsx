import { Card, CardContent } from '@/components/ui/card'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { LeaveListResponse } from '@/features/leave/types'
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
