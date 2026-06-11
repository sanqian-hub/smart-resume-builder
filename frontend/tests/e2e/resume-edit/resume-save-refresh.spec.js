import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('resume save and refresh integration', () => {
  test('edits basic name, saves successfully, and keeps saved value after reload', async ({ page }) => {
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

    await nameInput.fill('测试保存姓名')
    await page.getByRole('button', { name: '保存' }).click()

    await expect(page.getByRole('button', { name: '保存中...' })).toBeDisabled()
    await expect(page).toHaveURL(/\/edit\/101$/)
    await expect(page.getByRole('button', { name: '已保存' })).toBeVisible()

    await page.reload()

    await expect(page).toHaveURL(/\/edit\/101$/)
    await page.getByRole('button', { name: '基本信息', exact: true }).click()
    await expect(nameInput).toHaveValue('测试保存姓名')
  })
})
