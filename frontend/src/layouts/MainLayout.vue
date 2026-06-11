<template>
  <div class="main-layout">
    <header v-show="!isEditPage" class="nav-bar">
      <div class="nav-inner">
        <div class="nav-left">
          <router-link to="/" class="logo" :class="{ 'logo-static': route.path === '/' }">
            <div class="logo-mark">
              <img src="/logo.png" alt="logo" />
            </div>
            <span class="logo-label">ArxFolio</span>
          </router-link>
        </div>

        <div class="nav-right">
          <button v-if="userStore.user" class="nav-bell" @click="toggleNoticePanel" title="消息通知">
            <Bell :size="18" />
            <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </button>
          <div v-if="userStore.user" class="nav-user" @click="$router.push('/profile')">
            <div class="nav-avatar">
              <img v-if="userStore.user.avatarUrl" :src="userStore.user.avatarUrl" />
              <span v-else>{{ (userStore.user.username || 'U')[0] }}</span>
            </div>
            <span class="nav-username">{{ userStore.user.username }}</span>
          </div>
          <button v-if="userStore.user" class="nav-logout" @click="handleLogout" title="退出登录">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
          <button v-else class="nav-login" @click="router.push('/login')">
            <LogIn :size="14" />
            <span>登录</span>
          </button>
        </div>
      </div>
    </header>

    <main class="content" :class="{ 'no-pad': isEditPage }">
      <router-view />
    </main>

    <RecordFooter v-if="showRecordFooter" />
    <NoticeCenterDialog v-model="showNoticePanel" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Bell, LogIn } from 'lucide-vue-next'
import RecordFooter from '../components/RecordFooter.vue'
import NoticeCenterDialog from '../components/NoticeCenterDialog.vue'
import { useUserStore } from '../stores/user'
import { logout } from '../api/user'
import { useResumeListCache } from '../composables/useResumeListCache'
import { useMemoryCache } from '../composables/useMemoryCache'
import { useNotice } from '../composables/useNotice'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { clear: clearListCache } = useResumeListCache()
const { cache: memoryCache, refresh: refreshMemoryCache, clear: clearMemoryCache } = useMemoryCache()
const { unreadCount, fetchUnread } = useNotice()

const isEditPage = computed(() => route.path.startsWith('/edit') || route.path.startsWith('/preview') || route.name === 'ResumeEdit' || route.name === 'ResumePreview')
const showRecordFooter = computed(() => !isEditPage.value)

const showNoticePanel = ref(false)

async function toggleNoticePanel() {
  showNoticePanel.value = !showNoticePanel.value
}

function syncStableScrollbar(enabled) {
  document.documentElement.classList.toggle('page-scroll-stable', enabled)
}

onMounted(() => {
  fetchUnread()
  if (userStore.user && memoryCache.value == null) {
    refreshMemoryCache().catch(() => {})
  }
  syncStableScrollbar(!isEditPage.value)
})

watch(isEditPage, (value) => {
  syncStableScrollbar(!value)
}, { immediate: true })

async function handleLogout() {
  try {
    await logout()
  } finally {
    clearListCache()
    clearMemoryCache()
    userStore.clearUser()
    router.push('/login')
  }
}

onBeforeUnmount(() => {
  syncStableScrollbar(false)
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-inner {
  max-width: 1100px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.nav-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 2px;
  text-decoration: none;
}

.logo-static {
  pointer-events: none;
  cursor: default;
}

.logo-mark {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-mark img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  object-position: center;
  display: block;
  transform: translate(1px, -1px);
}

.logo-label {
  font-family: 'Chillax', sans-serif;
  font-size: 28px;
  font-weight: 600;
  color: #355fe5;
  letter-spacing: -0.4px;
  line-height: 1;
  display: block;
  transform: translateY(1px);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-bell {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-2);
  position: relative;
  transition: color 0.15s, background 0.15s;
  margin-right: 6px;
}
.nav-bell:hover {
  color: var(--primary);
  background: var(--primary-light, #eef2ff);
}
.bell-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: var(--danger, #ef4444);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 100px;
  cursor: pointer;
  transition: background 0.15s;
}

.nav-user:hover {
  background: var(--bg-page);
}

.nav-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  flex-shrink: 0;
}

.nav-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nav-username {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-1);
}

.nav-logout {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  transition: color 0.15s, background 0.15s;
}

.nav-logout:hover {
  color: var(--danger);
  background: #fef2f2;
}

.nav-login {
  height: 28px;
  min-width: 90px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: #34b3a0;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: border-color 0.15s, color 0.15s, background 0.15s, transform 0.15s;
}

.nav-login:hover {
  background: #2fa290;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 179, 160, 0.25);
}

.nav-login:active {
  transform: translateY(0);
}

.content {
  flex: 1;
  padding: 32px 24px;
  background: var(--bg-page);
}

@media (max-width: 1200px) {
  .content {
    padding: 24px 16px;
  }
}

.content.no-pad {
  padding: 0;
  background: none;
}
</style>
