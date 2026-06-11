import { ref } from 'vue'
import { myList } from '../api/resume'

const cache = ref(null)

export function useResumeListCache() {
  async function refresh() {
    cache.value = await myList()
    return cache.value
  }

  function clear() {
    cache.value = null
  }

  return { cache, refresh, clear }
}
