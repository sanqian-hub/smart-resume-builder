<template>
  <Teleport to="body">
    <Transition name="notice-fade">
      <div v-if="modelValue" class="notice-overlay" @click.self="close">
        <div class="notice-dialog">
          <div class="notice-dialog-header">
            <span>消息通知</span>
            <div class="notice-dialog-header-actions">
              <button v-if="notices.some(n => !n.isRead)" class="notice-mark-all" @click="handleMarkAll">全部已读</button>
              <button class="notice-close-btn" @click="close" aria-label="关闭通知">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18"/>
                  <path d="M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>
          <div class="notice-dialog-body">
            <div v-if="notices.length === 0" class="notice-panel-empty">暂无通知</div>
            <div v-else class="notice-panel-list">
              <div
                v-for="n in notices"
                :key="n.id"
                class="notice-panel-item"
                :class="{ unread: !n.isRead, expanded: expandedId === n.id }"
                @click="toggleExpand(n)"
              >
                <div class="notice-panel-icon" :class="n.type">
                  <ClipboardCheck v-if="n.type === 'completeness_check'" :size="18" />
                  <Lightbulb v-else-if="n.type === 'optimize_suggest'" :size="18" />
                  <Bell v-else :size="18" />
                </div>
                <div class="notice-panel-body">
                  <div class="notice-panel-title">{{ n.title }}</div>
                  <div class="notice-panel-sub">
                    <span class="notice-panel-resume">
                      {{ n.resumeTitle || '未知简历' }}<template v-if="n.resumeVersionNum"> · V{{ n.resumeVersionNum }}</template>
                    </span>
                    <span class="notice-panel-time">{{ formatNoticeTime(n.createTime) }}</span>
                  </div>
                  <div v-if="expandedId === n.id" class="notice-panel-detail" @click.stop>
                    <div class="notice-panel-content" v-html="n.content" />
                    <button class="notice-panel-goto" @click="goResume(n)">查看简历 →</button>
                  </div>
                </div>
                <div v-if="!n.isRead && expandedId !== n.id" class="notice-panel-dot"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, ClipboardCheck, Lightbulb } from 'lucide-vue-next'
import { getNoticeList, markAllRead, markRead } from '../api/notice'
import { useNotice } from '../composables/useNotice'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const { fetchUnread } = useNotice()

const notices = ref([])
const expandedId = ref(null)

watch(
  () => props.modelValue,
  async open => {
    if (!open) return
    expandedId.value = null
    try {
      const [list] = await Promise.all([getNoticeList(), fetchUnread()])
      notices.value = list
    } catch {}
  },
)

function close() {
  emit('update:modelValue', false)
}

function formatNoticeTime(str) {
  if (!str) return ''
  return str.replace('T', ' ').substring(0, 16)
}

function toggleExpand(n) {
  if (expandedId.value === n.id) {
    expandedId.value = null
    return
  }
  expandedId.value = n.id
  if (!n.isRead) {
    n.isRead = 1
    markRead(n.id).then(() => fetchUnread()).catch(() => {})
  }
}

function goResume(n) {
  close()
  router.push(`/edit/${n.resumeId}`)
}

async function handleMarkAll() {
  try {
    await markAllRead()
    notices.value.forEach(n => {
      n.isRead = 1
    })
    fetchUnread()
  } catch {}
}
</script>

<style scoped>
.notice-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10001;
}

.notice-dialog {
  width: 520px;
  max-width: 92vw;
  max-height: 70vh;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.notice-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  flex-shrink: 0;
}

.notice-dialog-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notice-mark-all {
  min-width: 88px;
  height: 28px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  background: var(--primary);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s ease;
}

.notice-mark-all:hover {
  opacity: 0.85;
}

.notice-close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-3);
  transition: all 0.15s ease;
}

.notice-close-btn:hover {
  color: var(--text-1);
  background: rgba(0, 0, 0, 0.06);
}

.notice-dialog-body {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
}

.notice-dialog-body::-webkit-scrollbar {
  display: none;
}

.notice-panel-empty {
  padding: 60px 0;
  text-align: center;
  font-size: 14px;
  color: var(--text-3);
}

.notice-panel-list {
  padding: 4px;
}

.notice-panel-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.12s;
}

.notice-panel-item:hover {
  background: var(--bg-page);
}

.notice-panel-item.unread {
  background: #fafaff;
}

.notice-panel-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notice-panel-icon.completeness_check {
  background: #eef2ff;
  color: var(--primary);
}

.notice-panel-icon.optimize_suggest {
  background: #fef3c7;
  color: #d97706;
}

.notice-panel-body {
  flex: 1;
  min-width: 0;
}

.notice-panel-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-1);
  margin-bottom: 4px;
}

.notice-panel-sub {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-panel-resume {
  font-size: 12px;
  color: var(--text-3);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-panel-content {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border);
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.7;
}

.notice-panel-content :deep(p) {
  margin: 0 0 6px;
}

.notice-panel-content :deep(ol),
.notice-panel-content :deep(ul) {
  margin: 6px 0 0;
  padding-left: 20px;
}

.notice-panel-content :deep(li) {
  margin: 0 0 4px;
  padding-left: 2px;
}

.notice-panel-content :deep(li:last-child) {
  margin-bottom: 0;
}

.notice-panel-detail {
  margin-top: 10px;
}

.notice-panel-goto {
  margin-top: 8px;
  padding: 0;
  border: none;
  background: none;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  font-family: inherit;
}

.notice-panel-goto:hover {
  text-decoration: underline;
}

.notice-panel-time {
  font-size: 12px;
  color: var(--text-3);
}

.notice-panel-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  flex-shrink: 0;
  margin-top: 14px;
}

.notice-fade-enter-active {
  transition: opacity 0.2s ease;
}

.notice-fade-leave-active {
  transition: opacity 0.15s ease;
}

.notice-fade-enter-from,
.notice-fade-leave-to {
  opacity: 0;
}

.notice-fade-enter-active .notice-dialog {
  transition: transform 0.2s ease;
}

.notice-fade-leave-active .notice-dialog {
  transition: transform 0.15s ease;
}

.notice-fade-enter-from .notice-dialog {
  transform: scale(0.95) translateY(-10px);
}

.notice-fade-leave-to .notice-dialog {
  transform: scale(0.95) translateY(-10px);
}
</style>
