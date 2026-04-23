import { request as playwrightRequest } from '@playwright/test'

const BACKEND = 'http://localhost:8080'

/**
 * Truncate + reseed the e2e database.
 * Call at start of each test suite (beforeAll) for isolation.
 */
export async function resetDb(): Promise<void> {
  const ctx = await playwrightRequest.newContext()
  const res = await ctx.post(`${BACKEND}/test-only/reset`)
  if (!res.ok()) {
    const body = await res.text()
    throw new Error(`resetDb failed: ${res.status()} ${body}`)
  }
  await ctx.dispose()
}

/**
 * Direct login against backend (bypasses UI) — useful when a test needs a token
 * without exercising the login UI.
 */
export async function backendLogin(
  email: string,
  password: string,
): Promise<{ accessToken: string; refreshToken: string }> {
  const ctx = await playwrightRequest.newContext()
  const res = await ctx.post(`${BACKEND}/api/auth/login`, { data: { email, password } })
  if (!res.ok()) throw new Error(`login failed: ${res.status()}`)
  const body = await res.json()
  await ctx.dispose()
  return { accessToken: body.accessToken, refreshToken: body.refreshToken }
}
