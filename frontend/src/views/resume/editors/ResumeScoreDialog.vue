<template>
  <Teleport to="body">
    <div v-if="visible" class="score-overlay">
      <div
        class="score-dialog"
        :class="{ 'is-dragging': dragging }"
        :style="{ left: pos.x + 'px', top: pos.y + 'px', width: size.w + 'px', height: size.h + 'px' }"
        @mousedown.stop
      >
        <div class="score-header" @mousedown="startDrag">
          <div class="score-title">
            <FileText :size="18" color="var(--primary)" />
            简历打分
          </div>
          <button class="score-close-btn" @click.stop="$emit('close')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="score-body">
          <Transition name="toast-fade">
            <div v-if="toast" class="score-toast" :class="toast.type">
              <svg v-if="toast.type === 'error'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              <span>{{ toast.message }}</span>
              <button class="toast-close" @click="toast = null">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
          </Transition>

          <div class="score-input-section">
            <label class="score-label-text">智能评估当前简历</label>
            <div class="score-intro-card">
              <div class="score-intro-head">
                <Sparkles :size="14" />
                <span>从四个维度给出总分与优化建议</span>
              </div>
              <p>从内容完整度、表达清晰度、岗位相关性和亮点竞争力四个维度给出总分、分析和优化建议。</p>
            </div>
            <button
              class="score-analyze-btn"
              :class="{ active: !analyzing }"
              :disabled="analyzing"
              @click="analyze"
            >
              <BadgeCheck v-if="!analyzing" :size="16" />
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="spin"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              {{ analyzing ? '打分中...' : '开始打分' }}
            </button>
          </div>

          <div v-if="result" class="score-result">
            <div class="result-divider"></div>

            <div class="score-overview">
              <div class="score-ring">
                <svg viewBox="0 0 100 100">
                  <circle cx="50" cy="50" r="42" fill="none" stroke="var(--border)" stroke-width="6"/>
                  <circle
                    cx="50"
                    cy="50"
                    r="42"
                    fill="none"
                    :stroke="scoreColor(result.totalScore)"
                    stroke-width="6"
                    stroke-linecap="round"
                    :stroke-dasharray="2 * Math.PI * 42"
                    :stroke-dashoffset="2 * Math.PI * 42 * (1 - result.totalScore / 100)"
                    transform="rotate(-90 50 50)"
                    class="score-ring-arc"
                  />
                </svg>
                <div class="score-ring-text">
                  <span class="score-number" :style="{ color: scoreColor(result.totalScore) }">{{ result.totalScore }}</span>
                  <span class="score-label">总分</span>
                </div>
              </div>
            </div>

            <div class="dimensions">
              <div v-for="dim in result.dimensions" :key="dim.name" class="dim-item">
                <div class="dim-header">
                  <span class="dim-name">{{ dim.name }}</span>
                  <span class="dim-score" :style="{ color: scoreColor(dim.score) }">{{ dim.score }}</span>
                </div>
                <div class="dim-bar-bg">
                  <div class="dim-bar-fill" :style="{ width: dim.score + '%', background: scoreColor(dim.score) }"></div>
                </div>
                <div v-if="dim.analysis" class="dim-analysis">{{ dim.analysis }}</div>
                <div v-if="dim.suggestion" class="dim-suggestion">
                  <Sparkles :size="12" />
                  {{ dim.suggestion }}
                </div>
              </div>
            </div>

            <div v-if="result.highlights?.length" class="tag-section">
              <div class="tag-section-title">当前亮点</div>
              <div class="tag-list">
                <span v-for="item in result.highlights" :key="item" class="tag tag-highlight">{{ item }}</span>
              </div>
            </div>

            <div v-if="result.suggestions?.length" class="tag-section">
              <div class="tag-section-title">优化建议</div>
              <div class="score-suggestion-list">
                <div v-for="item in result.suggestions" :key="item" class="score-suggestion-item">
                  <span class="score-suggestion-dot"></span>
                  <span>{{ item }}</span>
                </div>
              </div>
            </div>

            <div v-if="result.summary" class="score-summary">
              <div class="tag-section-title">总结</div>
              <p>{{ result.summary }}</p>
            </div>
          </div>

          <div v-else-if="!analyzing" class="score-empty">
            <FileText :size="40" color="var(--text-3)" :stroke-width="1.2" />
            <p>点击开始打分，AI 将对当前简历做一次通用评估</p>
          </div>

          <div v-if="analyzing && !result" class="score-loading">
            <div class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <p>AI 正在阅读当前简历并生成评分建议...</p>
          </div>
        </div>

        <div class="score-resize score-resize-tl" @mousedown.stop="startResize($event, 'tl')"></div>
        <div class="score-resize score-resize-tr" @mousedown.stop="startResize($event, 'tr')"></div>
        <div class="score-resize score-resize-bl" @mousedown.stop="startResize($event, 'bl')"></div>
        <div class="score-resize score-resize-br" @mousedown.stop="startResize($event, 'br')"></div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { BadgeCheck, FileText, Sparkles } from 'lucide-vue-next'
