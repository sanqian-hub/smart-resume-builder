import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('share close integration', () => {
  test('closes a share in manager and makes public link unavailable', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'clipboard', {
        configurable: true,
        value: { writeText: async () => {} },
      })
    })

    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await expect(page.locator('#app-loader')).toBeHidden()
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page).toHaveURL(/\/edit\/101$/)

    await page.getByRole('button', { name: '分享简历' }).click()
    await page.getByRole('button', { name: '直接分享' }).click()

    await page.getByRole('button', { name: '分享管理' }).click()
    await expect(page.getByText('已开启')).toBeVisible()
    await page.getByRole('button', { name: '关闭' }).click()
    await expect(page.getByText('已关闭')).toBeVisible()

    await page.goto('/share/share-1')
    await expect(page.getByRole('heading', { name: '分享链接已过期' })).toBeVisible()
  })
})
