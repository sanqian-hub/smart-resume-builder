import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('ai chat integration', () => {
  test('sends a prompt, receives streamed reply, and clears chat history', async ({ page }) => {
    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
      chatHistory: [],
    })

    await page.goto('/login')
    await expect(page.locator('#app-loader')).toBeHidden()
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page).toHaveURL(/\/edit\/101$/)

    await page.getByRole('button', { name: 'AI工具' }).click()
    await page.getByRole('button', { name: '智能助手' }).click()
    await expect(page.getByText('有什么可以帮助你的？')).toBeVisible()
    await page.getByPlaceholder('描述你想要直接修改的简历内容...').fill('帮我优化项目经历')
    await page.locator('.ai-send-btn.active').click()

    await expect(page.getByText('帮我优化项目经历')).toBeVisible()
    await expect(page.getByText('这是 AI 返回的简历优化建议。')).toBeVisible()

    await page.getByTitle('清空对话').click()
    await expect(page.getByText('有什么可以帮助你的？')).toBeVisible()
  })
})
