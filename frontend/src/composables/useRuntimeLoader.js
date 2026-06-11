import { readonly, ref } from 'vue'

const visible = ref(false)
const message = ref('正在加载...')

export function useRuntimeLoader() {
  function show(nextMessage = '正在加载...') {
    message.value = nextMessage
    visible.value = true
  }

  function hide() {
    visible.value = false
  }

  return {
    visible: readonly(visible),
    message: readonly(message),
    show,
    hide,
  }
}
