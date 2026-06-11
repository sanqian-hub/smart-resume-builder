import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('version back to latest integration', () => {
  test('returns from an older version view to the latest saved content', async ({ page }) => {
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

    const basicSection = page.locator('.accordion-item').filter({ has: page.getByRole('button', { name: '基本信息', exact: true }) }).first()
    await page.getByRole('button', { name: '基本信息', exact: true }).click()
    const nameInput = basicSection.locator('.form-item').nth(0).locator('input')

    await nameInput.fill('最新前的版本')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('button', { name: '已保存' })).toBeVisible()

    await nameInput.fill('当前最新版本')
    await page.getByRole('button', { name: '保存' }).click()
    await expect(page.getByRole('button', { name: '已保存' })).toBeVisible()

    await page.getByRole('button', { name: '历史版本' }).click()
    await page.locator('.version-item').filter({ has: page.getByText('v1', { exact: true }) }).first().click()

    await expect(page.getByRole('button', { name: '返回最新' })).toBeVisible()
    await expect(nameInput).toHaveValue('最新前的版本')

    await page.getByRole('button', { name: '返回最新' }).click()

    await expect(nameInput).toHaveValue('当前最新版本')
    await expect(page.getByRole('button', { name: '返回最新' })).toHaveCount(0)
  })
})
