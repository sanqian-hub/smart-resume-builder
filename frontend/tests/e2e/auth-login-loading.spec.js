import { test, expect } from '@playwright/test'

test.describe('login loading integration', () => {
  test('shows full-screen loading until home data is ready after login succeeds', async ({ page }) => {
    await page.route('**/api/user/login', async route => {
      await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
    })
    await page.route('**/api/user/current', async route => {
      await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
    })
    await page.route('**/api/resume/my/list', async route => {
      await new Promise(resolve => setTimeout(resolve, 1200))
      await route.fulfill({ json: { code: 0, data: [] } })
    })

    await page.goto('/login')
    await expect(page.locator('#app-loader')).toBeHidden()

    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()

    await expect(page.locator('#app-runtime-loader')).toBeVisible()
    await expect(page).toHaveURL(/\/$/)
    await expect(page.locator('#app-runtime-loader')).toBeVisible()

    await expect(page.locator('#app-runtime-loader')).toBeHidden()
    await expect(page.getByText('开始创建你的第一份专业简历')).toBeVisible()
  })
})
