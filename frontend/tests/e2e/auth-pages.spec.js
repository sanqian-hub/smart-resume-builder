import { test, expect } from '@playwright/test'

test.describe('auth pages integration', () => {
  test('login page exposes stable auth entry behavior', async ({ page }) => {
    await page.goto('/login')

    await expect(page.getByLabel('账号')).toBeVisible()
    await expect(page.getByLabel('密码')).toHaveAttribute('type', 'password')
    await expect(page.locator('#app-loader')).toHaveClass(/hide/)

    await page.locator('.pw-toggle').click()
    await expect(page.getByLabel('密码')).toHaveAttribute('type', 'text')

    await page.getByRole('link', { name: '立即注册' }).click()
    await expect(page).toHaveURL(/\/register$/)
  })

  test('register page validates locally and can return to login', async ({ page }) => {
    await page.goto('/register')

    await expect(page.getByLabel('用户名')).toBeVisible()
    await expect(page.getByLabel('账号')).toBeVisible()
    await expect(page.getByLabel('密码')).toHaveAttribute('type', 'password')
    await expect(page.getByLabel('手机号')).toBeVisible()
    await expect(page.getByLabel('邮箱')).toBeVisible()
    await expect(page.locator('#app-loader')).toHaveClass(/hide/)

    await page.locator('.pw-toggle').click()
    await expect(page.getByLabel('密码')).toHaveAttribute('type', 'text')

    await page.getByLabel('用户名').fill('测试用户')
    await page.getByLabel('账号').fill('ab')
    await page.getByLabel('密码').fill('1234567')
    await page.getByRole('button', { name: '注册' }).click()

    await expect(page.getByText('账号至少 4 位')).toBeVisible()

    await page.getByRole('link', { name: '立即登录' }).click()
    await expect(page).toHaveURL(/\/login$/)
  })
})
