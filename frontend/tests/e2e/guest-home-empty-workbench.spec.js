import { expect, test } from '@playwright/test'

test('guest home shows empty workbench and does not request myList', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
    sessionStorage.removeItem('resume-pending-login-intent')
    sessionStorage.removeItem('resume-pending-guest-payload')
  })

  let myListCalls = 0
  await page.route('**/api/resume/my/list', async route => {
    myListCalls += 1
    await route.fulfill({ json: { code: 0, data: [] } })
  })

  await page.goto('/')

  await expect(page).toHaveURL(/\/$/)
  await expect(page.getByText('还没有简历，先去体验编辑器')).toBeVisible()
  await expect(page.getByRole('button', { name: '去体验编辑器' })).toBeVisible()
  await expect(page.getByRole('button', { name: '已有账号，去登录' })).toBeVisible()
  await expect(page.getByRole('button', { name: '去体验编辑器' })).toHaveCount(1)
  await expect.poll(() => myListCalls).toBe(0)

  await page.getByRole('button', { name: '去体验编辑器' }).click()
  await expect(page).toHaveURL(/\/edit$/)
})

test('guest home secondary action leads existing users to login', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
    sessionStorage.removeItem('resume-pending-login-intent')
    sessionStorage.removeItem('resume-pending-guest-payload')
  })

  await page.goto('/')

  await page.getByRole('button', { name: '已有账号，去登录' }).click()
  await expect(page).toHaveURL(/\/login$/)
})

test('guest home shows a lightweight login entry instead of logout semantics in the nav bar', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
    sessionStorage.removeItem('resume-pending-login-intent')
    sessionStorage.removeItem('resume-pending-guest-payload')
  })

  await page.goto('/')

  const navLoginButton = page.locator('.nav-login')

  await expect(navLoginButton).toBeVisible()
  await expect(page.locator('.nav-logout')).toHaveCount(0)

  await navLoginButton.click()
  await expect(page).toHaveURL(/\/login$/)
})

test('guest home keeps primary and secondary actions stacked vertically', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    localStorage.removeItem('guest-resume-draft')
    sessionStorage.removeItem('resume-pending-login-intent')
    sessionStorage.removeItem('resume-pending-guest-payload')
  })

  await page.goto('/')

  const primaryButton = page.getByRole('button', { name: '去体验编辑器' })
  const secondaryButton = page.getByRole('button', { name: '已有账号，去登录' })
  const primaryBox = await primaryButton.boundingBox()
  const secondaryBox = await secondaryButton.boundingBox()

  expect(primaryBox).not.toBeNull()
  expect(secondaryBox).not.toBeNull()
  expect(secondaryBox.y).toBeGreaterThan(primaryBox.y + primaryBox.height - 1)
})
