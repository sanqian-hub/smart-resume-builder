import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('self intro generation integration', () => {
  test('generates a self introduction card from current resume content', async ({ page }) => {
    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()

    await page.getByRole('button', { name: 'AI工具' }).click()
    await page.getByRole('button', { name: '自我介绍' }).click()
    await expect(page.getByRole('heading', { name: 'AI 自我介绍' })).toBeVisible()

    await page.getByRole('button', { name: '生成' }).click()
    await expect(page.getByRole('button', { name: '生成中...' })).toBeDisabled()

    await expect(page.getByText('面试自我介绍', { exact: true })).toBeVisible()
    await expect(page.getByRole('button', { name: '复制' })).toBeVisible()
    await expect(page.getByRole('button', { name: '导出图片' })).toBeVisible()
    await expect(page.getByRole('button', { name: '重新生成' })).toBeVisible()
  })
})
