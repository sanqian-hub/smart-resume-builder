import { ref } from 'vue'
import { getMemoryList } from '../api/ai'

const cache = ref(null)

export function useMemoryCache() {
  async function refresh() {
    cache.value = await getMemoryList()
    return cache.value
  }

  function clear() {
    cache.value = null
  }

  function set(data) {
    cache.value = data
  }

  return { cache, refresh, clear, set }
}
