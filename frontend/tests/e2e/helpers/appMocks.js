export async function mockAuthenticatedShell(page, options = {}) {
  const {
    user = { id: 1, username: '测试用户' },
    resumeList = [],
    unreadCount = 0,
    memoryList = [],
    persistUser = true,
  } = options

  if (persistUser) {
    await page.addInitScript((storedUser) => {
      localStorage.setItem('user', JSON.stringify(storedUser))
    }, user)
  }

  await page.route('**/api/user/current', async route => {
    await route.fulfill({ json: { code: 0, data: user } })
  })

  await page.route('**/api/resume/my/list', async route => {
    await route.fulfill({ json: { code: 0, data: resumeList } })
  })

  await page.route('**/api/notice/unread-count', async route => {
    await route.fulfill({ json: { code: 0, data: unreadCount } })
  })

  await page.route('**/api/ai/memory/list', async route => {
    await route.fulfill({ json: { code: 0, data: memoryList } })
  })
}
