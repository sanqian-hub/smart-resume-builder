import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('notice center integration', () => {
  test('marks all unread notices as read from the notice center dialog', async ({ page }) => {
    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
      notices: [
        { id: 1, title: '完整度提醒', content: '<p>请补充项目经历</p>', type: 'completeness_check', isRead: 0, resumeId: 101, resumeTitle: '未命名简历', resumeVersionNum: 1, createTime: '2026-05-06T16:00:00' },
        { id: 2, title: '优化建议', content: '<p>建议补充结果量化</p>', type: 'optimize_suggest', isRead: 0, resumeId: 101, resumeTitle: '未命名简历', resumeVersionNum: 1, createTime: '2026-05-06T16:10:00' },
      ],
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()

    await expect(page.locator('.toolbar-notice-badge')).toHaveText('2')
    await page.locator('.toolbar-notice-btn').click()

    await expect(page.locator('.notice-dialog-header')).toContainText('消息通知')
    await expect(page.getByRole('button', { name: '全部已读' })).toBeVisible()
    await page.getByRole('button', { name: '全部已读' }).click()

    await expect(page.getByRole('button', { name: '全部已读' })).toHaveCount(0)
    await expect(page.locator('.toolbar-notice-badge')).toHaveCount(0)
  })
})