import { scoreResume } from '../../../api/resume'

const props = defineProps({
  visible: { type: Boolean, default: false },
  resumeId: { type: [Number, String], default: null },
  moduleData: { type: Object, default: () => ({}) },
})

defineEmits(['close'])

const analyzing = ref(false)
const result = ref(null)
const toast = ref(null)
let toastTimer = null

function showToast(message, type = 'error') {
  toast.value = { message, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value = null }, 4000)
}

const pos = reactive({ x: 0, y: 0 })
const size = reactive({ w: 560, h: 620 })
const MIN_W = 400
const MIN_H = 420

function initPos() {
  const w = window.innerWidth
  const h = window.innerHeight
  pos.x = Math.max(0, (w - size.w) / 2 + 100)
  pos.y = Math.max(80, (h - size.h) / 2)
}

watch(() => props.visible, (val) => {
  if (val) {
    initPos()
    return
  }
  analyzing.value = false
  result.value = null
  toast.value = null
})

async function analyze() {
  if (analyzing.value) return
  analyzing.value = true
  result.value = null
  try {
    result.value = await scoreResume(props.resumeId, props.moduleData)
  } catch (err) {
    result.value = null
    if (err?.code === 'ECONNABORTED') {
      showToast('打分超时，请重试（AI 评分可能需要较长时间）')
    } else {
      showToast(err?.response?.data?.message || err?.message || '打分失败，请重试')
    }
  } finally {
    analyzing.value = false
  }
}

function scoreColor(score) {
  if (score >= 80) return '#10b981'
  if (score >= 60) return '#f59e0b'
  return '#ef4444'
}

let dragging = false
let dragStartX = 0
let dragStartY = 0
let dragStartPosX = 0
let dragStartPosY = 0

function startDrag(e) {
  dragging = true
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartPosX = pos.x
  dragStartPosY = pos.y
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

function onDrag(e) {
  if (!dragging) return
  pos.x = Math.max(0, Math.min(window.innerWidth - size.w, dragStartPosX + e.clientX - dragStartX))
  pos.y = Math.max(0, Math.min(window.innerHeight - size.h, dragStartPosY + e.clientY - dragStartY))
}

function stopDrag() {
  dragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

let resizing = false
let resizeDir = ''
let resizeStartX = 0
let resizeStartY = 0
let resizeStartPosX = 0
let resizeStartPosY = 0
let resizeStartW = 0
let resizeStartH = 0

function startResize(e, dir) {
  resizing = true
  resizeDir = dir
  resizeStartX = e.clientX
  resizeStartY = e.clientY
  resizeStartPosX = pos.x
  resizeStartPosY = pos.y
  resizeStartW = size.w
  resizeStartH = size.h
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

function onResize(e) {
  if (!resizing) return
  const dx = e.clientX - resizeStartX
  const dy = e.clientY - resizeStartY
  let newW = resizeStartW
  let newH = resizeStartH
  let newX = resizeStartPosX
  let newY = resizeStartPosY

  if (resizeDir.includes('r')) newW = Math.max(MIN_W, resizeStartW + dx)
  if (resizeDir.includes('l')) {
    newW = Math.max(MIN_W, resizeStartW - dx)
    newX = resizeStartPosX + resizeStartW - newW
  }
  if (resizeDir.includes('b')) newH = Math.max(MIN_H, resizeStartH + dy)
  if (resizeDir.includes('t')) {
    newH = Math.max(MIN_H, resizeStartH - dy)
    newY = resizeStartPosY + resizeStartH - newH
  }

  size.w = newW
  size.h = newH
  pos.x = newX
  pos.y = newY
}

function stopResize() {
  resizing = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}
</script>

<style scoped>
.score-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  pointer-events: none;
}

.score-dialog {
  position: fixed;
  width: 560px;
  height: 620px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(111, 124, 162, 0.18);
  border-radius: 18px;
  box-shadow: 0 28px 80px rgba(33, 46, 93, 0.18);
  overflow: visible;
  pointer-events: auto;
}

.score-dialog.is-dragging { cursor: default; }

.score-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border);
  cursor: default;
  user-select: none;
  flex-shrink: 0;
}

