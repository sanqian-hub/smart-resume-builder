import { describe, expect, test, vi } from 'vitest'
import { inlineImageSourcesForExport } from '../../src/views/resume/exportInlineAssets'

class FakeFileReader {
  readAsDataURL(blob) {
    this.result = `data:image/png;base64,${blob.__mockBase64}`
    queueMicrotask(() => this.onload?.({ target: this }))
  }
}

describe('export inline assets', () => {
  test('replaces image urls with data urls and restores them afterwards', async () => {
    document.body.innerHTML = `
      <div id="root">
        <img id="avatar" src="https://example.com/avatar.png">
      </div>
    `

    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      blob: async () => ({ __mockBase64: 'avatar-inline' }),
    })

    const root = document.getElementById('root')
    const image = document.getElementById('avatar')

    const restore = await inlineImageSourcesForExport(root, {
      fetchImpl: fetchMock,
      FileReaderCtor: FakeFileReader,
    })

    expect(fetchMock).toHaveBeenCalledWith('https://example.com/avatar.png')
    expect(image.getAttribute('src')).toBe('data:image/png;base64,avatar-inline')

    restore()

    expect(image.getAttribute('src')).toBe('https://example.com/avatar.png')
  })

  test('keeps existing data urls untouched', async () => {
    document.body.innerHTML = `
      <div id="root">
        <img id="avatar" src="data:image/png;base64,already-inline">
      </div>
    `

    const fetchMock = vi.fn()
    const root = document.getElementById('root')
    const image = document.getElementById('avatar')

    const restore = await inlineImageSourcesForExport(root, {
      fetchImpl: fetchMock,
      FileReaderCtor: FakeFileReader,
    })

    expect(fetchMock).not.toHaveBeenCalled()
    expect(image.getAttribute('src')).toBe('data:image/png;base64,already-inline')

    restore()

    expect(image.getAttribute('src')).toBe('data:image/png;base64,already-inline')
  })
})
