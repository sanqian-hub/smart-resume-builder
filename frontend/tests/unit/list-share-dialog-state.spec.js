import { describe, expect, it } from 'vitest'
import { getListShareConfirmUi, shouldKeepListShareDialogOpen } from '../../src/views/resume/listShareDialogState'

describe('list share dialog state', () => {
  it('keeps dialog open and shows copied state after successful direct share', () => {
    expect(shouldKeepListShareDialogOpen('copied')).toBe(true)
    expect(getListShareConfirmUi('copied', false)).toEqual({
      label: '已复制',
      loading: false,
      success: true,
    })
  })

  it('shows minimum loading copy during submit', () => {
    expect(getListShareConfirmUi('submitting', false)).toEqual({
      label: '分享中...',
      loading: true,
      success: false,
    })
  })
})
