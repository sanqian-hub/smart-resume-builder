import { expect, test } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('resume action bar grouping', () => {
  test('consolidates AI actions into one entry while keeping share tools independent', async ({ page }) => {
    await page.setViewportSize({ width: 1600, height: 900 })

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

    await expect(page.getByRole('button', { name: 'AI工具' })).toBeVisible()
    await expect(page.getByRole('button', { name: '分享简历' })).toBeVisible()
    await expect(page.getByRole('button', { name: '分享管理' })).toBeVisible()
    await expect(page.getByRole('button', { name: '邮件通知' })).toBeVisible()
    await expect(page.getByRole('button', { name: '智能助手' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: '岗位匹配' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: '自我介绍' })).toHaveCount(0)

    await page.getByRole('button', { name: 'AI工具' }).click()
    await expect(page.getByRole('button', { name: '智能助手' })).toBeVisible()
    await expect(page.getByRole('button', { name: '岗位匹配' })).toBeVisible()
    await expect(page.getByRole('button', { name: '自我介绍' })).toBeVisible()
    await expect(page.getByRole('button', { name: '简历打分' })).toBeVisible()
    await expect(page.getByRole('button', { name: '语法纠错' })).toBeVisible()
    await expect(page.locator('.resume-score-icon')).toHaveCount(1)
    const aiMenuColumn = await page.evaluate(() => {
      const options = Array.from(document.querySelectorAll('.ai-tools-menu .ai-tool-option')).map(button => button.getBoundingClientRect())
      if (options.length !== 5) {
        return null
      }

      return {
        sameColumn: options.every(rect => Math.abs(rect.left - options[0].left) <= 2),
        increasingTop: options.every((rect, index) => index === 0 || rect.top > options[index - 1].top),
        widths: options.map(rect => Math.round(rect.width)),
        heights: options.map(rect => Math.round(rect.height)),
      }
    })
    expect(aiMenuColumn).not.toBeNull()
    expect(aiMenuColumn.sameColumn).toBe(true)
    expect(aiMenuColumn.increasingTop).toBe(true)
    expect(Math.max(...aiMenuColumn.widths) - Math.min(...aiMenuColumn.widths)).toBeLessThanOrEqual(2)
    expect(Math.max(...aiMenuColumn.heights) - Math.min(...aiMenuColumn.heights)).toBeLessThanOrEqual(2)

    await page.setViewportSize({ width: 1280, height: 720 })
    await page.waitForTimeout(100)

    const narrowMetrics = await page.evaluate(() => {
      const container = document.querySelector('.edit-actions-inner')
      const trigger = Array.from(document.querySelectorAll('.edit-actions-inner .action-btn')).find(
        button => button.textContent?.includes('AI工具'),
      )
      const options = Array.from(document.querySelectorAll('.ai-tools-menu .ai-tool-option')).map(button => button.getBoundingClientRect())
      if (!container || !trigger) {
        return null
      }

      const containerRect = container.getBoundingClientRect()

      return {
        triggerRight: Math.round(trigger.getBoundingClientRect().right),
        containerRight: Math.round(containerRect.right),
        optionCount: options.length,
        sameColumn: options.length === 5 && options.every(rect => Math.abs(rect.left - options[0].left) <= 2),
        optionWidths: options.map(rect => Math.round(rect.width)),
        optionHeights: options.map(rect => Math.round(rect.height)),
      }
    })

    expect(narrowMetrics).not.toBeNull()
    expect(narrowMetrics.triggerRight).toBeLessThanOrEqual(narrowMetrics.containerRight)
    expect(narrowMetrics.optionCount).toBe(5)
    expect(narrowMetrics.sameColumn).toBe(true)
    expect(Math.max(...narrowMetrics.optionWidths) - Math.min(...narrowMetrics.optionWidths)).toBeLessThanOrEqual(2)
    expect(Math.max(...narrowMetrics.optionHeights) - Math.min(...narrowMetrics.optionHeights)).toBeLessThanOrEqual(2)
  })
})
