import { expect, test } from '@playwright/test'

test('guest protected toolbar actions redirect to login and return to edit', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
    sessionStorage.removeItem('resume-pending-login-intent')
    sessionStorage.removeItem('resume-pending-guest-payload')
  })

  const loginBackToGuestEdit = async () => {
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\/edit$/)
    await page.addInitScript(() => {
      localStorage.removeItem('user')
      sessionStorage.removeItem('resume-pending-login-intent')
      sessionStorage.removeItem('resume-pending-guest-payload')
    })
    await page.reload()
    await expect(page).toHaveURL(/\/edit$/)
  }

  await page.route('**/api/user/login', async route => {
    await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
  })
  await page.route('**/api/user/current', async route => {
    await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
  })
  await page.route('**/api/notice/unread-count', async route => {
    await route.fulfill({ json: { code: 0, data: 0 } })
  })

  await page.goto('/edit')
  await page.locator('.title-input').fill('游客受限动作')

  await page.getByRole('button', { name: '分享管理' }).click()
  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:&|$)/)
  await loginBackToGuestEdit()

  await page.getByRole('button', { name: '历史版本' }).click()
  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:&|$)/)
  await loginBackToGuestEdit()

  await page.getByRole('button', { name: '邮件通知' }).click()
  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:&|$)/)
})
