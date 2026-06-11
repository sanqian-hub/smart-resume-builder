<template>
  <Teleport to="body">
    <div v-if="visible" class="match-overlay">
      <div
        class="match-dialog"
        :class="{ 'is-dragging': dragging }"
        :style="{ left: pos.x + 'px', top: pos.y + 'px', width: size.w + 'px', height: size.h + 'px' }"
        @mousedown.stop
      >
        <!-- 标题栏 -->
        <div class="match-header" @mousedown="startDrag">
          <div class="match-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            岗位匹配分析
          </div>
          <button class="match-close-btn" @click.stop="$emit('close')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>

        <!-- 内容区 -->
        <div class="match-body">
          <!-- Toast 提示 -->
          <Transition name="toast-fade">
            <div v-if="toast" class="match-toast" :class="toast.type">
              <svg v-if="toast.type === 'error'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              <span>{{ toast.message }}</span>
              <button class="toast-close" @click="toast = null">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
          </Transition>

          <!-- 输入区 -->
          <div class="match-input-section">
            <label class="match-label">粘贴目标岗位 JD</label>
            <textarea
              v-model="jdText"
              class="match-textarea"
              placeholder="将岗位描述（JD）粘贴到此处，包括岗位职责、任职要求等..."
              rows="5"
              @input="autoResizeTextarea"
            ></textarea>
            <button
              class="match-analyze-btn"
              :class="{ active: !!jdText.trim() && !analyzing }"
              :disabled="!jdText.trim() || analyzing"
              @click="analyze"
            >
              <svg v-if="!analyzing" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="spin"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              {{ analyzing ? '分析中...' : '开始分析' }}
            </button>
          </div>

          <!-- 结果区 -->
          <div v-if="result" class="match-result">
            <div class="result-divider"></div>

            <!-- 综合评分 -->
            <div class="score-overview">
              <div class="score-ring">
                <svg viewBox="0 0 100 100">
                  <circle cx="50" cy="50" r="42" fill="none" stroke="var(--border)" stroke-width="6"/>
                  <circle cx="50" cy="50" r="42" fill="none" :stroke="scoreColor(result.overallScore)" stroke-width="6" stroke-linecap="round"
                    :stroke-dasharray="2 * Math.PI * 42" :stroke-dashoffset="2 * Math.PI * 42 * (1 - result.overallScore / 100)"
                    transform="rotate(-90 50 50)" class="score-ring-arc"/>
                </svg>
                <div class="score-ring-text">
                  <span class="score-number" :style="{ color: scoreColor(result.overallScore) }">{{ result.overallScore }}</span>
                  <span class="score-label">综合评分</span>
                </div>
              </div>
            </div>

            <!-- 维度分析 -->
            <div class="dimensions">
              <div v-for="dim in result.dimensions" :key="dim.name" class="dim-item">
                <div class="dim-header">
                  <span class="dim-name">{{ dim.name }}</span>
                  <span class="dim-score" :style="{ color: scoreColor(dim.score) }">{{ dim.score }}</span>
                </div>
                <div class="dim-bar-bg">
                  <div class="dim-bar-fill" :style="{ width: dim.score + '%', background: scoreColor(dim.score) }"></div>
                </div>
                <div class="dim-analysis">{{ dim.analysis }}</div>
                <div v-if="dim.suggestion" class="dim-suggestion">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 1 1 7.072 0l-.548.547A3.374 3.374 0 0 0 14 18.469V19a2 2 0 1 1-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/></svg>
                  {{ dim.suggestion }}
                </div>
              </div>
            </div>

            <!-- 缺失技能 -->
            <div v-if="result.missingSkills?.length" class="tag-section">
              <div class="tag-section-title">缺失技能</div>
              <div class="tag-list">
                <span v-for="skill in result.missingSkills" :key="skill" class="tag tag-missing">{{ skill }}</span>
              </div>
            </div>

            <!-- 简历亮点 -->
            <div v-if="result.highlights?.length" class="tag-section">
              <div class="tag-section-title">简历亮点</div>
              <div class="tag-list">
                <span v-for="h in result.highlights" :key="h" class="tag tag-highlight">{{ h }}</span>
              </div>
            </div>

            <!-- 总结 -->
            <div v-if="result.summary" class="match-summary">
              <div class="tag-section-title">总结</div>
              <p>{{ result.summary }}</p>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="!analyzing" class="match-empty">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--text-3)" stroke-width="1.2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            <p>粘贴岗位 JD，AI 将分析简历匹配度</p>
          </div>

          <!-- 加载状态 -->
          <div v-if="analyzing && !result" class="match-loading">
            <div class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <p>AI 正在分析简历与岗位的匹配度...</p>
          </div>
        </div>

        <!-- 四角缩放手柄 -->
        <div class="match-resize match-resize-tl" @mousedown.stop="startResize($event, 'tl')"></div>
        <div class="match-resize match-resize-tr" @mousedown.stop="startResize($event, 'tr')"></div>
        <div class="match-resize match-resize-bl" @mousedown.stop="startResize($event, 'bl')"></div>
        <div class="match-resize match-resize-br" @mousedown.stop="startResize($event, 'br')"></div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { matchAnalysis } from '../../../api/resume'
