import { ref } from 'vue'
import { getUnreadCount } from '../api/notice'

const unreadCount = ref(0)

async function fetchUnread() {
  try {
    unreadCount.value = await getUnreadCount()
  } catch {}
}

export function useNotice() {
  return { unreadCount, fetchUnread }
}
