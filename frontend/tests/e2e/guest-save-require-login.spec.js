import { expect, test } from '@playwright/test'

test('guest save redirects to login and restores draft after login', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
  })

  let saveCalled = false

  await page.route('**/api/resume/add', async route => {
    saveCalled = true
    await route.fulfill({ json: { code: 0, data: 101 } })
  })
  await page.route('**/api/resume/update', async route => {
    saveCalled = true
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/api/resume/version/save**', async route => {
    saveCalled = true
    await route.fulfill({ json: { code: 0, data: { id: 1, versionNum: 1 } } })
  })
  await page.route('**/api/notice/analyze/**', async route => {
    saveCalled = true
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/api/user/login', async route => {
    await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
  })
  await page.route('**/api/user/current', async route => {
    await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
  })
  await page.route('**/api/notice/unread-count', async route => {
    await route.fulfill({ json: { code: 0, data: 0 } })
  })

  await page.goto('/edit')

  await page.locator('.title-input').fill('游客登录后继续保存')
  await page.getByRole('button', { name: '保存' }).click()

  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:$|&)/)
  await expect(page.getByText('登录后即可保存当前简历')).toBeVisible()
  expect(saveCalled).toBe(false)

  await page.getByLabel('账号').fill('tester')
  await page.getByLabel('密码').fill('password123')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/edit$/)
  await expect(page.locator('.title-input')).toHaveValue('游客登录后继续保存')
})

test('guest save can register first and still return to edit after login', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
  })

  await page.route('**/api/user/register', async route => {
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/api/user/login', async route => {
    await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
  })
  await page.route('**/api/user/current', async route => {
    await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
  })
  await page.route('**/api/notice/unread-count', async route => {
    await route.fulfill({ json: { code: 0, data: 0 } })
  })

  await page.goto('/edit')

  await page.locator('.title-input').fill('游客先注册再登录')
  await page.getByRole('button', { name: '保存' }).click()

  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:$|&)/)
  await page.getByRole('link', { name: '立即注册' }).click()

  await expect(page).toHaveURL(/\/register\?redirect=\/edit(?:$|&)/)
  await page.getByLabel('用户名').fill('测试用户')
  await page.getByLabel('账号').fill('tester')
  await page.getByLabel('密码').fill('password123')
  await page.getByLabel('手机号').fill('13800000000')
  await page.getByLabel('邮箱').fill('test@example.com')
  await page.getByRole('button', { name: '注册' }).click()

  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:$|&)/)
  await page.getByLabel('账号').fill('tester')
  await page.getByLabel('密码').fill('password123')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/edit$/)
  await expect(page.locator('.title-input')).toHaveValue('游客先注册再登录')
})