import { validateJobDescription } from '../jobDescriptionValidation'

const props = defineProps({
  visible: { type: Boolean, default: false },
  resumeId: { type: [Number, String], default: null },
  moduleData: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['close'])

const jdText = ref('')
const analyzing = ref(false)
const result = ref(null)
const textareaRef = ref(null)
const toast = ref(null)
let toastTimer = null

function showToast(message, type = 'error') {
  toast.value = { message, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value = null }, 4000)
}

const pos = reactive({ x: 0, y: 0 })
const size = reactive({ w: 560, h: 580 })
const MIN_W = 400, MIN_H = 400

function initPos() {
  const w = window.innerWidth
  const h = window.innerHeight
  pos.x = Math.max(0, (w - size.w) / 2 + 100)
  pos.y = Math.max(80, (h - size.h) / 2)
}

watch(() => props.visible, (val) => {
  if (val) initPos()
})

async function analyze() {
  const jd = jdText.value.trim()
  if (!jd || analyzing.value) return
  const validation = validateJobDescription(jd, { minLength: 20 })
  if (!validation.valid) {
    showToast(validation.message)
    result.value = null
    return
  }

  analyzing.value = true
  result.value = null
  try {
    const data = await matchAnalysis(props.resumeId, jd, props.moduleData)
    if (data?.invalidJobDescription) {
      showToast(data.message || '岗位描述无效，请补充完整后再试')
      result.value = null
      return
    }
    result.value = data
  } catch (err) {
    result.value = null
    if (err?.code === 'ECONNABORTED') {
      showToast('分析超时，请重试（AI 分析可能需要较长时间）')
    } else {
      showToast(err?.response?.data?.message || '分析失败，请重试')
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

function autoResizeTextarea() {
  nextTick(() => {
    const el = document.querySelector('.match-textarea')
    if (el) {
      el.style.height = 'auto'
      el.style.height = Math.min(el.scrollHeight, 160) + 'px'
    }
  })
}

// 拖动
let dragging = false
let dragStartX = 0, dragStartY = 0, dragStartPosX = 0, dragStartPosY = 0

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

// 缩放
let resizing = false
let resizeDir = ''
let resizeStartX = 0, resizeStartY = 0
let resizeStartPosX = 0, resizeStartPosY = 0
let resizeStartW = 0, resizeStartH = 0

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
  let newW = resizeStartW, newH = resizeStartH, newX = resizeStartPosX, newY = resizeStartPosY

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
.match-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  pointer-events: none;
}

.match-dialog {
  position: fixed;
  width: 560px;
  height: 580px;
  min-width: 400px;
  min-height: 400px;
  background: #fff;
  pointer-events: auto;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.match-dialog.is-dragging { cursor: default; }

/* 标题栏 */
.match-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border);
  user-select: none;
  flex-shrink: 0;
}
.match-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}
.match-close-btn {
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
.match-close-btn:hover { color: var(--text-1); background: rgba(0,0,0,0.06); }

/* 内容区 */
.match-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  scrollbar-width: thin;
}

