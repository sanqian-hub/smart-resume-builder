<template>
  <div class="profile-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">个人资料</h2>
        <p class="page-desc">管理你的个人信息和头像</p>
      </div>
      <button class="btn-back" @click="$router.push('/')">返回首页</button>
    </div>
    <div class="profile-card" style="position: relative;">
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar-img" />
          <div v-else class="avatar-empty">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="8" r="5"/>
              <path d="M20 21a8 8 0 0 0-16 0"/>
            </svg>
          </div>
          <button class="avatar-edit-btn" @click="$refs.fileInput.click()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
          </button>
        </div>
        <input ref="fileInput" type="file" accept="image/*" hidden @change="handleAvatarChange" />
        <p class="avatar-hint">点击图标更换头像</p>
      </div>
      <div class="divider"></div>
      <form class="info-section" @submit.prevent="handleSave">
        <div class="info-grid">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="form.username" placeholder="请输入用户名" />
          </div>
          <div class="form-group">
            <label>性别</label>
            <div class="gender-select">
              <label class="radio-item" :class="{ active: form.gender === 1 }" @click="form.gender = 1">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="5" y="2" width="14" height="20" rx="2"/>
                </svg>
                男
              </label>
              <label class="radio-item" :class="{ active: form.gender === 0 }" @click="form.gender = 0">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="5" y="2" width="14" height="20" rx="2"/>
                </svg>
                女
              </label>
              <label class="radio-item" :class="{ active: form.gender === null }" @click="form.gender = null">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="5" y="2" width="14" height="20" rx="2"/>
                </svg>
                保密
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.email" placeholder="请输入邮箱" type="email" />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input v-model="form.phone" placeholder="请输入手机号" />
          </div>
        </div>
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <div class="form-actions">
          <button type="submit" class="btn-save" :class="{ saving }" :disabled="saving">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </div>
        <Transition name="toast">
          <div v-if="toastMsg" class="profile-toast">{{ toastMsg }}</div>
        </Transition>
      </form>
    </div>
    <div class="memory-card">
      <div class="memory-header">
        <div>
          <h3 class="memory-title">AI 对你的了解</h3>
          <p class="memory-desc">基于你和 AI 的对话自动提取</p>
        </div>
        <button v-if="memories.length" class="btn-clear-memory" @click="handleClearMemory">清空</button>
      </div>
      <div v-if="memoryLoading" class="memory-loading">加载中...</div>
      <div v-else-if="!memories.length" class="memory-empty">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.4">
          <path d="M12 2a7 7 0 0 1 7 7c0 2.5-1.3 4.8-3.5 6v1.5a1.5 1.5 0 0 1-1.5 1.5h-4a1.5 1.5 0 0 1-1.5-1.5V15C6.3 13.8 5 11.5 5 9a7 7 0 0 1 7-7z"/>
          <line x1="9" y1="22" x2="15" y2="22"/>
        </svg>
        <p>AI 还不够了解你，多和 AI 对话后这里会显示你的画像</p>
      </div>
      <div v-else class="memory-list">
        <div v-for="mem in memories" :key="mem.id" class="memory-item">
          <span class="memory-tag" :class="'tag-' + mem.category">{{ categoryLabel(mem.category) }}</span>
          <span class="memory-content">{{ mem.content }}</span>
          <button class="memory-del" @click="handleDeleteMemory(mem.id)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { updateMyInfo, uploadAvatar } from '../api/user'
import { deleteMemory, clearMemory } from '../api/ai'
import { useMemoryCache } from '../composables/useMemoryCache'

const userStore = useUserStore()
const fileInput = ref(null)
const errorMsg = ref('')
const toastMsg = ref('')
const saving = ref(false)
const { cache: memoryCache, refresh: refreshMemoryCache, set: setMemoryCache } = useMemoryCache()

const form = reactive({
  username: '',
  avatarUrl: '',
  gender: null,
  email: '',
  phone: '',
})

const memories = ref([])
const memoryLoading = ref(false)

const CATEGORY_MAP = {
  preference: '偏好',
  skill: '技能',
  career: '职业',
  habit: '习惯',
}

function categoryLabel(cat) {
  return CATEGORY_MAP[cat] || cat
}

onMounted(() => {
  if (userStore.user) {
    form.username = userStore.user.username || ''
    form.avatarUrl = userStore.user.avatarUrl || ''
    form.gender = userStore.user.gender ?? null
    form.email = userStore.user.email || ''
    form.phone = userStore.user.phone || ''
  }
  loadMemories()
})

