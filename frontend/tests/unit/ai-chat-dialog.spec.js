import { describe, expect, test, vi, beforeEach, afterEach } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/vue'

const {
  clearChatHistoryMock,
  getChatHistoryMock,
  chatStreamMock,
} = vi.hoisted(() => ({
  clearChatHistoryMock: vi.fn(),
  getChatHistoryMock: vi.fn(),
  chatStreamMock: vi.fn(),
}))

function installScrollMetrics(el, metrics) {
  let scrollTopValue = metrics.scrollTop
  Object.defineProperty(el, 'clientHeight', {
    configurable: true,
    get: () => metrics.clientHeight,
  })
  Object.defineProperty(el, 'scrollHeight', {
    configurable: true,
    get: () => metrics.scrollHeight,
  })
  Object.defineProperty(el, 'scrollTop', {
    configurable: true,
    get: () => scrollTopValue,
    set: (value) => {
      scrollTopValue = value
    },
  })
  return {
    get scrollTop() {
      return scrollTopValue
    },
    set scrollTop(value) {
      scrollTopValue = value
    },
  }
}

vi.mock('../../src/api/ai', () => ({
  chatStream: chatStreamMock,
  getChatHistory: getChatHistoryMock,
  clearChatHistory: clearChatHistoryMock,
}))

import AiChatDialog from '../../src/views/resume/editors/AiChatDialog.vue'

describe('AI 助手标题栏按钮', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
    vi.stubGlobal('requestAnimationFrame', (cb) => setTimeout(cb, 0))
    vi.stubGlobal('cancelAnimationFrame', (id) => clearTimeout(id))
    clearChatHistoryMock.mockResolvedValue(undefined)
    getChatHistoryMock.mockResolvedValue([])
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
    vi.unstubAllGlobals()
  })

  test('清空对话按钮使用危险态 class，关闭按钮保持普通样式', () => {
    render(AiChatDialog, {
      props: {
        visible: true,
        resumeId: null,
        moduleData: {},
      },
    })

    const clearButton = screen.getByTitle('清空对话')
    const closeButton = screen.getByTitle('关闭')

    expect(clearButton).toHaveClass('ai-header-btn')
    expect(clearButton).toHaveClass('ai-header-btn-danger')
    expect(closeButton).toHaveClass('ai-header-btn')
    expect(closeButton).not.toHaveClass('ai-header-btn-danger')
  })

  test('流式输出过程中清空对话不会触发残留动画异常', async () => {
    chatStreamMock.mockImplementation((resumeId, message, mode, moduleData, handlers) => {
      handlers.onMessage('这是一条用于测试清空时机的流式回复')
      handlers.onDone()
      return vi.fn()
    })

    render(AiChatDialog, {
      props: {
        visible: true,
        resumeId: 101,
        moduleData: {},
      },
    })

    const textarea = screen.getByPlaceholderText('描述你想要直接修改的简历内容...')
    await fireEvent.update(textarea, '帮我优化项目经历')
    await fireEvent.click(document.querySelector('.ai-send-btn.active'))
    await fireEvent.click(screen.getByTitle('清空对话'))

    expect(screen.getByText('有什么可以帮助你的？')).toBeVisible()
    expect(() => {
      vi.runAllTimers()
    }).not.toThrow()
  })

  test('打开弹窗时会渲染后端返回的历史消息', async () => {
    getChatHistoryMock.mockResolvedValue([
      { role: 'user', content: '帮我优化项目经历' },
      { role: 'assistant', content: '这是历史里的 AI 回复' },
    ])

    const view = render(AiChatDialog, {
      props: {
        visible: false,
        resumeId: 101,
        moduleData: {},
      },
    })

    await view.rerender({
      visible: true,
      resumeId: 101,
      moduleData: {},
    })

    expect(await screen.findByText('帮我优化项目经历')).toBeVisible()
    expect(await screen.findByText('这是历史里的 AI 回复')).toBeVisible()
  })

  test('输入框区域会展示机器人装饰挂件', () => {
    render(AiChatDialog, {
      props: {
        visible: true,
        resumeId: null,
        moduleData: {},
      },
    })

    expect(screen.getByAltText('AI 助手机器人挂件')).toBeVisible()
  })

  test('输入区不再渲染顶部硬分界线', () => {
    render(AiChatDialog, {
      props: {
        visible: true,
        resumeId: null,
        moduleData: {},
      },
    })

    const inputArea = document.querySelector('.ai-input-area')
    expect(inputArea).not.toBeNull()
    expect(inputArea.style.borderTopStyle).toBe('none')
    expect(inputArea.style.borderTopWidth).toBe('0px')
  })

  test('用户上滑离开底部后，流式输出不会强行把消息列表拉回底部', async () => {
    let streamHandlers
    chatStreamMock.mockImplementation((resumeId, message, mode, moduleData, handlers) => {
      streamHandlers = handlers
      return vi.fn()
    })

    render(AiChatDialog, {
      props: {
        visible: true,
        resumeId: 101,
        moduleData: {},
      },
    })

    const messagesEl = document.querySelector('.ai-messages')
    expect(messagesEl).not.toBeNull()
    const metrics = installScrollMetrics(messagesEl, {
      clientHeight: 300,
      scrollHeight: 900,
      scrollTop: 600,
    })

    const textarea = screen.getByPlaceholderText('描述你想要直接修改的简历内容...')
    await fireEvent.update(textarea, '帮我润色工作经历')
    await fireEvent.click(document.querySelector('.ai-send-btn.active'))

    metrics.scrollTop = 220
    await fireEvent.scroll(messagesEl)

    streamHandlers.onMessage('第一段流式回复')
    await vi.runAllTimersAsync()

    expect(metrics.scrollTop).toBe(220)
  })

  test('用户刚从底部轻微上滑时，即使仍接近底部也会立刻暂停自动跟随', async () => {
    let streamHandlers
    chatStreamMock.mockImplementation((resumeId, message, mode, moduleData, handlers) => {
      streamHandlers = handlers
      return vi.fn()
    })

    render(AiChatDialog, {
      props: {
        visible: true,
        resumeId: 101,
        moduleData: {},
      },
    })

    const messagesEl = document.querySelector('.ai-messages')
    expect(messagesEl).not.toBeNull()
    const metrics = installScrollMetrics(messagesEl, {
      clientHeight: 300,
      scrollHeight: 900,
      scrollTop: 600,
    })

    const textarea = screen.getByPlaceholderText('描述你想要直接修改的简历内容...')
    await fireEvent.update(textarea, '帮我继续优化工作经历')
    await fireEvent.click(document.querySelector('.ai-send-btn.active'))

    metrics.scrollTop = 560
    await fireEvent.scroll(messagesEl)

    streamHandlers.onMessage('第二段流式回复')
    await vi.runAllTimersAsync()

    expect(metrics.scrollTop).toBe(560)
  })
})
