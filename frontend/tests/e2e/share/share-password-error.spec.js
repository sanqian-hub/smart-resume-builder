import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('password protected share error integration', () => {
  test('keeps password gate visible and shows error when visitor enters wrong password', async ({ page }) => {
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
    await expect(page).toHaveURL(/\/edit\/101$/)

    await page.getByRole('button', { name: '分享简历' }).click()
    await page.getByRole('button', { name: '设置密码' }).click()
    await page.getByPlaceholder('请输入 6 位数字密码').fill('123456')
    await page.getByRole('button', { name: '生成链接' }).click()
    await expect(page.getByRole('button', { name: /已复制|已生成/ })).toBeVisible()

    await page.goto('/share/share-1', { waitUntil: 'domcontentloaded' })
    await expect(page.getByRole('heading', { name: '请输入访问密码' })).toBeVisible()

    await page.getByPlaceholder('请输入 6 位数字密码').fill('654321')
    await page.getByRole('button', { name: '查看简历' }).click()

    await expect(page.getByText('访问密码错误')).toBeVisible()
    await expect(page.getByRole('heading', { name: '请输入访问密码' })).toBeVisible()
  })
})
