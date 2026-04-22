import { useAuthStore, type Role } from '@/features/auth/store'
import { tokenStorage } from './token-storage'

export function useAuth() {
  const currentUser = useAuthStore((s) => s.currentUser)
  const hasToken = tokenStorage.getAccessToken() !== null
  const isAuthenticated = !!currentUser && hasToken
  const hasRole = (roles: Role[]): boolean => !!currentUser && roles.includes(currentUser.role)
  return { currentUser, isAuthenticated, hasRole }
}
