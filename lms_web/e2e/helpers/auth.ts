import type { Page } from '@playwright/test'
import { USERS } from '../fixtures/users'

type UserKey = keyof typeof USERS

/**
 * Fill login form and submit. After login:
 * - EMPLOYEE lands on /home
 * - MANAGER / SUPER_ADMIN land on /admin
 */
export async function loginAs(page: Page, userKey: UserKey): Promise<void> {
  const u = USERS[userKey]
  await page.goto('/login')
  await page.getByLabel('이메일').fill(u.email)
  await page.getByLabel('비밀번호').fill(u.password)
  await page.getByRole('button', { name: '로그인' }).click()

  const target = u.role === 'EMPLOYEE' ? '/home' : '/admin'
  await page.waitForURL(`**${target}`, { timeout: 10_000 })
}

export async function logout(page: Page): Promise<void> {
  await page.getByRole('button', { name: '로그아웃' }).click()
  // Logout uses window.location.replace('/login') — a hard browser navigation.
  // We must wait for both the URL match AND the page load to complete, otherwise
  // the next page.goto() can race with the in-flight hard navigation and error
  // with "Navigation is interrupted by another navigation".
  await page.waitForURL('**/login', { timeout: 5_000 })
  await page.waitForLoadState('load')
}

export function accessToken(page: Page): Promise<string | null> {
  return page.evaluate(() => localStorage.getItem('lms.access_token'))
}

export async function setAccessToken(page: Page, token: string): Promise<void> {
  await page.evaluate((t) => localStorage.setItem('lms.access_token', t), token)
}
