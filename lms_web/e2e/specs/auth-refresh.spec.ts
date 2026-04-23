import { test, expect } from '@playwright/test'
import { loginAs, setAccessToken, accessToken } from '../helpers/auth'
import { resetDb } from '../helpers/api'

test.describe('401 refresh flow', () => {
  test.beforeEach(async () => {
    await resetDb()
  })

  test('Scenario 5 — expired access token triggers silent refresh on next authenticated request', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')

    // We start on /home with a valid access token.
    const original = await accessToken(page)
    expect(original).toBeTruthy()

    // Mutate the signature of the real token — preserves JWT structure
    // (header.payload.signature) but invalidates signature verification.
    // Spring Security's JWT filter then returns 401 (not 403 for malformed-as-not-a-JWT),
    // which triggers the axios interceptor's refresh flow.
    const corruptedToken = original!.replace(/.{5}$/, 'XXXXX')
    await setAccessToken(page, corruptedToken)

    // Navigate to a page that makes an authenticated request.
    // /attendance/history calls GET /api/attendance/my-records →
    // stale token rejected → interceptor refreshes → retry succeeds.
    await page.goto('/attendance/history')

    // Filter inputs present (page rendered successfully after recovery)
    await expect(page.getByLabel('시작일')).toBeVisible({ timeout: 10_000 })

    // Access token in localStorage must have been replaced with a new one
    const updated = await accessToken(page)
    expect(updated).not.toBe(corruptedToken)
    expect(updated).not.toBe(original) // new token, different from both
  })
})
