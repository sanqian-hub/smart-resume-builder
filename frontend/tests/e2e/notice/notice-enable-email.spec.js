import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('notice enable email integration', () => {
  test('prompts for email before enabling email notice and closes after confirm', async ({ page }) => {
    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户', noticeEnabled: 0, email: '' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()

    const noticeToggle = page.locator('.action-btn').filter({ has: page.getByText('邮件通知', { exact: true }) }).first()
    await noticeToggle.evaluate(element => element.click())
    await expect(page.getByText('开启消息通知')).toBeVisible()
    await page.getByPlaceholder('请输入邮箱地址').fill('tester@example.com')
    await page.getByRole('button', { name: '确定' }).click()

    await expect(page.getByText('开启消息通知')).toHaveCount(0)
    await expect(page.locator('.action-btn.on').filter({ has: page.getByText('邮件通知') })).toBeVisible()
  })
})
