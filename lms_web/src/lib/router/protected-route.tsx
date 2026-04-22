import { Navigate, Outlet, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '@/lib/auth/use-auth'
import type { Role } from '@/features/auth/store'

interface Props {
  children?: ReactNode
  roles?: Role[]
}

export function ProtectedRoute({ children, roles }: Props) {
  const location = useLocation()
  const { isAuthenticated, hasRole } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  if (roles && roles.length > 0 && !hasRole(roles)) {
    return <Navigate to="/403" replace />
  }
  return children ? <>{children}</> : <Outlet />
}
