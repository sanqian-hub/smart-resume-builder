import { test, expect } from '@playwright/test'

test.describe('auth entry flow', () => {
  test('shows guest empty workbench on home for unauthenticated visitors', async ({ page }) => {
    await page.goto('/')

    await expect(page).toHaveURL(/\/$/)
    await expect(page.getByText('还没有简历，先去体验编辑器')).toBeVisible()
    await expect(page.getByRole('button', { name: '去体验编辑器' })).toBeVisible()
  })

  test('lets visitors navigate from login to register', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('#app-loader')).toHaveClass(/hide/)

    await page.getByRole('link', { name: '立即注册' }).click()

    await expect(page).toHaveURL(/\/register$/)
  })
})
