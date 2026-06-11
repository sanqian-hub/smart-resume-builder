import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUser } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  let initialized = false

  async function fetchUser() {
    try {
      const data = await getCurrentUser()
      setUser(data)
      return true
    } catch {
      clearUser()
      return false
    }
  }

  async function init() {
    if (initialized) return true
    initialized = true
    if (!user.value) return false
    return fetchUser()
  }

  function setUser(data) {
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
  }

  function clearUser() {
    user.value = null
    localStorage.removeItem('user')
    initialized = false
  }

  return { user, fetchUser, init, setUser, clearUser }
})