async function loadMemories() {
  if (memoryCache.value != null) {
    memories.value = memoryCache.value
    memoryLoading.value = false
    return
  }
  memoryLoading.value = true
  try {
    memories.value = await refreshMemoryCache()
  } catch {
    memories.value = []
  } finally {
    memoryLoading.value = false
  }
}

async function handleDeleteMemory(id) {
  try {
    await deleteMemory(id)
    memories.value = memories.value.filter(m => m.id !== id)
    setMemoryCache(memories.value)
  } catch {}
}

async function handleClearMemory() {
  try {
    await clearMemory()
    memories.value = []
    setMemoryCache([])
  } catch {}
}

async function handleAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const url = await uploadAvatar(file)
    form.avatarUrl = url
    await userStore.fetchUser()
  } catch (err) {
    errorMsg.value = '头像上传失败'
  }
}

function showToast(msg, duration = 1200) {
  toastMsg.value = msg
  setTimeout(() => { toastMsg.value = '' }, duration)
}

async function handleSave() {
  errorMsg.value = ''
  saving.value = true
  try {
    await updateMyInfo(form)
    await userStore.fetchUser()
    showToast('保存成功')
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 640px;
  margin: 0 auto;
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}

.btn-back {
  font-size: 14px;
  color: var(--text-2);
  background: none;
  border: 1px solid var(--border);
  padding: 6px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
  font-weight: 500;
}

.btn-back:hover {
  color: var(--primary);
  border-color: var(--primary);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--text-3);
}

.profile-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px;
  gap: 12px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-img {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--primary-light);
}

.avatar-empty {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: var(--bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3);
  border: 3px solid var(--border);
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: var(--primary);
  border: 3px solid var(--bg-card);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: var(--transition);
}

.avatar-edit-btn:hover {
  background: var(--primary-hover);
  transform: scale(1.1);
}

.avatar-hint {
  font-size: 13px;
  color: var(--text-3);
}

.divider {
  height: 1px;
  background: var(--border);
}

.info-section {
  padding: 24px 32px 32px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-2);
}

.form-group input {
  padding: 10px 12px;
  font-size: 14px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-1);
  transition: var(--transition);
  width: 100%;
  min-width: 0;
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-group input::placeholder {
  color: var(--text-3);
}

.gender-select {
  display: flex;
  gap: 8px;
}

.radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  font-size: 13px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--text-2);
  transition: var(--transition);
  user-select: none;
}

.radio-item:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.radio-item.active {
  border-color: var(--primary);
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 500;
}

.error-msg {
  font-size: 13px;
  color: var(--danger);
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 16px;
}

.form-actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.btn-save {
  padding: 10px 32px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: var(--primary);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
}

.btn-save:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-save:active {
  transform: translateY(0);
}

.btn-save.saving {
  opacity: 0.7;
  cursor: wait;
}

.profile-toast {
  position: absolute;
  top: -66px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--success);
  color: #fff;
  padding: 10px 28px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: var(--shadow-md);
  z-index: 9999;
  white-space: nowrap;
}

.toast-enter-active { transition: all 0.25s ease; }
.toast-leave-active { transition: all 0.15s ease; }
.toast-enter-from { opacity: 0; transform: translateX(-50%) translateY(-8px); }
.toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(-8px); }

.memory-card {
  margin-top: 24px;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 24px;
  min-height: 280px;
}

.memory-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.memory-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-1);
  margin-bottom: 2px;
}

.memory-desc {
  font-size: 13px;
  color: var(--text-3);
}

.btn-clear-memory {
  min-width: 96px;
  font-size: 13px;
  color: var(--text-1);
  background: #f8fafc;
  border: 1px solid var(--border);
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition);
}

.btn-clear-memory:hover {
  background: #f1f5f9;
  border-color: #d1d5db;
}

.memory-empty {
  text-align: center;
  padding: 32px 0;
  color: var(--text-3);
  font-size: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.memory-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.memory-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  transition: var(--transition);
}

.memory-item:hover {
  background: var(--bg-hover, var(--bg-page));
}

.memory-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.tag-preference { background: #eff6ff; color: #2563eb; }
.tag-skill { background: #f0fdf4; color: #16a34a; }
.tag-career { background: #fff7ed; color: #ea580c; }
.tag-habit { background: #f5f5f5; color: #737373; }

.memory-content {
  flex: 1;
  font-size: 14px;
  color: var(--text-1);
  line-height: 1.5;
  min-width: 0;
  word-break: break-word;
}

.memory-del {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: var(--text-3);
  cursor: pointer;
  border-radius: 4px;
  transition: var(--transition);
}

.memory-del:hover {
  background: #fef2f2;
  color: var(--danger);
}

.memory-loading {
  text-align: center;
  padding: 24px;
  color: var(--text-3);
  font-size: 14px;
}
</style>
