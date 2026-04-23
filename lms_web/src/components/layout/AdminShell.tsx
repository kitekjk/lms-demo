import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard,
  Users,
  Building2,
  CalendarDays,
  Clock,
  FileCheck2,
  Wallet,
  Settings2,
  LogOut,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { useAuthStore } from '@/features/auth/store'
import { useLogout } from '@/features/auth/api'
import { cn } from '@/lib/utils'

interface NavItem {
  to: string
  label: string
  icon: typeof LayoutDashboard
  roles?: Array<'MANAGER' | 'SUPER_ADMIN'>
}

const navItems: NavItem[] = [
  { to: '/admin', label: '대시보드', icon: LayoutDashboard },
  { to: '/admin/employees', label: '직원 관리', icon: Users },
  { to: '/admin/stores', label: '매장 관리', icon: Building2, roles: ['SUPER_ADMIN'] },
  { to: '/admin/schedules', label: '근무 일정', icon: CalendarDays },
  { to: '/admin/attendance', label: '근태 관리', icon: Clock },
  { to: '/admin/leaves', label: '휴가 관리', icon: FileCheck2 },
  { to: '/admin/payroll', label: '급여 관리', icon: Wallet },
  { to: '/admin/payroll/policies', label: '급여 정책', icon: Settings2, roles: ['SUPER_ADMIN'] },
]

export default function AdminShell() {
  const user = useAuthStore((s) => s.currentUser)
  const logoutMutation = useLogout()
  const navigate = useNavigate()

  const handleLogout = () => {
    const logoutStore = useAuthStore.getState().logout
    logoutMutation.mutate(undefined, {
      onSettled: () => {
        // Navigate first (while still "authenticated") to avoid ProtectedRoute
        // redirecting to /login with state.from=<current-route>.
        navigate('/login', { replace: true })
        logoutStore()
      },
    })
  }

  const visibleItems = navItems.filter(
    (item) => !item.roles || (user && item.roles.includes(user.role as 'MANAGER' | 'SUPER_ADMIN')),
  )

  return (
    <div className="flex min-h-screen bg-background">
      <aside className="hidden w-64 shrink-0 border-r bg-muted/30 md:flex md:flex-col">
        <div className="flex h-14 items-center border-b px-4 font-semibold">LMS 관리자</div>
        <nav className="flex-1 space-y-1 p-2">
          {visibleItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/admin'}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 rounded-md px-3 py-2 text-sm',
                  isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-accent',
                )
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
        <Separator />
        <div className="p-3 text-xs text-muted-foreground">{user?.email}</div>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex h-14 items-center justify-between border-b px-6">
          <span className="text-sm text-muted-foreground md:hidden">LMS 관리자</span>
          <div className="flex flex-1 items-center justify-end gap-3">
            <span className="text-sm text-muted-foreground">{user?.role}</span>
            <Button variant="ghost" size="icon" aria-label="로그아웃" onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
