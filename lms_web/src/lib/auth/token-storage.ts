const ACCESS_KEY = 'lms.access_token'
const REFRESH_KEY = 'lms.refresh_token'

export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_KEY)
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY)
  },
  setTokens(access: string, refresh: string): void {
    localStorage.setItem(ACCESS_KEY, access)
    localStorage.setItem(REFRESH_KEY, refresh)
  },
  setAccessToken(access: string): void {
    localStorage.setItem(ACCESS_KEY, access)
  },
  clear(): void {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}
