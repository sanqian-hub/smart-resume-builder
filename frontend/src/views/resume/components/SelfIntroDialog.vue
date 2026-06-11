<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="self-intro-overlay" @click.self="emit('close')">
        <div class="self-intro-panel">
          <div class="self-intro-header">
            <div>
              <h3>AI 自我介绍</h3>
              <p>根据当前简历内容生成一段可直接口播的面试开场，并支持导出图片。</p>
            </div>
            <button class="self-intro-close" @click="emit('close')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="self-intro-body">
            <div class="self-intro-form">
              <div class="self-intro-form-inner">
                <div class="self-intro-group">
                  <div class="self-intro-label">时长</div>
                  <div class="self-intro-chip-row">
                    <button
                      v-for="option in durationOptions"
                      :key="option.value"
                      class="self-intro-chip"
                      :class="{ active: form.durationSeconds === option.value }"
                      @click="selectDuration(option.value)"
                    >{{ option.label }}</button>
                  </div>
                </div>

                <div class="self-intro-group">
                  <div class="self-intro-label">风格</div>
                  <div class="self-intro-chip-row">
                    <button
                      v-for="option in styleOptions"
                      :key="option.value"
                      class="self-intro-chip"
                      :class="{ active: form.style === option.value }"
                      @click="selectStyle(option.value)"
                    >{{ option.label }}</button>
                  </div>
                </div>

                <div class="self-intro-group">
                  <div class="self-intro-label">岗位描述（可选）</div>
                  <textarea
                    v-model="form.jobDescription"
                    class="self-intro-textarea"
                    placeholder="可粘贴目标岗位 JD，生成结果会更偏向岗位重点。"
                    rows="5"
                  ></textarea>
                </div>

                <div class="self-intro-actions">
                  <button class="self-intro-btn btn-cancel" :disabled="generating" @click="emit('close')">取消</button>
                  <button class="self-intro-btn btn-confirm" :class="{ active: generating }" :disabled="generating" @click="handleGenerate">
                    <span v-if="generating" class="self-intro-btn-content">
                      <span class="self-intro-spinner"></span>
                      生成中...
                    </span>
                    <span v-else>生成</span>
                  </button>
                </div>

                <div class="self-intro-form-divider"></div>

                <div class="self-intro-helper">
                  <div class="self-intro-helper-title">生成建议</div>
                  <ul class="self-intro-helper-list">
                    <li>岗位描述越具体，生成结果越容易贴近面试场景。</li>
                    <li>30 秒更适合电梯介绍，60 秒更适合常规开场。</li>
                    <li>如果语气不满意，直接重新生成即可，不会改动简历内容。</li>
                  </ul>
                </div>

                <div class="self-intro-helper self-intro-helper-secondary">
                  <div class="self-intro-helper-title">结果操作</div>
                  <ul class="self-intro-helper-list">
                    <li>复制：快速带走当前这版口播稿。</li>
                    <li>导出图片：按当前卡片样式导出 PNG。</li>
                  </ul>
                </div>
              </div>
            </div>

            <div class="self-intro-result">
              <div v-if="errorMessage" class="self-intro-error">{{ errorMessage }}</div>

              <div v-if="result" class="self-intro-result-wrap">
                <div class="self-intro-result-actions">
                  <button class="result-action-btn" :class="{ active: copying }" :disabled="copying" @click="handleCopy">
                    <span v-if="copying" class="self-intro-btn-content">
                      <span class="self-intro-spinner result-spinner"></span>
                      复制中
                    </span>
                    <span v-else-if="copied">已复制</span>
                    <span v-else>复制</span>
                  </button>
                  <button class="result-action-btn" :class="{ active: exportingImage }" :disabled="exportingImage" @click="handleExportImage">
                    <span v-if="exportingImage" class="self-intro-btn-content">
                      <span class="self-intro-spinner result-spinner"></span>
                      导出中
                    </span>
                    <span v-else-if="exportDone">已导出</span>
                    <span v-else>导出图片</span>
                  </button>
                  <button class="result-action-btn" :class="{ active: generating }" :disabled="generating" @click="handleGenerate">
                    <span v-if="generating" class="self-intro-btn-content">
                      <span class="self-intro-spinner result-spinner"></span>
                      生成中
                    </span>
                    <span v-else>重新生成</span>
                  </button>
                </div>

                <div ref="cardRef" class="self-intro-card">
                  <div class="self-intro-card-head">
                    <div>
                      <div class="self-intro-card-title">{{ result.title }}</div>
                      <div class="self-intro-card-sub">{{ currentStyleLabel }} · {{ currentDurationLabel }} · {{ exportTimeLabel }}</div>
                    </div>
                    <div class="self-intro-card-brand">
                      <span class="brand-mark"></span>
                      <span>SmartResume AI</span>
                    </div>
                  </div>
                  <div class="self-intro-card-body">{{ result.content }}</div>
                </div>
              </div>

              <div v-else class="self-intro-empty">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3Z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><path d="M12 19v2"/><path d="M8 21h8"/></svg>
                <p>选择时长和风格后，生成一段适合面试开场的自我介绍。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import html2canvas from 'html2canvas'
