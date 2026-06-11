import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from './helpers/resumeEditorMocks'

test.describe('record footer integration', () => {
  test('footer links highlight with the theme color on hover', async ({ page }) => {
    await page.goto('/login')

    const githubLink = page.getByRole('link', { name: 'GitHub · sanqian' })
    const icpLink = page.getByRole('link', { name: '粤ICP备2026028310号-2' })
    const baseColor = await githubLink.evaluate(el => getComputedStyle(el).color)
    const primaryColor = await page.evaluate(() => {
      const probe = document.createElement('span')
      probe.style.color = 'var(--primary)'
      document.body.appendChild(probe)
      const color = getComputedStyle(probe).color
      probe.remove()
      return color
    })

    expect(baseColor).not.toBe(primaryColor)

    await githubLink.hover()
    await expect.poll(() => githubLink.evaluate(el => getComputedStyle(el).color)).toBe(primaryColor)

    await icpLink.hover()
    await expect.poll(() => icpLink.evaluate(el => getComputedStyle(el).color)).toBe(primaryColor)
  })

  test('login and register pages show record links', async ({ page }) => {
    await page.goto('/login')

    await expect(page.getByRole('link', { name: '粤ICP备2026028310号-2' })).toBeVisible()
    await expect(page.getByRole('link', { name: /粤公网安备44088202000096号/ })).toBeVisible()

    await page.getByRole('link', { name: '立即注册' }).click()
    await expect(page).toHaveURL(/\/register$/)
    await expect(page.getByRole('link', { name: '粤ICP备2026028310号-2' })).toBeVisible()
    await expect(page.getByRole('link', { name: /粤公网安备44088202000096号/ })).toBeVisible()
  })

  test('home page shows record footer after auth init', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('user', JSON.stringify({ id: 1, username: '测试用户' }))
    })
    await page.route('**/api/user/current', async route => {
      await route.fulfill({ json: { code: 0, data: { id: 1, username: '测试用户' } } })
    })
    await page.route('**/api/resume/my/list', async route => {
      await route.fulfill({ json: { code: 0, data: [] } })
    })

    await page.goto('/')

    await expect(page.getByRole('link', { name: '粤ICP备2026028310号-2' })).toBeVisible()
    await expect(page.getByRole('link', { name: /粤公网安备44088202000096号/ })).toBeVisible()
  })

  test('edit workspace hides record footer', async ({ page }) => {
    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 1,
      seedLocalUser: true,
    })

    await page.goto('/edit/1')

    await expect(page.getByRole('link', { name: '粤ICP备2026028310号-2' })).toHaveCount(0)
    await expect(page.getByRole('link', { name: /粤公网安备44088202000096号/ })).toHaveCount(0)
  })

  test('share page hides record footer', async ({ page }) => {
    await page.route('**/api/resume/share/public/*', async route => {
      await route.fulfill({ json: { code: 0, data: { expired: true } } })
    })

    await page.goto('/share/demo')

    await expect(page.getByRole('link', { name: '粤ICP备2026028310号-2' })).toHaveCount(0)
    await expect(page.getByRole('link', { name: /粤公网安备44088202000096号/ })).toHaveCount(0)
  })
})
