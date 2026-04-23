import { test, expect } from '@playwright/test'
import { loginAs, logout } from '../helpers/auth'
import { resetDb } from '../helpers/api'

function futureDate(daysAhead: number): string {
  const d = new Date()
  d.setDate(d.getDate() + daysAhead)
  return d.toISOString().slice(0, 10)
}

test.describe('leave approval flow', () => {
  test.beforeEach(async () => {
    await resetDb()
  })

  test('Scenarios 3 + 4 — employee requests leave → manager approves → employee sees APPROVED', async ({ page }) => {
    // === Part 1: Employee creates leave request ===
    await loginAs(page, 'employeeGangnam')
    await page.getByRole('link', { name: '휴가' }).click()
    await page.getByRole('link', { name: '신청하기' }).click()

    const start = futureDate(7)
    const end = futureDate(8)

    // Leave type select — open the combobox, pick 연차 explicitly
    await page.getByRole('combobox').click()
    await page.getByRole('option', { name: '연차' }).click()

    await page.getByLabel('시작일').fill(start)
    await page.getByLabel('종료일').fill(end)
    await page.getByLabel('사유 (선택)').fill('e2e 테스트')

    await page.getByRole('button', { name: '신청' }).click()
    await expect(page.getByText('휴가가 신청되었습니다')).toBeVisible({ timeout: 5_000 })

    // Back on /leave list — should see PENDING status (승인 대기)
    await expect(page).toHaveURL(/\/leave$/)
    await expect(page.getByText('승인 대기')).toBeVisible({ timeout: 5_000 })

    // === Part 2: Log out, log in as manager, approve ===
    await logout(page)
    await loginAs(page, 'managerGangnam')

    await page.goto('/admin/leaves')

    // There should be at least 1 pending row. Click first 승인 button.
    await page.getByRole('button', { name: '승인' }).first().click()
    await expect(page.getByText('승인되었습니다')).toBeVisible({ timeout: 5_000 })

    // === Part 3: Log back in as employee, verify status change ===
    await logout(page)
    await loginAs(page, 'employeeGangnam')
    await page.goto('/leave')

    // The e2e test request should now show APPROVED (승인됨)
    await expect(page.getByText('승인됨')).toBeVisible({ timeout: 5_000 })
  })
})
