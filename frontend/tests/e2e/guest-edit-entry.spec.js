import { expect, test } from '@playwright/test'

test('unauthenticated visitors can open /edit directly', async ({ page }) => {
  await page.goto('/edit')

  await expect(page).toHaveURL(/\/edit$/)
  await expect(page.getByRole('button', { name: '保存' })).toBeVisible()
})

test('unauthenticated visitors are still redirected away from /edit/:id', async ({ page }) => {
  await page.goto('/edit/123')

  await expect(page).toHaveURL(/\/login$/)
})
