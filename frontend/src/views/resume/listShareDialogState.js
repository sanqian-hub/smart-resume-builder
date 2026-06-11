export function getListShareConfirmUi(submitState, usePassword = false) {
  switch (submitState) {
    case 'submitting':
      return {
        label: usePassword ? '生成中...' : '分享中...',
        loading: true,
        success: false,
      }
    case 'copied':
      return {
        label: '已复制',
        loading: false,
        success: true,
      }
    case 'generated':
      return {
        label: '已生成',
        loading: false,
        success: true,
      }
    case 'error':
      return {
        label: '重新尝试',
        loading: false,
        success: false,
      }
    default:
      return {
        label: usePassword ? '生成链接' : '直接分享',
        loading: false,
        success: false,
      }
  }
}

export function shouldKeepListShareDialogOpen(submitState) {
  return submitState === 'copied' || submitState === 'generated'
}