import { generateSelfIntro } from '../../../api/selfIntro'
import { validateJobDescription } from '../jobDescriptionValidation'

const props = defineProps({
  visible: { type: Boolean, default: false },
  resumeId: { type: [Number, String], default: null },
  moduleData: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['close'])

const durationOptions = [
  { label: '30秒', value: 30 },
  { label: '60秒', value: 60 },
  { label: '90秒', value: 90 },
]

const styleOptions = [
  { label: '正式稳重', value: 'formal' },
  { label: '自然真诚', value: 'natural' },
  { label: '偏岗位导向', value: 'jobFocused' },
]

const form = reactive({
  durationSeconds: 60,
  style: 'natural',
  jobDescription: '',
})

const generating = ref(false)
const copying = ref(false)
const copied = ref(false)
const exportingImage = ref(false)
const exportDone = ref(false)
const errorMessage = ref('')
const result = ref(null)
const cardRef = ref(null)
const generatedMeta = ref({
  durationSeconds: 60,
  style: 'natural',
  generatedAt: new Date(),
})
let copyTimer = null
let exportTimer = null

const currentStyleLabel = computed(() => styleOptions.find(item => item.value === generatedMeta.value.style)?.label || '自然真诚')
const currentDurationLabel = computed(() => `${generatedMeta.value.durationSeconds}秒`)
const exportTimeLabel = computed(() => {
  const now = generatedMeta.value.generatedAt || new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
})

watch(() => props.visible, (visible) => {
  if (!visible) {
    clearTimeout(copyTimer)
    clearTimeout(exportTimer)
    copying.value = false
    copied.value = false
    exportingImage.value = false
    exportDone.value = false
    errorMessage.value = ''
    generatedMeta.value = {
      durationSeconds: 60,
      style: 'natural',
      generatedAt: new Date(),
    }
  }
})

function selectDuration(value) {
  if (form.durationSeconds !== value) {
    form.durationSeconds = value
  }
}

function selectStyle(value) {
  if (form.style !== value) {
    form.style = value
  }
}

function nextPaint() {
  return new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))
}

async function handleGenerate() {
  if (generating.value) return
  const trimmedJobDescription = form.jobDescription.trim()
  if (trimmedJobDescription) {
    const validation = validateJobDescription(trimmedJobDescription, { minLength: 12 })
    if (!validation.valid) {
      errorMessage.value = validation.message
      return
    }
  }
  generating.value = true
  errorMessage.value = ''
  const minDelay = new Promise(resolve => setTimeout(resolve, 800))
  try {
    const requestMeta = {
      durationSeconds: form.durationSeconds,
      style: form.style,
      generatedAt: new Date(),
    }
    result.value = await generateSelfIntro({
      resumeId: props.resumeId || null,
      durationSeconds: form.durationSeconds,
      style: form.style,
      jobDescription: form.jobDescription.trim(),
      moduleData: props.moduleData,
    })
    generatedMeta.value = requestMeta
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || '生成失败，请重试'
  } finally {
    await minDelay
    generating.value = false
  }
}

async function handleCopy() {
  if (!result.value?.content || copying.value) return
  copying.value = true
  copied.value = false
  clearTimeout(copyTimer)
  const minDelay = new Promise(resolve => setTimeout(resolve, 800))
  try {
    await navigator.clipboard.writeText(result.value.content)
    await minDelay
    copied.value = true
    copyTimer = setTimeout(() => { copied.value = false }, 1800)
  } catch {
    window.prompt('请复制以下内容', result.value.content)
  } finally {
    copying.value = false
  }
}

async function handleExportImage() {
  if (!cardRef.value || exportingImage.value) return
  exportingImage.value = true
  exportDone.value = false
  const minDelay = new Promise(resolve => setTimeout(resolve, 800))
  try {
    await nextTick()
    await nextPaint()
    const canvas = await html2canvas(cardRef.value, {
      backgroundColor: '#ffffff',
      scale: 2,
      useCORS: true,
      logging: false,
    })
    const link = document.createElement('a')
    link.download = `${result.value?.title || 'AI自我介绍'}.png`
    link.href = canvas.toDataURL('image/png')
    await minDelay
    link.click()
    exportDone.value = true
    clearTimeout(exportTimer)
    exportTimer = setTimeout(() => { exportDone.value = false }, 1800)
  } finally {
    exportingImage.value = false
  }
}
</script>

