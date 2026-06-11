import { beforeEach, describe, expect, test, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/vue'
import { createPinia } from 'pinia'
import { defineComponent, nextTick } from 'vue'
import ResumeEdit from '../../src/views/resume/ResumeEdit.vue'

const GUEST_DRAFT_KEY = 'guest-resume-draft'

const {
  routeMock,
  routerReplaceMock,
  getResumeMock,
  addResumeMock,
  updateResumeMock,
  listVersionsMock,
  analyzeAndNotifyMock,
  createShareMock,
  listSharesMock,
  closeShareMock,
  updateSharePasswordMock,
  updateShareExpireMock,
  getCurrentUserMock,
  updateMyInfoMock,
  fetchUnreadMock,
} = vi.hoisted(() => ({
  routeMock: { params: {} },
  routerReplaceMock: vi.fn(),
  getResumeMock: vi.fn(),
  addResumeMock: vi.fn(),
  updateResumeMock: vi.fn(),
  listVersionsMock: vi.fn(),
  analyzeAndNotifyMock: vi.fn(),
  createShareMock: vi.fn(),
  listSharesMock: vi.fn(),
  closeShareMock: vi.fn(),
  updateSharePasswordMock: vi.fn(),
  updateShareExpireMock: vi.fn(),
  getCurrentUserMock: vi.fn(),
  updateMyInfoMock: vi.fn(),
  fetchUnreadMock: vi.fn(),
}))

vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRoute: () => routeMock,
    useRouter: () => ({
      replace: routerReplaceMock,
      push: vi.fn(),
    }),
  }
})

vi.mock('../../src/api/resume', () => ({
  getResume: getResumeMock,
  addResume: addResumeMock,
  updateResume: updateResumeMock,
}))

vi.mock('../../src/api/version', () => ({
  saveVersion: vi.fn(),
  listVersions: listVersionsMock,
}))

vi.mock('../../src/api/notice', () => ({
  analyzeAndNotify: analyzeAndNotifyMock,
}))

vi.mock('../../src/api/share', () => ({
  createShare: createShareMock,
  listShares: listSharesMock,
  closeShare: closeShareMock,
  updateSharePassword: updateSharePasswordMock,
  updateShareExpire: updateShareExpireMock,
}))

vi.mock('../../src/api/user', () => ({
  getCurrentUser: getCurrentUserMock,
  updateMyInfo: updateMyInfoMock,
}))

vi.mock('../../src/composables/useResumeListCache', () => ({
  useResumeListCache: () => ({
    refresh: vi.fn(),
  }),
}))

vi.mock('../../src/composables/useNotice', () => ({
  useNotice: () => ({
    unreadCount: { value: 0 },
    fetchUnread: fetchUnreadMock,
  }),
}))

vi.mock('../../src/views/resume/templateRegistry', () => ({
  DEFAULT_TEMPLATE_ID: 'classic-1',
  normalizeTemplateId: (id) => id || 'classic-1',
  TEMPLATE_OPTIONS: [
    { id: 'classic-1', label: '经典模板01', color: '#4672f2', family: 'classic' },
  ],
  TEMPLATE_LABELS: { 'classic-1': '经典模板01' },
  TEMPLATE_COLORS: { 'classic-1': '#4672f2' },
  TEMPLATE_COMPONENTS: {
    'classic-1': defineComponent({
      name: 'MockResumeTemplate',
      template: `
        <div data-testid="mock-template">
          <section class="profile">基本信息预览</section>
          <section class="educationList">教育经历预览</section>
        </div>
      `,
      props: ['contents', 'themeColor', 'fontSize', 'fontFamily', 'lineHeight'],
    }),
  },
}))

vi.mock('../../src/views/resume/styleConfig', () => ({
  DEFAULT_RICH_FONT_FAMILY: 'inherit',
  DEFAULT_RICH_FONT_SIZE: '16px',
  DEFAULT_RICH_LINE_HEIGHT: '2x',
  DEFAULT_THEME_COLOR: '#4672f2',
  hasStoredStyleConfig: () => false,
  readStyleConfig: () => ({
    themeColor: '#4672f2',
    richFontSize: '16px',
    richFontFamily: 'inherit',
    richLineHeight: '2x',
  }),
  serializeStyleConfig: () => '',
  stripLegacyStyleFieldsFromBasic: (value) => value,
}))

