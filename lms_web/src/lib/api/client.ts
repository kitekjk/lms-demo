import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { tokenStorage } from '@/lib/auth/token-storage'
import type { ApiError, ApiResponse, AuthErrorResponse } from './types'

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

// Normalize errors — response interceptor for errors only at this stage (401 refresh added in Task 1.3)
api.interceptors.response.use(
  (res) => res,
  (err: AxiosError) => Promise.reject(normalizeError(err)),
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
