import { describe, expect, test, vi } from 'vitest'
import { render, screen } from '@testing-library/vue'
import ResumeShareView from '../../src/views/resume/ResumeShareView.vue'

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { shareKey: 'demo' },
  }),
}))

vi.mock('../../src/api/share', () => ({
  getPublicShare: vi.fn().mockResolvedValue({ expired: true }),
  verifyPublicShare: vi.fn(),
}))

vi.mock('../../src/views/resume/styleConfig', () => ({
  readStyleConfig: () => ({
    themeColor: '#4672f2',
    richFontFamily: 'inherit',
    richFontSize: '14px',
    richLineHeight: '1.6',
  }),
}))

vi.mock('../../src/views/resume/templateRegistry', () => ({
  DEFAULT_TEMPLATE_ID: 'classic-1',
  normalizeTemplateId: (id) => id || 'classic-1',
  TEMPLATE_COMPONENTS: {
    'classic-1': { template: '<div />' },
  },
}))

describe('分享页页脚', () => {
  test('分享页不展示备案页脚', async () => {
    render(ResumeShareView)

    expect(screen.queryByRole('link', { name: '粤ICP备2026028310号-2' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /粤公网安备44088202000096号/ })).not.toBeInTheDocument()
  })
})
