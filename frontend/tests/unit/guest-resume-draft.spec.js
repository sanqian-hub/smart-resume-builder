import { beforeEach, describe, expect, test, vi } from 'vitest'
import {
  clearGuestResumeDraft,
  getGuestResumeDraftKey,
  loadGuestResumeDraft,
  saveGuestResumeDraft,
} from '../../src/composables/useGuestResumeDraft'

describe('guest resume draft lifecycle', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.useRealTimers()
  })

  test('loads a fresh guest draft payload', () => {
    vi.setSystemTime(new Date('2026-05-08T12:00:00.000Z'))
    saveGuestResumeDraft({
      title: '新草稿',
      currentTemplate: 'classic-1',
      styleConfig: '',
      contents: [],
    })

    expect(loadGuestResumeDraft()).toEqual({
      title: '新草稿',
      currentTemplate: 'classic-1',
      styleConfig: '',
      contents: [],
    })
  })

  test('expires and clears guest drafts older than seven days', () => {
    localStorage.setItem(getGuestResumeDraftKey(), JSON.stringify({
      version: 1,
      updatedAt: '2026-04-30T12:00:00.000Z',
      payload: {
        title: '过期草稿',
        currentTemplate: 'classic-1',
        styleConfig: '',
        contents: [],
      },
    }))

    vi.setSystemTime(new Date('2026-05-08T12:00:00.000Z'))

    expect(loadGuestResumeDraft()).toBeNull()
    expect(localStorage.getItem(getGuestResumeDraftKey())).toBeNull()
  })

  test('clearGuestResumeDraft removes draft from localStorage', () => {
    saveGuestResumeDraft({
      title: '待清理草稿',
      currentTemplate: 'classic-1',
      styleConfig: '',
      contents: [],
    })

    clearGuestResumeDraft()

    expect(localStorage.getItem(getGuestResumeDraftKey())).toBeNull()
  })
})
