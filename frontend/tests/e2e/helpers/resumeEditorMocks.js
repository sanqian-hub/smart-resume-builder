export async function mockResumeEditorPersistence(page, options = {}) {
  const {
    user = { id: 1, username: '测试用户' },
    initialResumeId = 101,
    notices = [],
    chatHistory = [],
    seedLocalUser = false,
  } = options

  const clone = (value) => JSON.parse(JSON.stringify(value))
  function cloneSeedNotices(list = []) {
    return clone(list.map(item => ({
      ...item,
      isRead: item.isRead ? 1 : 0,
    })))
  }

  const state = {
    user: clone(user),
    resumeId: initialResumeId,
    resume: null,
    versions: [],
    versionNum: 0,
    shares: [],
    nextShareId: 1,
    notices: cloneSeedNotices(notices),
    chatHistory: clone(chatHistory),
  }

  const ok = (data) => ({ code: 0, data })
  const isoAt = (days) => {
    if (!days) return null
    const date = new Date('2026-05-06T16:00:00')
    date.setDate(date.getDate() + days)
    return date.toISOString()
  }

  const buildResumeSnapshot = () => clone({
    id: state.resumeId,
    title: state.resume?.title || '未命名简历',
    currentTemplate: state.resume?.currentTemplate,
    styleConfig: state.resume?.styleConfig || '',
    contents: state.resume?.contents || [],
  })

  if (seedLocalUser) {
    await page.addInitScript((seedUser) => {
      localStorage.setItem('user', JSON.stringify(seedUser))
    }, clone(state.user))
  }

  await page.route('**/api/user/current', async route => {
    await route.fulfill({ json: ok(clone(state.user)) })
  })

  await page.route('**/api/user/login', async route => {
    await route.fulfill({ json: ok(clone(state.user)) })
  })

  await page.route('**/api/user/update', async route => {
    const payload = route.request().postDataJSON()
    state.user = {
      ...state.user,
      ...payload,
    }
    await route.fulfill({ json: ok(clone(state.user)) })
  })


  await page.route('**/api/resume/my/list', async route => {
    const list = state.resume
      ? [
          {
            id: state.resumeId,
            title: state.resume.title,
            currentTemplate: state.resume.currentTemplate,
            updateTime: '2026-05-06 16:00:00',
          },
        ]
      : []
    await route.fulfill({ json: ok(list) })
  })

  await page.route('**/api/notice/unread-count', async route => {
    await route.fulfill({ json: ok(state.notices.filter(item => !item.isRead).length) })
  })

  await page.route('**/api/notice/list', async route => {
    await route.fulfill({ json: ok(clone(state.notices)) })
  })

  await page.route('**/api/notice/read-all', async route => {
    state.notices = state.notices.map(item => ({ ...item, isRead: 1 }))
    await route.fulfill({ json: ok(true) })
  })

  await page.route('**/api/notice/read/*', async route => {
    const match = route.request().url().match(/\/read\/(\d+)/)
    const noticeId = Number(match?.[1] || 0)
    state.notices = state.notices.map(item => (
      item.id === noticeId
        ? { ...item, isRead: 1 }
        : item
    ))
    await route.fulfill({ json: ok(true) })
  })

  await page.route('**/api/ai/memory/list', async route => {
    await route.fulfill({ json: ok([]) })
  })

  await page.route('**/api/ai/chat/history**', async route => {
    await route.fulfill({ json: ok(clone(state.chatHistory)) })
  })

  await page.route('**/api/ai/chat/clear**', async route => {
    state.chatHistory = []
    await route.fulfill({ json: ok(true) })
  })

  await page.route('**/api/ai/chat/stream', async route => {
    const payload = route.request().postDataJSON()
    const assistantReply = {
      role: 'assistant',
      content: '这是 AI 返回的简历优化建议。',
    }
    state.chatHistory = [
      ...state.chatHistory,
      { role: 'user', content: payload.message },
      assistantReply,
    ]
    await route.fulfill({
      status: 200,
      contentType: 'text/event-stream',
      headers: {
        'Cache-Control': 'no-cache',
        Connection: 'keep-alive',
      },
      body: [
        `data: ${JSON.stringify({ content: '这是 AI 返回的简历优化建议。' })}`,
        'data: [DONE]',
        '',
      ].join('\n\n'),
    })
  })

  await page.route('**/api/resume/add', async route => {
    await route.fulfill({ json: ok(state.resumeId) })
  })

  await page.route('**/api/resume/update', async route => {
    const payload = route.request().postDataJSON()
    state.resume = {
      id: state.resumeId,
      title: payload.title,
      currentTemplate: payload.currentTemplate,
      styleConfig: payload.styleConfig || '',
      contents: payload.contents || [],
    }
    await route.fulfill({ json: ok(true) })
  })

  await page.route('**/api/resume/get/*', async route => {
    await route.fulfill({ json: ok(state.resume) })
  })

  await page.route('**/api/resume/version/save**', async route => {
    state.versionNum += 1
    const snapshot = JSON.stringify({
      template: state.resume?.currentTemplate,
      styleConfig: state.resume?.styleConfig || '',
      contents: state.resume?.contents || [],
    })
    const version = {
      id: state.versionNum,
      versionNum: state.versionNum,
      remark: '手动保存',
      createTime: '2026-05-06T16:00:00',
      snapshotJson: snapshot,
    }
    state.versions = [version, ...state.versions]
    await route.fulfill({ json: ok(version) })
  })

  await page.route('**/api/resume/version/list**', async route => {
    await route.fulfill({ json: ok(state.versions) })
  })

  await page.route('**/api/notice/analyze/**', async route => {
    await route.fulfill({ json: ok(true) })
  })

  await page.route('**/api/resume/match', async route => {
    await route.fulfill({
      json: ok({
        overallScore: 82,
        dimensions: [
          { name: '技能匹配', score: 85, analysis: '核心技能覆盖较好', suggestion: '补充更多项目细节会更完整' },
          { name: '经历相关度', score: 78, analysis: '经历与目标岗位存在明显相关性', suggestion: '' },
        ],
        missingSkills: ['系统设计'],
        highlights: ['具备项目落地经验', '有明确技术栈优势'],
        summary: '整体匹配度较高，建议补充系统设计相关案例。',
      }),
    })
  })

  await page.route('**/api/resume/self-intro', async route => {
    const payload = route.request().postDataJSON()
    await route.fulfill({
      json: ok({
        title: '面试自我介绍',
        content: `您好，我是${state.resume?.contents?.[0]?.contentJson ? '候选人' : '候选人'}，我希望应聘这个岗位，并结合 ${payload.style} 风格做了准备。`,
      }),
    })
  })

  await page.route('**/api/resume/share/create**', async route => {
    const requestUrl = new URL(route.request().url())
    const versionId = Number(requestUrl.searchParams.get('versionId') || 0)
    const password = requestUrl.searchParams.get('password') || ''
    const expireDays = Number(requestUrl.searchParams.get('expireDays') || 0)
    const version = state.versions.find(item => item.id === versionId) || state.versions[0] || null
    const share = {
      id: state.nextShareId,
      shareKey: `share-${state.nextShareId}`,
      sourceVersionNum: version?.versionNum || state.versionNum || 1,
      password,
      status: 1,
      expired: false,
      expireTime: isoAt(expireDays),
      createTime: '2026-05-06T16:00:00',
      viewCount: 0,
      resume: buildResumeSnapshot(),
    }
    state.nextShareId += 1
    state.shares = [share, ...state.shares]
    await route.fulfill({ json: ok(share.shareKey) })
  })

  await page.route('**/api/resume/share/list**', async route => {
    await route.fulfill({ json: ok(clone(state.shares)) })
  })

  await page.route('**/api/resume/share/close**', async route => {
    const requestUrl = new URL(route.request().url())
    const shareId = Number(requestUrl.searchParams.get('shareId') || 0)
    state.shares = state.shares.map(item => (
      item.id === shareId
        ? { ...item, status: 0 }
        : item
    ))
    await route.fulfill({ json: ok(true) })
  })

  await page.route('**/api/resume/share/password**', async route => {
    const requestUrl = new URL(route.request().url())
    const shareId = Number(requestUrl.searchParams.get('shareId') || 0)
    const password = requestUrl.searchParams.get('password') || ''
    let updatedShare = null
    state.shares = state.shares.map(item => {
      if (item.id !== shareId) return item
      updatedShare = { ...item, password }
      return updatedShare
    })
    await route.fulfill({ json: ok(updatedShare) })
  })

  await page.route('**/api/resume/share/expire**', async route => {
    const requestUrl = new URL(route.request().url())
    const shareId = Number(requestUrl.searchParams.get('shareId') || 0)
    const expireDays = Number(requestUrl.searchParams.get('expireDays') || 0)
    let updatedShare = null
    state.shares = state.shares.map(item => {
      if (item.id !== shareId) return item
      updatedShare = {
        ...item,
        expireTime: isoAt(expireDays),
        expired: false,
      }
      return updatedShare
    })
    await route.fulfill({ json: ok(updatedShare) })
  })

  await page.route('**/api/resume/share/public/*/verify**', async route => {
    const match = route.request().url().match(/\/public\/([^/?]+)\/verify/)
    const shareKey = match?.[1]
    const requestUrl = new URL(route.request().url())
    const password = requestUrl.searchParams.get('password') || ''
    const share = state.shares.find(item => item.shareKey === shareKey)
    if (!share || share.status !== 1 || share.expired) {
      await route.fulfill({ json: ok({ expired: true }) })
      return
    }
    if (share.password !== password) {
      await route.fulfill({ json: { code: 1, message: '访问密码错误' } })
      return
    }
    share.viewCount += 1
    await route.fulfill({ json: ok(clone(share.resume)) })
  })

  await page.route('**/api/resume/share/public/*', async route => {
    const match = route.request().url().match(/\/public\/([^/?]+)/)
    const shareKey = match?.[1]
    const share = state.shares.find(item => item.shareKey === shareKey)
    if (!share || share.status !== 1 || share.expired) {
      await route.fulfill({ json: ok({ expired: true, needPassword: false, resume: null }) })
      return
    }
    if (share.password) {
      await route.fulfill({ json: ok({ expired: false, needPassword: true, resume: null }) })
      return
    }
    share.viewCount += 1
    await route.fulfill({ json: ok({ expired: false, needPassword: false, resume: clone(share.resume) }) })
  })
}
