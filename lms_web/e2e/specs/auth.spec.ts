import { test, expect } from '@playwright/test'
import { USERS } from '../fixtures/users'
import { loginAs } from '../helpers/auth'
import { resetDb } from '../helpers/api'

test.describe('auth scenarios', () => {
  test.beforeAll(async () => {
    await resetDb()
  })

  test('Scenario 1 — employee logs in and reaches /home with their name', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')
    await expect(page).toHaveURL(/\/home$/)
    // Name comes from useCurrentEmployee → /api/employees list + client-side filter
    await expect(page.getByRole('heading').first()).toContainText(USERS.employeeGangnam.name)
  })

  test('Scenario 6 — employee attempting /admin is redirected to /403', async ({ page }) => {
    await loginAs(page, 'employeeGangnam')
    await page.goto('/admin')
    await expect(page).toHaveURL(/\/403$/)
    await expect(page.getByText('접근 권한 없음')).toBeVisible()
  })

  test('login with wrong password shows toast error and stays on /login', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('이메일').fill(USERS.employeeGangnam.email)
    await page.getByLabel('비밀번호').fill('wrong-password')
    await page.getByRole('button', { name: '로그인' }).click()

    // sonner toast appears somewhere on the page
    await expect(page.getByText(/일치하지 않|인증|AUTH/i).first()).toBeVisible({ timeout: 5_000 })
    await expect(page).toHaveURL(/\/login$/)
  })
})