describe('游客草稿恢复', () => {
  beforeEach(() => {
    routeMock.params = {}
    routerReplaceMock.mockReset()
    getResumeMock.mockReset()
    addResumeMock.mockReset()
    updateResumeMock.mockReset()
    listVersionsMock.mockReset()
    analyzeAndNotifyMock.mockReset()
    createShareMock.mockReset()
    listSharesMock.mockReset()
    closeShareMock.mockReset()
    updateSharePasswordMock.mockReset()
    updateShareExpireMock.mockReset()
    getCurrentUserMock.mockReset()
    updateMyInfoMock.mockReset()
    fetchUnreadMock.mockReset()
    listVersionsMock.mockResolvedValue([])
    listSharesMock.mockResolvedValue([])
    getCurrentUserMock.mockResolvedValue({ id: 1, username: '测试用户' })
    analyzeAndNotifyMock.mockResolvedValue(undefined)
    fetchUnreadMock.mockResolvedValue(undefined)
    localStorage.clear()
    sessionStorage.clear()
  })

  test('已有简历重新进入后，长标题输入框不会停留在最小宽度', async () => {
    routeMock.params = { id: 'resume-1' }
    getResumeMock.mockResolvedValue({
      id: 'resume-1',
      title: '这是一个很长的简历标题用于验证重新进入后的宽度初始化',
      currentTemplate: 'classic-1',
      styleConfig: '',
      contents: [],
    })

    const offsetWidthGetter = Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'offsetWidth')
    Object.defineProperty(HTMLElement.prototype, 'offsetWidth', {
      configurable: true,
      get() {
        if (this.classList?.contains('title-sizer')) return 220
        if (this.classList?.contains('title-edit-icon')) return 14
        return offsetWidthGetter?.get ? offsetWidthGetter.get.call(this) : 0
      },
    })

    vi.useFakeTimers()

    try {
      render(ResumeEdit, {
        global: {
          plugins: [createPinia()],
          stubs: {
            ModuleSelector: true,
            BasicEditor: true,
            EducationEditor: true,
            ExperienceEditor: true,
            ProjectEditor: true,
            SkillEditor: true,
            SelfIntroEditor: true,
            AwardEditor: true,
            PortfolioEditor: true,
            OtherEditor: true,
            AiChatDialog: true,
            MatchDialog: true,
            ResumeScoreDialog: true,
            SelfIntroDialog: true,
            TemplateSelectDialog: true,
            NoticeCenterDialog: true,
            Teleport: true,
          },
        },
      })

      await vi.advanceTimersByTimeAsync(500)
      await nextTick()

      const titleInput = await screen.findByDisplayValue('这是一个很长的简历标题用于验证重新进入后的宽度初始化')
      expect(titleInput.style.width).not.toBe('80px')
      expect(Number.parseInt(titleInput.style.width, 10)).toBeGreaterThan(80)
    } finally {
      vi.useRealTimers()
      if (offsetWidthGetter) {
        Object.defineProperty(HTMLElement.prototype, 'offsetWidth', offsetWidthGetter)
      } else {
        delete HTMLElement.prototype.offsetWidth
      }
    }
  })

  test('模块行操作按钮区常驻 DOM，不再通过 v-show 注入 display none', async () => {
    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    await nextTick()

    const actions = document.querySelector('.accordion-actions')
    expect(actions).not.toBeNull()
    expect(actions?.getAttribute('style') || '').not.toContain('display: none')
  })

  test('点击模块只展开编辑区，不再驱动右侧预览自动滚动', async () => {
    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    await screen.findByRole('button', { name: '教育经历' })

    const previewScroll = document.querySelector('.preview-scroll')
    const scrollToMock = vi.fn()
    previewScroll.scrollTo = scrollToMock
    Object.defineProperty(previewScroll, 'offsetWidth', {
      configurable: true,
      value: 794,
    })

    const educationSection = document.querySelector('.educationList')
    Object.defineProperty(educationSection, 'offsetTop', {
      configurable: true,
      value: 320,
    })

    await fireEvent.click(screen.getByRole('button', { name: '教育经历' }))
    await nextTick()

    expect(scrollToMock).not.toHaveBeenCalled()
  })

  test('未认证访问 /edit 时点击继续编辑后才恢复本地游客草稿标题', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '游客草稿标题',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    await waitFor(() => {
      expect(screen.getByText('发现上次草稿')).toBeVisible()
    })
    expect(screen.queryByDisplayValue('游客草稿标题')).not.toBeInTheDocument()
    await screen.getByRole('button', { name: '继续编辑' }).click()
    expect(await screen.findByDisplayValue('游客草稿标题')).toBeVisible()
  })

  test('有效游客草稿的恢复提示会在页面稳定后的下一帧再显示，避免刷新首帧闪动', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '下一帧再显示的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    vi.useFakeTimers()

    try {
      render(ResumeEdit, {
        global: {
          plugins: [createPinia()],
          stubs: {
            ModuleSelector: true,
            BasicEditor: true,
            EducationEditor: true,
            ExperienceEditor: true,
            ProjectEditor: true,
            SkillEditor: true,
            SelfIntroEditor: true,
            AwardEditor: true,
            PortfolioEditor: true,
            OtherEditor: true,
            AiChatDialog: true,
            MatchDialog: true,
            ResumeScoreDialog: true,
            SelfIntroDialog: true,
            TemplateSelectDialog: true,
            NoticeCenterDialog: true,
            Teleport: true,
          },
        },
      })

      const promptOverlay = document.querySelector('.guest-draft-modal-overlay')
      expect(promptOverlay).not.toBeNull()
      expect(promptOverlay.style.display).toBe('none')
      expect(promptOverlay).not.toBeVisible()
      await vi.runAllTimersAsync()
      await nextTick()

      expect(screen.getByText('发现上次草稿')).toBeVisible()
      expect(document.querySelector('.guest-draft-modal-overlay')).toBeVisible()
    } finally {
      vi.useRealTimers()
    }
  })

  test('应用启动遮罩未退场前不会显示恢复弹窗，避免与冷启动淡入层叠加闪烁', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '等待启动遮罩退场的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    const appRoot = document.createElement('div')
    appRoot.id = 'app'
    document.body.appendChild(appRoot)
    const bootLoader = document.createElement('div')
    bootLoader.id = 'app-loader'
    document.body.appendChild(bootLoader)

    vi.useFakeTimers()

    try {
      render(ResumeEdit, {
        global: {
          plugins: [createPinia()],
          stubs: {
            ModuleSelector: true,
            BasicEditor: true,
            EducationEditor: true,
            ExperienceEditor: true,
            ProjectEditor: true,
            SkillEditor: true,
            SelfIntroEditor: true,
            AwardEditor: true,
            PortfolioEditor: true,
            OtherEditor: true,
            AiChatDialog: true,
            MatchDialog: true,
            ResumeScoreDialog: true,
            SelfIntroDialog: true,
            TemplateSelectDialog: true,
            NoticeCenterDialog: true,
            Teleport: true,
          },
        },
      })

      await vi.advanceTimersByTimeAsync(500)
      await nextTick()
      expect(screen.queryByText('发现上次草稿')).not.toBeVisible()
      expect(document.querySelector('.guest-draft-modal-overlay').style.display).toBe('none')

      bootLoader.remove()
      appRoot.classList.add('loaded')

      await vi.advanceTimersByTimeAsync(100)
      await nextTick()
      expect(screen.getByText('发现上次草稿')).toBeVisible()
    } finally {
      vi.useRealTimers()
      bootLoader.remove()
      appRoot.remove()
    }
  })

  test('未认证访问 /edit 时如果存在有效游客草稿，只提示恢复而不自动覆盖默认简历', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '需要确认的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    await waitFor(() => {
      expect(screen.getByText('发现上次草稿')).toBeVisible()
    })
    expect(screen.getByRole('button', { name: '继续编辑' })).toBeVisible()
    expect(screen.queryByDisplayValue('需要确认的游客草稿')).not.toBeInTheDocument()
    await screen.getByRole('button', { name: '继续编辑' }).click()
    expect(await screen.findByDisplayValue('需要确认的游客草稿')).toBeVisible()
  })

  test('本地 user 存在但会话失效时，仍会提示恢复游客草稿', async () => {
    localStorage.setItem('user', JSON.stringify({ id: 1, username: '测试用户' }))
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '会话过期草稿标题',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))
    getCurrentUserMock.mockRejectedValueOnce(new Error('not login'))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    await waitFor(() => {
      expect(screen.getByText('发现上次草稿')).toBeVisible()
    })
    expect(screen.queryByDisplayValue('会话过期草稿标题')).not.toBeInTheDocument()
    await screen.getByRole('button', { name: '继续编辑' }).click()
    expect(await screen.findByDisplayValue('会话过期草稿标题')).toBeVisible()
  })

  test('本地 user 且会话有效时，新建编辑页不会被旧游客草稿覆盖', async () => {
    localStorage.setItem('user', JSON.stringify({ id: 1, username: '测试用户' }))
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '不应被采用的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    expect(await screen.findByDisplayValue('未命名简历')).toBeVisible()
  })

  test('登录后首次正式保存成功时会清掉游客草稿', async () => {
    localStorage.setItem('user', JSON.stringify({ id: 1, username: '测试用户' }))
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '待转正游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))
    sessionStorage.setItem('resume-pending-login-intent', 'save')
    sessionStorage.setItem('resume-pending-guest-payload', JSON.stringify({
      title: '待转正游客草稿',
      currentTemplate: 'classic-1',
      styleConfig: '',
      contents: [],
    }))
    addResumeMock.mockResolvedValue(101)
    updateResumeMock.mockResolvedValue(undefined)

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    expect(await screen.findByDisplayValue('待转正游客草稿')).toBeVisible()
    await (await screen.findByRole('button', { name: '保存' })).click()

    await waitFor(() => {
      expect(addResumeMock).toHaveBeenCalled()
    })
    await waitFor(() => {
      expect(localStorage.getItem(GUEST_DRAFT_KEY)).toBeNull()
    })
  })

  test('刷新前会从标题输入框同步最新值后再写入游客草稿', async () => {
    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    const titleInput = await screen.findByPlaceholderText('简历标题')
    titleInput.value = '刷新前最后一刻的标题'

    await fireEvent(window, new Event('pagehide'))

    const savedDraft = JSON.parse(localStorage.getItem(GUEST_DRAFT_KEY))
    expect(savedDraft.payload.title).toBe('刷新前最后一刻的标题')
  })

  test('页面切到 hidden 时会立刻写入游客草稿，避免刷新时序丢失', async () => {
    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    const titleInput = await screen.findByPlaceholderText('简历标题')
    titleInput.value = 'hidden 时立即保存'

    const original = Object.getOwnPropertyDescriptor(document, 'visibilityState')
    Object.defineProperty(document, 'visibilityState', {
      configurable: true,
      value: 'hidden',
    })

    await fireEvent(document, new Event('visibilitychange'))

    const savedDraft = JSON.parse(localStorage.getItem(GUEST_DRAFT_KEY))
    expect(savedDraft.payload.title).toBe('hidden 时立即保存')

    if (original) {
      Object.defineProperty(document, 'visibilityState', original)
    }
  })

  test('点击重新开始后不会把默认数据重新保存成新的游客草稿', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '需要放弃的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    await waitFor(() => {
      expect(screen.getByText('发现上次草稿')).toBeVisible()
    })
    await screen.getByRole('button', { name: '重新开始' }).click()
    await fireEvent(window, new Event('pagehide'))
    expect(localStorage.getItem(GUEST_DRAFT_KEY)).toBeNull()
  })

  test('发现上次草稿弹窗的遮罩层自带关键内联定位样式，避免刷新首帧闪到页面底部', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '需要提示的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    const dialogTitle = await screen.findByText('发现上次草稿')
    const overlay = dialogTitle.closest('.guest-draft-modal-overlay')
    expect(overlay).not.toBeNull()
    expect(overlay.getAttribute('style')).toContain('position: fixed')
    expect(overlay.getAttribute('style')).toContain('inset: 0')
    expect(overlay.getAttribute('style')).toContain('z-index: 10010')
  })

  test('发现上次草稿弹窗使用纯色遮罩而不是 backdrop-filter，避免刷新首帧合成闪烁', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '需要提示的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    const dialogTitle = await screen.findByText('发现上次草稿')
    const overlay = dialogTitle.closest('.guest-draft-modal-overlay')
    expect(overlay).not.toBeNull()
    expect(overlay.style.background).toBe('rgba(0, 0, 0, 0.4)')
    expect(overlay.getAttribute('style')).not.toContain('backdrop-filter')
  })

  test('发现上次草稿弹窗主体自带关键内联布局样式，避免首帧以默认文档流形态闪现', async () => {
    localStorage.setItem(GUEST_DRAFT_KEY, JSON.stringify({
      version: 1,
      updatedAt: '2026-05-08T09:00:00.000Z',
      payload: {
        title: '需要提示的游客草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    render(ResumeEdit, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ModuleSelector: true,
          BasicEditor: true,
          EducationEditor: true,
          ExperienceEditor: true,
          ProjectEditor: true,
          SkillEditor: true,
          SelfIntroEditor: true,
          AwardEditor: true,
          PortfolioEditor: true,
          OtherEditor: true,
          AiChatDialog: true,
          MatchDialog: true,
          ResumeScoreDialog: true,
          SelfIntroDialog: true,
          TemplateSelectDialog: true,
          NoticeCenterDialog: true,
          Teleport: true,
        },
      },
    })

    const dialogTitle = await screen.findByText('发现上次草稿')
    const box = dialogTitle.closest('.guest-draft-modal-box')
    expect(box).not.toBeNull()
    const style = box.getAttribute('style') || ''
    expect(style).toContain('width: 360px')
    expect(style).toContain('background: rgb(255, 255, 255)')
    expect(style).toContain('border-radius: 14px')
    expect(style).toContain('text-align: center')
  })
})
