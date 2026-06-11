import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('share manager expire integration', () => {
  test('updates share expire days from manager dialog', async ({ page }) => {
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
    await page.getByRole('button', { name: '直接分享' }).click()
    await expect(page.getByRole('button', { name: /已复制|已生成/ })).toBeVisible()

    await page.getByRole('button', { name: '分享管理' }).click()
    const beforeExpire = page.getByText(/剩余 \d+ 天/).first()
    await expect(beforeExpire).toBeVisible()
    const beforeExpireText = await beforeExpire.textContent()
    await page.getByRole('button', { name: '有效期' }).click()
    await expect(page.getByRole('heading', { name: '设置有效期' })).toBeVisible()
    await page.getByRole('button', { name: '7天' }).click()
    await page.locator('.share-config-panel .modal-btn--confirm').click()
    await expect(page.getByRole('heading', { name: '设置有效期' })).toHaveCount(0)
    const afterExpire = page.getByText(/剩余 \d+ 天/).first()
    await expect(afterExpire).toBeVisible()
    await expect(afterExpire).not.toHaveText(beforeExpireText || '')
  })
})
