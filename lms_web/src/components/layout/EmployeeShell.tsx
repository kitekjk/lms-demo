import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Home, Clock, Calendar, FileText, Wallet, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/features/auth/store'
import { useLogout } from '@/features/auth/api'
import { cn } from '@/lib/utils'

const tabs = [
  { to: '/home', label: '홈', icon: Home },
  { to: '/attendance', label: '출퇴근', icon: Clock },
  { to: '/schedule', label: '일정', icon: Calendar },
  { to: '/leave', label: '휴가', icon: FileText },
  { to: '/payroll', label: '급여', icon: Wallet },
] as const

export default function EmployeeShell() {
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

  return (
    <div className="mx-auto flex min-h-screen max-w-xl flex-col bg-background">
      <header className="flex h-14 items-center justify-between border-b px-4">
        <span className="font-semibold">LMS</span>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <span>{user?.email}</span>
          <Button variant="ghost" size="icon" aria-label="로그아웃" onClick={handleLogout}>
            <LogOut className="h-4 w-4" />
          </Button>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto p-4 pb-24">
        <Outlet />
      </main>

      <nav className="fixed bottom-0 left-1/2 w-full max-w-xl -translate-x-1/2 border-t bg-background">
        <ul className="grid grid-cols-5">
          {tabs.map(({ to, label, icon: Icon }) => (
            <li key={to}>
              <NavLink
                to={to}
                className={({ isActive }) =>
                  cn(
                    'flex flex-col items-center justify-center gap-0.5 py-2 text-xs',
                    isActive ? 'text-primary' : 'text-muted-foreground',
                  )
                }
              >
                <Icon className="h-5 w-5" />
                <span>{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </div>
  )
}
