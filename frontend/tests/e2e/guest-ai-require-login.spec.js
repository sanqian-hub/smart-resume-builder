import { expect, test } from '@playwright/test'

test('guest ai actions redirect to login and return to edit', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
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
  const titleInput = page.locator('.title-input')
  await titleInput.fill('游客 AI 需登录')
  await titleInput.blur()

  await page.getByRole('button', { name: 'AI工具' }).click()
  await page.getByRole('button', { name: '智能助手' }).click()

  await expect(page).toHaveURL(/\/login\?redirect=\/edit(?:&|$)/)

  await page.getByLabel('账号').fill('tester')
  await page.getByLabel('密码').fill('password123')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/edit$/)
  await expect(page.locator('.title-input')).toHaveValue('游客 AI 需登录')
})

test('guest proofread action redirects to login and return to edit', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
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
  const titleInput = page.locator('.title-input')
  await titleInput.fill('游客 纠错需登录')
  await titleInput.blur()

  await page.getByRole('button', { name: 'AI工具' }).click()
  await page.getByRole('button', { name: '语法纠错' }).click()

  await expect(page).toHaveURL(/\/login\?redirect=\/edit&intent=ai-proofread|\/login\?intent=ai-proofread&redirect=\/edit/)

  await page.getByLabel('账号').fill('tester')
  await page.getByLabel('密码').fill('password123')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/edit$/)
  await expect(page.locator('.title-input')).toHaveValue('游客 纠错需登录')
})
