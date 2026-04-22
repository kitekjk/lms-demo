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
  const logout = useAuthStore((s) => s.logout)
  return useMutation({
    mutationFn: async () => {
      try {
        await api.post(endpoints.auth.logout)
      } catch {
        /* ignore — client-side logout primacy */
      }
    },
    onSettled: () => {
      logout()
    },
  })
}
