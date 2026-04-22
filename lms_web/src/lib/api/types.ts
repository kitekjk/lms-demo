// Generic ApiResponse envelope used by non-auth controllers
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T | null
  timestamp: string
}

// Raw error shape from AuthController's @ExceptionHandler
export interface AuthErrorResponse {
  code: string
  message: string
  timestamp: string
}

// Normalized error consumed by UI
export interface ApiError {
  status: number
  code: string | null // AUTH001, AUTH002, REG001, etc. if present
  message: string // Korean user-facing message
}
