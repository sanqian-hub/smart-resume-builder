import { test, expect } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('match analysis integration', () => {
  test('analyzes a valid job description and renders structured result', async ({ page }) => {
    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()
    await page.getByRole('button', { name: '新建简历' }).click()

    await page.getByRole('button', { name: 'AI工具' }).click()
    await page.getByRole('button', { name: '岗位匹配' }).click()
    await expect(page.getByText('岗位匹配分析')).toBeVisible()

    await page.getByPlaceholder('将岗位描述（JD）粘贴到此处，包括岗位职责、任职要求等...').fill('负责后端系统设计与开发，要求具备 Java、Spring Boot、MySQL、分布式系统相关经验。')
    await page.getByRole('button', { name: '开始分析' }).click()

    await expect(page.getByText('综合评分')).toBeVisible()
    await expect(page.getByText('82')).toBeVisible()
    await expect(page.getByText('系统设计', { exact: true })).toBeVisible()
    await expect(page.getByText('整体匹配度较高，建议补充系统设计相关案例。')).toBeVisible()
  })
})
