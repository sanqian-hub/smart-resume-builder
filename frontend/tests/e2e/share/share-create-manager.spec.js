import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('share create and manager integration', () => {
  test('creates a share from a saved resume and shows it in share manager', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'clipboard', {
        configurable: true,
        value: {
          writeText: async () => {},
        },
      })
    })

    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()

    await expect(page).toHaveURL(/\/$/)
    await page.getByRole('button', { name: '新建简历' }).click()
    await expect(page).toHaveURL(/\/edit$/)

    await page.getByRole('button', { name: '保存' }).click()
    await expect(page).toHaveURL(/\/edit\/101$/)
    await expect(page.getByRole('button', { name: '已保存' })).toBeVisible()

    await page.getByRole('button', { name: '分享简历' }).click()
    await expect(page.getByRole('heading', { name: '分享设置' })).toBeVisible()
    await page.getByRole('button', { name: '直接分享' }).click()
    await expect(page.getByRole('heading', { name: '分享设置' })).toHaveCount(0)

    await page.getByRole('button', { name: '分享管理' }).click()
    await expect(page.getByText('简历版本 v1')).toBeVisible()
    await expect(page.getByText('已开启')).toBeVisible()
  })
})
