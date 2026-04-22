import axios, { type AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '@/lib/auth/token-storage'
import type { ApiError, ApiResponse, AuthErrorResponse } from './types'
import { endpoints } from './endpoints'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

// Request: inject Authorization header
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.set?.('Authorization', `Bearer ${token}`)
    // Fallback for older axios where headers is plain object
    ;(config.headers as Record<string, unknown>).Authorization = `Bearer ${token}`
  }
  return config
})

// Shared refresh promise to deduplicate concurrent 401s
let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = tokenStorage.getRefreshToken()
  if (!refreshToken) throw new Error('no refresh token')
  // Use api instance (so mock adapter intercepts in tests), but mark as non-retryable via
  // the isRefreshCall guard in the response interceptor.
  const res = await api.post<{ accessToken: string }>(
    endpoints.auth.refresh,
    { refreshToken },
  )
  tokenStorage.setAccessToken(res.data.accessToken)
  return res.data.accessToken
}

type RetriableRequest = AxiosRequestConfig & { _retry?: boolean }

api.interceptors.response.use(
  (res) => res,
  async (err: AxiosError) => {
    const original = err.config as RetriableRequest | undefined
    const status = err.response?.status
    const isRefreshCall = original?.url?.includes(endpoints.auth.refresh)

    if (status === 401 && original && !original._retry && !isRefreshCall) {
      original._retry = true
      try {
        if (!refreshPromise) {
          refreshPromise = refreshAccessToken().finally(() => {
            refreshPromise = null
          })
        }
        const newToken = await refreshPromise
        original.headers = {
          ...(original.headers ?? {}),
          Authorization: `Bearer ${newToken}`,
        } as never
        return api.request(original)
      } catch {
        tokenStorage.clear()
        return Promise.reject(normalizeError(err))
      }
    }

    return Promise.reject(normalizeError(err))
  },
)

function normalizeError(err: AxiosError): ApiError {
  const status = err.response?.status ?? 0
  const data = err.response?.data as ApiResponse<unknown> | AuthErrorResponse | undefined
  if (data && typeof data === 'object') {
    if ('code' in data && typeof data.code === 'string') {
      return { status, code: data.code, message: data.message ?? '' }
    }
    if ('success' in data) {
      return { status, code: null, message: data.message ?? '' }
    }
  }
  return { status, code: null, message: err.message || '네트워크 오류가 발생했습니다.' }
}
