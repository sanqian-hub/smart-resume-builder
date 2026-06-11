<template>
  <Teleport to="body">
    <div v-if="visible" class="proofread-overlay">
      <div
        class="proofread-dialog"
        ref="dialogRef"
        :class="{ 'is-dragging': dragging }"
        :style="{ left: pos.x + 'px', top: pos.y + 'px' }"
        @mousedown.stop
      >
        <div class="proofread-header" @mousedown="startDrag">
          <div class="proofread-title">
            <BadgeCheck :size="18" color="var(--primary)" />
            语法纠错
          </div>
          <button class="proofread-close-btn" @mousedown.stop @click.stop="$emit('close')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="proofread-body">
          <Transition name="toast-fade">
            <div v-if="toast" class="proofread-toast" :class="toast.type">
              <svg v-if="toast.type === 'error'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              <span>{{ toast.message }}</span>
            </div>
          </Transition>

          <div class="proofread-summary">
            <BadgeCheck :size="18" color="var(--primary)" />
            <span>{{ summaryText }}</span>
          </div>

          <div v-if="loading" class="proofread-loading">
            <div class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <p>AI 正在检查当前简历中的错别字、病句和表达问题...</p>
          </div>

          <div v-else class="proofread-result-list">
            <div v-if="visibleItems.length > 0" class="proofread-list-head">
              <div class="proofread-count">共发现 {{ visibleItems.length }} 处可优化内容</div>
              <div v-if="visibleItems.length > 1" class="proofread-bulk-actions">
                <button class="proofread-result-btn proofread-result-btn-primary" @click="handleApplyAll">全部应用</button>
              </div>
            </div>
            <div class="proofread-scroll-area">
              <div v-for="item in visibleItems" :key="item.id" class="proofread-result-item">
                <div class="proofread-result-head">
                  <span class="proofread-result-field">{{ item.fieldLabel }}</span>
                  <span class="proofread-result-type">{{ item.typeLabel }}</span>
                </div>

                <div class="proofread-result-block">
                  <span class="proofread-result-label">原文</span>
                  <p class="proofread-result-text">{{ item.original }}</p>
                </div>

                <div class="proofread-result-block proofread-result-block-suggestion">
                  <span class="proofread-result-label">建议</span>
                  <p class="proofread-result-text">{{ item.suggestion }}</p>
                </div>

                <div class="proofread-result-reason">
                  <span class="proofread-result-label">原因</span>
                  <p class="proofread-result-reason-text">{{ item.reason }}</p>
                </div>

                <div class="proofread-result-actions">
                  <button class="proofread-result-btn proofread-result-btn-primary" @click="handleApply(item)">应用</button>
                  <button class="proofread-result-btn" @click="handleIgnore(item)">忽略</button>
                </div>
              </div>
              <div v-if="visibleItems.length === 0" class="proofread-empty">
                <p class="proofread-empty-text">{{ emptyText }}</p>
                <button v-if="canRetry" class="proofread-result-btn proofread-empty-btn" @click="requestProofread">重新检查</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { BadgeCheck } from 'lucide-vue-next'
import { proofreadResume } from '../../../api/resume'
import { clampProofreadDialogPosition, getCenteredProofreadDialogPosition } from './proofreadDialogPosition'
import { normalizeFieldPath } from '../proofreadHighlight'

const defaultProofreadResult = {
  summary: '当前未发现明显的语法或表达问题',
  items: [],
}

const props = defineProps({
  visible: { type: Boolean, default: false },
  resumeId: { type: [Number, String], default: null },
  moduleData: { type: Object, default: () => ({}) },
  result: { type: Object, default: null },
})

const emit = defineEmits(['close', 'apply', 'apply-all', 'ignore', 'update-highlights'])

const pos = reactive({ x: 0, y: 0 })
const dragging = ref(false)
const dialogRef = ref(null)
const dismissedIds = ref(new Set())
const loading = ref(false)
const result = ref(defaultProofreadResult)
const toast = ref(null)
let dragStartX = 0
let dragStartY = 0
let dragStartPosX = 0
let dragStartPosY = 0
let toastTimer = null

const proofreadResult = computed(() => {
  if (props.result) {
    return normalizeProofreadResult(props.result)
  }
  return result.value || defaultProofreadResult
})

const visibleItems = computed(() => proofreadResult.value.items.filter(item => !dismissedIds.value.has(item.id)))
const summaryText = computed(() => loading.value ? '正在生成语法纠错建议...' : proofreadResult.value.summary)
const canRetry = computed(() => !loading.value && toast.value?.type === 'error')
const emptyText = computed(() => {
  if (canRetry.value) return '本次检查未能返回有效结果'
  if (proofreadResult.value.items.length > 0) return '当前暂无需要处理的建议'
  return '当前未发现需要纠错的内容'
})

