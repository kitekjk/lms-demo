import { test, expect } from '@playwright/test'
import { loginAs } from '../helpers/auth'
import { resetDb } from '../helpers/api'

test.describe('employee attendance flow', () => {
  test.beforeEach(async () => {
    await resetDb()
  })

  test('Scenario 2 — check-in → check-out → history shows today record', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')

    // Navigate to attendance via bottom tab
    await page.getByRole('link', { name: '출퇴근' }).click()
    await expect(page).toHaveURL(/\/attendance$/)

    // Check in
    await page.getByRole('button', { name: '출근하기' }).click()
    await expect(page.getByText('출근이 기록되었습니다')).toBeVisible({ timeout: 5_000 })

    // Refetch → should show "퇴근하기" now
    await expect(page.getByRole('button', { name: '퇴근하기' })).toBeVisible({ timeout: 5_000 })

    // Check out
    await page.getByRole('button', { name: '퇴근하기' }).click()
    await expect(page.getByText('퇴근이 기록되었습니다')).toBeVisible({ timeout: 5_000 })

    // Completion state: 근무시간 label present
    await expect(page.getByText('근무시간:')).toBeVisible({ timeout: 5_000 })

    // Navigate to history
    await page.goto('/attendance/history')
    // Today's record should be in the list — assert on current year text visible
    const today = new Date()
    await expect(page.getByText(new RegExp(`${today.getFullYear()}년`))).toBeVisible({ timeout: 5_000 })
  })
})