/* 输入区 */
.match-input-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
.match-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
}
.match-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-1);
  background: var(--bg-page);
  resize: none;
  min-height: 80px;
  max-height: 160px;
  font-family: inherit;
  box-sizing: border-box;
  transition: border-color 0.15s ease;
}
.match-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
.match-textarea::placeholder { color: var(--text-3); }

.match-analyze-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
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
.match-analyze-btn.active {
  background: var(--primary);
  cursor: pointer;
}
.match-analyze-btn.active:hover { opacity: 0.85; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* 结果区 */
.match-result {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.result-divider {
  border-bottom: 1px solid var(--border);
  margin: 4px 0 0;
}

/* 综合评分环形图 */
.score-overview {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}
.score-ring {
  position: relative;
  width: 100px;
  height: 100px;
}
.score-ring svg { width: 100%; height: 100%; }
.score-ring-arc {
  transition: stroke-dashoffset 0.8s ease;
}
.score-ring-text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.score-number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}
.score-label {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 2px;
}

/* 维度分析 */
.dimensions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.dim-item {
  padding: 10px 12px;
  background: var(--bg-page);
  border-radius: 8px;
}
.dim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.dim-name { font-size: 13px; font-weight: 600; color: var(--text-1); }
.dim-score { font-size: 18px; font-weight: 700; }
.dim-bar-bg {
  height: 6px;
  background: var(--border);
  border-radius: 3px;
  overflow: hidden;
}
.dim-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.8s ease;
}
.dim-analysis {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 8px;
  line-height: 1.5;
}
.dim-suggestion {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 12px;
  color: #f59e0b;
  margin-top: 4px;
  line-height: 1.5;
}
.dim-suggestion svg { flex-shrink: 0; margin-top: 2px; }

/* 标签 */
.tag-section { display: flex; flex-direction: column; gap: 6px; }
.tag-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}
.tag-list { display: flex; flex-wrap: wrap; gap: 6px; }
.tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}
.tag-missing {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fecaca;
}
.tag-highlight {
  background: #ecfdf5;
  color: #10b981;
  border: 1px solid #a7f3d0;
}

/* 总结 */
.match-summary p {
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.6;
  margin: 4px 0 0;
}

/* 空状态 */
.match-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-3);
}
.match-empty p { font-size: 13px; margin: 0; }

/* 加载状态 */
.match-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-3);
}
.match-loading p { font-size: 13px; margin: 0; }
.loading-dots {
  display: flex;
  gap: 6px;
}
.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  animation: dotPulse 1.2s infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotPulse {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1.2); }
}

/* 缩放手柄 */
.match-resize {
  position: absolute;
  width: 12px;
  height: 12px;
  z-index: 2;
  opacity: 0;
  transition: opacity 0.15s;
}
.match-dialog:hover .match-resize { opacity: 1; }
.match-resize-tl { top: 0; left: 0; cursor: nwse-resize; border-top: 2px solid var(--text-3); border-left: 2px solid var(--text-3); border-top-left-radius: 12px; }
.match-resize-tr { top: 0; right: 0; cursor: nesw-resize; border-top: 2px solid var(--text-3); border-right: 2px solid var(--text-3); border-top-right-radius: 12px; }
.match-resize-bl { bottom: 0; left: 0; cursor: nesw-resize; border-bottom: 2px solid var(--text-3); border-left: 2px solid var(--text-3); border-bottom-left-radius: 12px; }
.match-resize-br { bottom: 0; right: 0; cursor: nwse-resize; border-bottom: 2px solid var(--text-3); border-right: 2px solid var(--text-3); border-bottom-right-radius: 12px; }

/* Toast */
.match-toast {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 36px 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-1);
  margin-bottom: 12px;
  flex-shrink: 0;
}
.match-toast.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
}
.match-toast svg { flex-shrink: 0; }
.match-toast span { flex: 0 1 auto; line-height: 1.4; text-align: center; }
.toast-close {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  cursor: pointer;
  color: inherit;
  opacity: 0.5;
  border-radius: 4px;
  flex-shrink: 0;
  transition: opacity 0.15s;
}
.toast-close:hover { opacity: 1; }

.toast-fade-enter-active { transition: all 0.25s ease; }
.toast-fade-leave-active { transition: all 0.2s ease; }
.toast-fade-enter-from { opacity: 0; transform: translateY(-8px); }
.toast-fade-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
