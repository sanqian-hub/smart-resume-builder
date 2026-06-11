import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('share manager password integration', () => {
  test('updates share password from manager dialog', async ({ page }) => {
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
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()
    await page.getByRole('button', { name: '保存' }).click()

    await page.getByRole('button', { name: '分享简历' }).click()
    await page.getByRole('button', { name: '设置密码' }).click()
    await page.getByPlaceholder('请输入 6 位数字密码').fill('123456')
    await page.getByRole('button', { name: '生成链接' }).click()
    await expect(page.getByRole('button', { name: /已复制|已生成/ })).toBeVisible()

    await page.getByRole('button', { name: '分享管理' }).click()
    await page.getByRole('button', { name: '密码' }).click()
    await expect(page.getByRole('heading', { name: '管理密码' })).toBeVisible()
    await expect(page.getByText('123456')).toBeVisible()

    await page.getByPlaceholder('输入新的 6 位数字密码').fill('654321')
    await page.locator('.share-config-panel').getByRole('button', { name: '保存', exact: true }).click()
    await expect(page.getByRole('heading', { name: '管理密码' })).toHaveCount(0)

    await page.getByRole('button', { name: '密码' }).click()
    await expect(page.getByText('654321')).toBeVisible()
  })
})
