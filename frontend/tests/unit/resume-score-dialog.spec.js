import { render, screen } from '@testing-library/vue'
import { describe, expect, test, vi } from 'vitest'
import ResumeScoreDialog from '../../src/views/resume/editors/ResumeScoreDialog.vue'

vi.mock('../../src/api/resume', () => ({
  scoreResume: vi.fn(),
}))

describe('简历打分弹窗', () => {
  test('开始打分按钮在空状态下显示打分语义图标而不是加号', async () => {
    const { container } = render(ResumeScoreDialog, {
      props: {
        visible: true,
        resumeId: 1,
        moduleData: {},
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await screen.findByRole('button', { name: '开始打分' })
    const buttonIconPath = container.querySelector('.score-analyze-btn svg path')
    const pathSignature = buttonIconPath?.getAttribute('d') || ''

    expect(pathSignature).not.toBe('M12 3v18')
  })
})
