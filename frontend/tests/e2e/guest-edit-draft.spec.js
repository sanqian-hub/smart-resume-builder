import { test, expect } from '@playwright/test'

test('guest edit draft shows restore prompt after reload and restores title after confirm', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    if (!sessionStorage.getItem('__guest-draft-test-init')) {
      localStorage.removeItem('guest-resume-draft')
      sessionStorage.setItem('__guest-draft-test-init', '1')
    }
  })

  await page.goto('/edit')

  const titleInput = page.locator('.title-input')
  await titleInput.fill('游客刷新后保留标题')

  await page.reload()

  await expect(page.getByText('发现上次草稿')).toBeVisible()
  await expect(page.locator('.title-input')).toHaveValue('未命名简历')
  await page.getByRole('button', { name: '继续编辑' }).click()
  await expect(page.locator('.title-input')).toHaveValue('游客刷新后保留标题')
})

test('guest edit draft can be discarded and should not reappear after another reload', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.removeItem('user')
    if (!sessionStorage.getItem('__guest-draft-test-init')) {
      localStorage.removeItem('guest-resume-draft')
      sessionStorage.setItem('__guest-draft-test-init', '1')
    }
  })

  await page.goto('/edit')

  await page.locator('.title-input').fill('需要放弃的游客草稿')
  await page.reload()

  await expect(page.getByText('发现上次草稿')).toBeVisible()
  await page.getByRole('button', { name: '重新开始' }).click()
  await expect(page.locator('.title-input')).toHaveValue('未命名简历')

  await page.reload()

  await expect(page.getByText('发现上次草稿')).not.toBeVisible()
  await expect(page.locator('.title-input')).toHaveValue('未命名简历')
})