function initPos() {
  const width = window.innerWidth
  const height = window.innerHeight
  const { width: dialogWidth, height: dialogHeight } = getDialogBounds()
  const centered = getCenteredProofreadDialogPosition(width, height, dialogWidth, dialogHeight)
  pos.x = centered.x
  pos.y = centered.y
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      dismissedIds.value = new Set()
      initPos()
      requestProofread()
      return
    }
    dragging.value = false
    loading.value = false
    emit('update-highlights', [])
  },
  { immediate: true },
)

watch(
  () => props.result,
  (val) => {
    dismissedIds.value = new Set()
    if (val) {
      result.value = normalizeProofreadResult(val)
    }
  },
)

watch(
  visibleItems,
  (items) => {
    emit('update-highlights', props.visible ? items : [])
  },
  { deep: true, immediate: true },
)

function startDrag(e) {
  dragging.value = true
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartPosX = pos.x
  dragStartPosY = pos.y
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

function getDialogBounds() {
  const rect = dialogRef.value?.getBoundingClientRect?.()
  return {
    width: rect?.width || 620,
    height: rect?.height || 640,
  }
}

function onDrag(e) {
  if (!dragging.value) return
  const { width: dialogWidth, height: dialogHeight } = getDialogBounds()
  const next = clampProofreadDialogPosition(
    dragStartPosX + e.clientX - dragStartX,
    dragStartPosY + e.clientY - dragStartY,
    window.innerWidth,
    window.innerHeight,
    dialogWidth,
    dialogHeight,
  )
  pos.x = next.x
  pos.y = next.y
}

function stopDrag() {
  dragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

function showToast(message, type = 'error') {
  toast.value = { message, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = null
  }, 2500)
}

function normalizeTypeLabel(type) {
  const map = {
    typo: '错别字',
    grammar: '语法问题',
    style: '表达优化',
    clarity: '表达澄清',
  }
  return map[type] || type || '纠错建议'
}

function normalizeProofreadItem(item, index) {
  return {
    id: item.id || `proofread-${index}`,
    moduleType: item.moduleType || '',
    itemIndex: Number.isInteger(item.itemIndex) ? item.itemIndex : item.itemIndex == null ? null : Number.parseInt(item.itemIndex, 10),
    fieldPath: item.fieldPath || null,
    occurrenceIndex: Number.isInteger(item.occurrenceIndex) ? item.occurrenceIndex : Number.parseInt(item.occurrenceIndex ?? 0, 10) || 0,
    fieldLabel: item.fieldLabel || '简历内容',
    type: item.type || '',
    typeLabel: item.typeLabel || normalizeTypeLabel(item.type),
    original: item.original || '',
    suggestion: item.suggestion || '',
    reason: item.reason || '',
  }
}

function shouldDropOverlappingUiItem(current, items) {
  if (!['style', 'clarity'].includes(current.type)) return false
  if (!current.original || !current.suggestion) return false

  return items.some((other) => {
    if (other.id === current.id) return false
    if (!['typo', 'grammar'].includes(other.type)) return false
    if (other.moduleType !== current.moduleType) return false
    if ((other.itemIndex ?? null) !== (current.itemIndex ?? null)) return false
    if (normalizeFieldPath(other.fieldPath) !== normalizeFieldPath(current.fieldPath)) return false
    if (!other.original || !other.suggestion) return false
    return current.original.includes(other.original) && current.suggestion.includes(other.suggestion)
  })
}

function filterOverlappingUiItems(items) {
  if (items.length <= 1) return items
  return items.filter(item => !shouldDropOverlappingUiItem(item, items))
}

function normalizeProofreadSummary(summary, count) {
  if (!summary) return count ? `发现 ${count} 处可优化内容` : defaultProofreadResult.summary
  return summary.replace(/共?发现\s*\d+\s*处/g, `共发现${count}处`)
}

function normalizeProofreadResult(raw) {
  const list = Array.isArray(raw?.items)
    ? raw.items
    : Array.isArray(raw?.issues)
      ? raw.issues
      : []

  const items = filterOverlappingUiItems(list
    .map(normalizeProofreadItem)
    .filter(item => item.original && item.suggestion)
  )

  return {
    summary: normalizeProofreadSummary(raw?.summary, items.length),
    items,
  }
}

async function requestProofread() {
  if (props.result) {
    result.value = normalizeProofreadResult(props.result)
    return
  }
  loading.value = true
  toast.value = null
  result.value = defaultProofreadResult
  try {
    const response = await proofreadResume(props.resumeId, props.moduleData)
    result.value = normalizeProofreadResult(response)
  } catch (err) {
    result.value = defaultProofreadResult
    showToast(err?.response?.data?.message || err?.message || '语法纠错失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function dismissItem(item) {
  dismissedIds.value = new Set([...dismissedIds.value, item.id])
}

function handleApply(item) {
  emit('apply', item)
  dismissItem(item)
  showToast('已应用 1 条修改建议', 'success')
}

function handleApplyAll() {
  if (!visibleItems.value.length) return
  const items = [...visibleItems.value]
  emit('apply-all', items)
  dismissedIds.value = new Set(proofreadResult.value.items.map(item => item.id))
  showToast(`已应用 ${items.length} 条修改建议`, 'success')
}

function handleIgnore(item) {
  emit('ignore', item)
  dismissItem(item)
}
</script>

<style scoped>
.proofread-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.proofread-dialog {
  position: fixed;
  width: 620px;
  max-width: calc(100vw - 32px);
  max-height: min(78vh, 640px);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(111, 124, 162, 0.18);
  box-shadow: 0 24px 72px rgba(33, 46, 93, 0.18);
  overflow: hidden;
  pointer-events: auto;
  display: flex;
  flex-direction: column;
}

.proofread-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border);
  cursor: default;
  user-select: none;
}

.proofread-dialog.is-dragging {
  cursor: default;
}

.proofread-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
}

