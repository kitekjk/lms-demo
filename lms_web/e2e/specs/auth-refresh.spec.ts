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

    // Corrupt the access token to force a 401 on next authenticated request.
    // Refresh token is untouched, so the interceptor should succeed refreshing.
    await setAccessToken(page, 'invalid-access-token-to-trigger-401')

    // Navigate to a page that makes an authenticated request.
    // /attendance/history calls GET /api/attendance/my-records →
    // stale token rejected → interceptor refreshes → retry succeeds.
    await page.goto('/attendance/history')

    // Filter inputs present (page rendered successfully after recovery)
    await expect(page.getByLabel('시작일')).toBeVisible({ timeout: 10_000 })

    // Access token in localStorage must have been replaced with a new one
    const updated = await accessToken(page)
    expect(updated).not.toBe('invalid-access-token-to-trigger-401')
    expect(updated).not.toBe(original) // new token, different from both
  })
})