<style scoped>
.self-intro-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(15, 23, 42, 0.24);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.self-intro-panel {
  width: min(1020px, 100%);
  height: min(760px, calc(100vh - 32px));
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.self-intro-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px 16px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.92) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.self-intro-header h3 {
  margin: 0;
  font-size: 18px;
  line-height: 1.2;
  color: var(--text-1);
}

.self-intro-header p {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-3);
}

.self-intro-close {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: none;
  color: var(--text-3);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}
.self-intro-close:hover {
  color: var(--text-1);
  background: rgba(0,0,0,0.06);
}

.self-intro-body {
  display: grid;
  grid-template-columns: minmax(0, 360px) minmax(0, 1fr);
  min-height: 0;
  flex: 1;
  overflow: hidden;
}

.self-intro-form,
.self-intro-result {
  min-height: 0;
  overflow: auto;
  padding: 20px;
}

.self-intro-form {
  border-right: 1px solid var(--border);
  background: #fbfcfe;
}

.self-intro-form-inner {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.self-intro-form-divider {
  width: calc(100% + 40px);
  margin: 18px 0 12px -20px;
  border-top: 1px solid rgba(100, 116, 139, 0.28);
}

.self-intro-helper {
  color: var(--text-3);
}

.self-intro-helper-secondary {
  margin-top: 12px;
}

.self-intro-helper-title {
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-2);
  letter-spacing: 0;
}

.self-intro-helper-list {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  font-size: 12px;
  line-height: 1.65;
}

.self-intro-group + .self-intro-group {
  margin-top: 20px;
}

.self-intro-label {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}

.self-intro-chip-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 10px 0;
}

.self-intro-chip {
  min-width: 92px;
  height: 28px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  color: var(--text-2);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.self-intro-chip.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light, #eef1ff);
}

.self-intro-textarea {
  width: 100%;
  min-height: 128px;
  padding: 12px 13px;
  border: 1px solid var(--border);
  border-radius: 10px;
  resize: vertical;
  font: inherit;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-1);
  background: #fff;
  box-sizing: border-box;
}

.self-intro-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(70, 114, 242, 0.12);
}

.self-intro-actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.self-intro-btn {
  min-width: 108px;
  height: 28px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text-1);
}

.self-intro-btn.btn-confirm.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light, #eef1ff);
}

.self-intro-btn:disabled,
.result-action-btn:disabled {
  cursor: default;
  opacity: 0.8;
}

.self-intro-btn-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  white-space: nowrap;
}

.self-intro-spinner {
  width: 12px;
  height: 12px;
  border: 1.8px solid rgba(70, 114, 242, 0.2);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: self-intro-spin 0.8s linear infinite;
  flex-shrink: 0;
}

.self-intro-error {
  margin-bottom: 12px;
  padding: 11px 12px;
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.18);
  background: #fef2f2;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}

.self-intro-empty {
  height: 100%;
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  color: var(--text-3);
  text-align: center;
}

.self-intro-empty p {
  margin: 0;
  max-width: 420px;
  font-size: 13px;
  line-height: 1.7;
  white-space: nowrap;
}

.self-intro-result-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.self-intro-result-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.result-action-btn {
  min-width: 92px;
  height: 28px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  color: var(--text-2);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.result-action-btn.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light, #eef1ff);
}

.self-intro-card {
  width: 100%;
  max-width: 540px;
  margin: 0 auto;
  padding: 28px 28px 30px;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid rgba(70, 114, 242, 0.14);
  box-shadow: 0 18px 42px rgba(70, 114, 242, 0.08);
  box-sizing: border-box;
}

.self-intro-card-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  padding-bottom: 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
}

.self-intro-card-title {
  font-size: 24px;
  line-height: 1.25;
  font-weight: 700;
  color: #0f172a;
}

.self-intro-card-sub {
  margin-top: 7px;
  font-size: 12px;
  color: #64748b;
}

.self-intro-card-brand {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.brand-mark {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4672f2 0%, #8ba6ff 100%);
  box-shadow: 0 0 0 4px rgba(70, 114, 242, 0.1);
}

.self-intro-card-body {
  color: #1e293b;
  font-size: 18px;
  line-height: 1.95;
  white-space: pre-wrap;
  word-break: break-word;
}

@keyframes self-intro-spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 900px) {
  .self-intro-panel {
    width: min(100%, 760px);
    height: min(860px, calc(100vh - 24px));
  }

  .self-intro-body {
    grid-template-columns: 1fr;
  }

  .self-intro-form {
    border-right: none;
    border-bottom: 1px solid var(--border);
  }

  .self-intro-card {
    max-width: none;
  }
}
</style>
