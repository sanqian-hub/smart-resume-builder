import { describe, expect, test, vi, afterEach } from 'vitest'
import { chatStream } from '../../src/api/ai'

function createStreamResponse(chunks) {
  const encoder = new TextEncoder()
  let index = 0
  return {
    ok: true,
    body: {
      getReader() {
        return {
          read: vi.fn(async () => {
            if (index >= chunks.length) return { done: true, value: undefined }
            const value = encoder.encode(chunks[index])
            index += 1
            return { done: false, value }
          }),
        }
      },
    },
  }
}

describe('AI API suggest stream', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.useRealTimers()
  })

  test('forwards itemIndex from suggest events', async () => {
    vi.useFakeTimers()
    const onSuggest = vi.fn()
    const fetchMock = vi.fn().mockResolvedValue(createStreamResponse([
      'data: {"type":"suggest","moduleType":"experience","itemIndex":1,"content":"{\\"company\\":\\"第二家公司\\"}"}\n\n',
      'data: [DONE]\n\n',
    ]))
    vi.stubGlobal('fetch', fetchMock)

    chatStream(101, '优化第二段工作经历', 'modify', {}, {
      onSuggest,
    })

    await vi.runAllTimersAsync()
    await Promise.resolve()

    expect(onSuggest).toHaveBeenCalledWith('experience', '{"company":"第二家公司"}', 1)
  })
})
