import { useMutation } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import { tokenStorage } from '@/lib/auth/token-storage'
import { useAuthStore, type AuthUser } from './store'
import type { LoginInput } from './schema'

interface LoginResponse {
  accessToken: string
  refreshToken: string
  userInfo: AuthUser
}

export function useLogin() {
  const setCurrentUser = useAuthStore((s) => s.setCurrentUser)
  return useMutation({
    mutationFn: async (input: LoginInput) => {
      const res = await api.post<LoginResponse>(endpoints.auth.login, input)
      return res.data
    },
    onSuccess: (data) => {
      tokenStorage.setTokens(data.accessToken, data.refreshToken)
      setCurrentUser(data.userInfo)
    },
  })
}

export function useLogout() {
  return useMutation({
    mutationFn: async () => {
      try {
        await api.post(endpoints.auth.logout)
      } catch {
        /* ignore — client-side logout primacy */
      }
    },
    // State clear is intentionally NOT here — the caller handles it AFTER navigation,
    // to prevent a race where ProtectedRoute redirects (with state.from) during the
    // brief window where state is cleared but we're still on a protected route.
  })
}
