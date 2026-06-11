import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('password protected share access integration', () => {
  test('shows password gate and reveals resume after correct password verification', async ({ page }) => {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, 'clipboard', {
        configurable: true,
        value: {
          writeText: async () => {},
        },
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

    await expect(page).toHaveURL(/\/$/)
    await page.getByRole('button', { name: '新建简历' }).click()
    await expect(page).toHaveURL(/\/edit$/)

    const basicSection = page.locator('.accordion-item').filter({ has: page.getByRole('button', { name: '基本信息', exact: true }) }).first()
    await page.getByRole('button', { name: '基本信息', exact: true }).click()
    const nameInput = basicSection.locator('.form-item').nth(0).locator('input')
    await nameInput.fill('受保护分享姓名')

    await page.getByRole('button', { name: '保存' }).click()
    await expect(page).toHaveURL(/\/edit\/101$/)
    await expect(page.getByRole('button', { name: '已保存' })).toBeVisible()

    await page.getByRole('button', { name: '分享简历' }).click()
    await expect(page.getByRole('heading', { name: '分享设置' })).toBeVisible()
    await page.getByRole('button', { name: '设置密码' }).click()
    await page.getByPlaceholder('请输入 6 位数字密码').fill('123456')
    await page.getByRole('button', { name: '生成链接' }).click()
    await expect(page.getByRole('heading', { name: '分享设置' })).toHaveCount(0)

    await page.goto('/share/share-1')

    await expect(page.getByRole('heading', { name: '请输入访问密码' })).toBeVisible()
    await page.getByPlaceholder('请输入 6 位数字密码').fill('123456')
    await page.getByRole('button', { name: '查看简历' }).click()

    await expect(page.getByRole('heading', { name: '请输入访问密码' })).toHaveCount(0)
    await expect(page.getByText('受保护分享姓名')).toBeVisible()
  })
})
