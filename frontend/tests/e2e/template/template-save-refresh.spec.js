import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('template persistence integration', () => {
  test('changes template, saves successfully, and keeps selected template after reload', async ({ page }) => {
    test.setTimeout(60_000)

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

    await page.getByRole('button', { name: '模板' }).click()
    await expect(page.getByText('选择模板')).toBeVisible()

    const creativeTemplateCard = page.locator('.template-option-card[aria-label="创意模板01"]').first()
    await creativeTemplateCard.click()
    await expect(page.getByText('选择模板')).not.toBeVisible()

    await page.getByRole('button', { name: '保存' }).click()

    await expect(page.getByRole('button', { name: '保存中...' })).toBeDisabled()
    await expect(page).toHaveURL(/\/edit\/101$/)
    await expect(page.getByRole('button', { name: '已保存' })).toBeVisible()

    await page.reload()

    await expect(page).toHaveURL(/\/edit\/101$/)
    await page.getByRole('button', { name: '模板' }).click()
    await expect(page.getByText('选择模板')).toBeVisible()
    await expect(page.locator('.template-option-card.active[aria-label="创意模板01"]').first()).toBeVisible()
  })
})