.proofread-close-btn {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  transition: all 0.15s ease;
}

.proofread-close-btn:hover {
  color: var(--text-1);
  background: rgba(0, 0, 0, 0.06);
}

.proofread-body {
  padding: 20px 20px 22px;
  overflow-y: auto;
}

.proofread-toast {
  margin-bottom: 12px;
  min-height: 38px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}

.proofread-toast.error {
  background: #fff4f4;
  color: #d14343;
  border: 1px solid rgba(209, 67, 67, 0.16);
}

.proofread-toast.success {
  background: #eefbf4;
  color: #1f9d67;
  border: 1px solid rgba(31, 157, 103, 0.16);
}

.proofread-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--primary-light);
  color: var(--text-1);
  font-size: 13px;
  font-weight: 600;
}

.proofread-loading {
  margin-top: 14px;
  padding: 28px 20px 18px;
  border-radius: 14px;
  border: 1px solid rgba(111, 124, 162, 0.14);
  background: #fff;
  box-shadow: 0 10px 28px rgba(33, 46, 93, 0.08);
  text-align: center;
}

.proofread-loading p {
  margin: 14px 0 0;
  font-size: 13px;
  color: var(--text-2);
}

.loading-dots {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--primary);
  animation: proofread-bounce 0.9s ease-in-out infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.12s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.24s;
}

.proofread-result-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.proofread-list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.proofread-bulk-actions {
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}

.proofread-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-3);
}

.proofread-scroll-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.proofread-result-item {
  padding: 16px;
  border: 1px solid rgba(111, 124, 162, 0.14);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(33, 46, 93, 0.08);
}

.proofread-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.proofread-result-field {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}

.proofread-result-type {
  padding: 3px 8px;
  border-radius: 999px;
  background: var(--primary-light);
  color: var(--primary);
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.proofread-result-block + .proofread-result-block,
.proofread-result-reason {
  margin-top: 10px;
}

.proofread-result-label {
  display: inline-flex;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-3);
}

.proofread-result-text,
.proofread-result-reason-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-1);
}

.proofread-result-block-suggestion .proofread-result-text {
  color: var(--primary);
  font-weight: 600;
}

.proofread-result-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.proofread-empty {
  padding: 14px 16px;
  border: 1px dashed rgba(111, 124, 162, 0.2);
  border-radius: 12px;
  background: var(--bg-page);
  text-align: center;
}

.proofread-empty-text {
  margin: 0;
  color: var(--text-3);
  font-size: 13px;
}

.proofread-empty-btn {
  margin: 12px auto 0;
}

.proofread-result-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 0;
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  background: var(--text-3);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.proofread-result-btn:hover {
  opacity: 0.92;
}

.proofread-result-btn-primary {
  background: var(--primary);
}

.proofread-result-btn-primary:hover {
  background: var(--primary-hover);
}

.proofread-result-btn:active {
  transform: translateY(1px);
}

@keyframes proofread-bounce {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
