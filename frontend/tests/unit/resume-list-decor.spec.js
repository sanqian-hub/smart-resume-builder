import { beforeEach, describe, expect, test, vi } from 'vitest'
import { render, screen } from '@testing-library/vue'
import { createPinia } from 'pinia'
import ResumeList from '../../src/views/resume/ResumeList.vue'

const { myListMock } = vi.hoisted(() => ({
  myListMock: vi.fn(),
}))

vi.mock('../../src/api/resume', () => ({
  myList: myListMock,
  deleteResume: vi.fn(),
}))

vi.mock('../../src/api/version', () => ({
  listVersions: vi.fn(),
}))

vi.mock('../../src/api/share', () => ({
  createShare: vi.fn(),
}))

vi.mock('../../src/composables/useResumeListCache', () => ({
  useResumeListCache: () => ({
    cache: { value: [] },
    refresh: vi.fn(),
    clear: vi.fn(),
  }),
}))

vi.mock('../../src/composables/useRuntimeLoader', () => ({
  useRuntimeLoader: () => ({
    hide: vi.fn(),
  }),
}))

vi.mock('../../src/views/resume/styleConfig', () => ({
  readSnapshotPresentation: () => ({
    template: 'classic-1',
    themeColor: '#4672f2',
    richFontFamily: 'inherit',
    richFontSize: '14px',
    richLineHeight: '1.6',
  }),
}))

vi.mock('../../src/views/resume/listShareDialogState', () => ({
  getListShareConfirmUi: () => ({ label: '分享', loading: false, success: false }),
  shouldKeepListShareDialogOpen: () => false,
}))

vi.mock('../../src/views/resume/templateRegistry', () => ({
  DEFAULT_TEMPLATE_ID: 'classic-1',
  normalizeTemplateId: (id) => id || 'classic-1',
  TEMPLATE_COLORS: { 'classic-1': '#4672f2' },
  TEMPLATE_COMPONENTS: { 'classic-1': { template: '<div />' } },
  TEMPLATE_LABELS: { 'classic-1': '经典模板01' },
}))

describe('首页列表装饰', () => {
  beforeEach(() => {
    myListMock.mockResolvedValue([])
  })

  test('在桌面端展示轻装饰文案和插画说明', async () => {
    render(ResumeList, {
      global: {
        plugins: [createPinia()],
        mocks: {
          $router: { push: vi.fn() },
        },
      },
    })

    expect(screen.getByText('今天也把亮点写出来')).toBeVisible()
    expect(screen.getByText('别让好项目躲在角落')).toBeVisible()
    expect(screen.getByText('简历也能有一点小脾气')).toBeVisible()
  })
})
