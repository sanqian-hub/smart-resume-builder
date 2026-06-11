import { test, expect } from '@playwright/test'
import { mockAuthenticatedShell } from '../helpers/appMocks'

test.describe('resume entry integration', () => {
  test('logs in, shows empty resume list, and enters edit workspace from new resume action', async ({ page }) => {
    await page.route('**/api/user/login', async route => {
      await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
    })
    await mockAuthenticatedShell(page, {
      user: { id: 1, username: '测试用户' },
      resumeList: [],
      unreadCount: 0,
      memoryList: [],
      persistUser: false,
    })

    await page.goto('/login')

    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()

    await expect(page).toHaveURL(/\/$/)
    await expect(page.getByText('还没有简历，开始创建你的第一份')).toBeVisible()
    await expect(page.getByRole('button', { name: '新建简历' })).toBeVisible()

    await page.getByRole('button', { name: '新建简历' }).click()

    await expect(page).toHaveURL(/\/edit$/)
    await expect(page.getByRole('button', { name: 'AI工具' })).toBeVisible()
    await expect(page.getByRole('button', { name: '保存' })).toBeVisible()
    await expect(page.getByPlaceholder('简历标题')).toHaveValue('未命名简历')
    await expect(page.getByRole('button', { name: '基本信息', exact: true })).toBeVisible()
  })
})