.score-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}

.score-close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-3);
  border-radius: 6px;
  transition: all 0.15s ease;
}

.score-close-btn:hover { color: var(--text-1); background: rgba(0, 0, 0, 0.06); }

.score-body {
  height: calc(100% - 61px);
  padding: 18px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.score-input-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-label-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
}

.score-intro-card {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-page);
}

.score-intro-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}

.score-intro-card p {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-2);
}

.score-analyze-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  background: var(--text-3);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: not-allowed;
  transition: all 0.15s ease;
  align-self: flex-end;
}

.score-analyze-btn.active {
  background: var(--primary);
  cursor: pointer;
}

.score-analyze-btn:disabled {
  cursor: not-allowed;
  opacity: 1;
}

.score-result {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.result-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(111, 124, 162, 0.24), transparent);
}

.score-overview {
  display: flex;
  justify-content: center;
}

.score-ring {
  position: relative;
  width: 132px;
  height: 132px;
}

.score-ring svg { width: 100%; height: 100%; }
.score-ring-arc { transition: stroke-dashoffset 0.45s ease, stroke 0.25s ease; }

.score-ring-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.score-number {
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
}

.score-label {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.dimensions {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dim-item {
  padding: 14px;
  border-radius: 14px;
  background: rgba(247, 248, 252, 0.92);
  border: 1px solid rgba(111, 124, 162, 0.14);
}

.dim-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.dim-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
}

.dim-score {
  font-size: 18px;
  font-weight: 700;
}

.dim-bar-bg {
  margin-top: 10px;
  height: 8px;
  border-radius: 999px;
  background: rgba(111, 124, 162, 0.14);
  overflow: hidden;
}

.dim-bar-fill {
  height: 100%;
  border-radius: inherit;
}

.dim-analysis {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-2);
}

.dim-suggestion {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #3f5ed8;
}

.tag-section-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
}

.tag-highlight {
  color: #34614a;
  background: rgba(16, 185, 129, 0.12);
}

.score-suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.score-suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-2);
}

.score-suggestion-dot {
  width: 7px;
  height: 7px;
  margin-top: 7px;
  border-radius: 999px;
  background: #5a78f2;
  flex-shrink: 0;
}

.score-summary p {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: var(--text-2);
}

.score-empty,
.score-loading {
  flex: 1;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-3);
  text-align: center;
}

.score-empty p,
.score-loading p {
  margin: 0;
  font-size: 13px;
}

.loading-dots {
  display: inline-flex;
  gap: 8px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #6a85f8;
  animation: dot-bounce 0.9s infinite ease-in-out;
}

.loading-dots span:nth-child(2) { animation-delay: 0.12s; }
.loading-dots span:nth-child(3) { animation-delay: 0.24s; }

.spin { animation: spin 0.8s linear infinite; }

.score-resize {
  position: absolute;
  width: 18px;
  height: 18px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.score-dialog:hover .score-resize { opacity: 1; }
.score-resize-tl { top: 0; left: 0; cursor: nwse-resize; border-top: 2px solid var(--text-3); border-left: 2px solid var(--text-3); border-top-left-radius: 12px; }
.score-resize-tr { top: 0; right: 0; cursor: nesw-resize; border-top: 2px solid var(--text-3); border-right: 2px solid var(--text-3); border-top-right-radius: 12px; }
.score-resize-bl { bottom: 0; left: 0; cursor: nesw-resize; border-bottom: 2px solid var(--text-3); border-left: 2px solid var(--text-3); border-bottom-left-radius: 12px; }
.score-resize-br { bottom: 0; right: 0; cursor: nwse-resize; border-bottom: 2px solid var(--text-3); border-right: 2px solid var(--text-3); border-bottom-right-radius: 12px; }

.score-toast {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
}

.score-toast.error {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.toast-close {
  margin-left: auto;
  border: none;
  background: transparent;
  color: currentColor;
  cursor: pointer;
  padding: 0;
  display: inline-flex;
  align-items: center;
}

@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
  40% { transform: translateY(-4px); opacity: 1; }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .score-dialog {
    left: 16px !important;
    right: 16px !important;
    top: 88px !important;
    width: auto !important;
    height: calc(100vh - 112px) !important;
  }

  .score-body {
    padding: 16px;
  }
}
</style>
