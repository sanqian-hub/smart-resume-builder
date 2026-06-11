<template>
  <div class="share-page">
    <div v-if="loading" class="share-state">加载中...</div>
    <div v-else-if="expired" class="share-state-card">
      <div class="share-state-icon share-state-icon-muted">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5"/><path d="M12 16h.01"/></svg>
      </div>
      <h3>分享链接已过期</h3>
      <p>该简历分享已失效，请联系分享者重新生成链接。</p>
    </div>
    <div v-else-if="needPassword" class="share-state-card">
      <div class="share-state-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="10" rx="2"/><path d="M7 11V8a5 5 0 0 1 10 0v3"/></svg>
      </div>
      <h3>请输入访问密码</h3>
      <p>这份简历已设置访问密码，输入 6 位数字后即可查看。</p>
      <input
        v-model="password"
        class="share-password-input"
        type="text"
        inputmode="numeric"
        maxlength="6"
        placeholder="请输入 6 位数字密码"
        @input="sanitizePassword"
        @keyup.enter="submitPassword"
      />
      <div v-if="verifyError" class="share-password-error">{{ verifyError }}</div>
      <button class="share-password-btn" :disabled="verifying || password.length !== 6" @click="submitPassword">
        {{ verifying ? '验证中...' : '查看简历' }}
      </button>
    </div>
    <div v-else-if="error" class="share-state share-state-error">{{ error }}</div>
    <div v-else-if="resume" class="share-page-body">
      <div v-if="isMobileView" ref="hintRef" class="share-mobile-hint">双指放大可查看细节</div>
      <div class="share-paper-wrap" :class="{ 'is-mobile': isMobileView }">
        <div
          ref="viewportRef"
          class="share-viewport"
          :class="{ 'is-mobile': isMobileView }"
        >
          <div class="share-canvas" :style="canvasStyle">
            <div ref="paperRef" class="share-paper" :style="paperStyle">
              <component
                :is="currentTemplate"
                :contents="resume.contents || []"
                :theme-color="themeColor"
                :rich-font-family="richFontFamily"
                :rich-font-size="richFontSize"
                :rich-line-height="richLineHeight"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getPublicShare, verifyPublicShare } from '../../api/share'
import { readStyleConfig } from './styleConfig'
import { DEFAULT_TEMPLATE_ID, normalizeTemplateId, TEMPLATE_COMPONENTS } from './templateRegistry'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const resume = ref(null)
const expired = ref(false)
const needPassword = ref(false)
const password = ref('')
const verifying = ref(false)
const verifyError = ref('')
const viewportRef = ref(null)
const paperRef = ref(null)
const hintRef = ref(null)
const viewportWidth = ref(0)
const paperHeight = ref(1123)
const isMobileView = ref(false)
const PAPER_WIDTH = 794
const MOBILE_BREAKPOINT = 768
const MOBILE_DEFAULT_SCALE_RATIO = 0.998
let resizeObserver = null

const currentTemplate = computed(() => {
  const tpl = normalizeTemplateId(resume.value?.currentTemplate || DEFAULT_TEMPLATE_ID)
  return TEMPLATE_COMPONENTS[tpl] || TEMPLATE_COMPONENTS[DEFAULT_TEMPLATE_ID]
})

const styleConfig = computed(() => {
  const basic = resume.value?.contents?.find(c => c.moduleType === 'basic')
  return readStyleConfig(resume.value?.styleConfig, basic?.contentJson)
})

const themeColor = computed(() => styleConfig.value.themeColor)
const richFontFamily = computed(() => styleConfig.value.richFontFamily)
const richFontSize = computed(() => styleConfig.value.richFontSize)
const richLineHeight = computed(() => styleConfig.value.richLineHeight)

const mobileScale = computed(() => {
  if (!viewportWidth.value) return 1
  const fitWidthScale = Math.min(1, viewportWidth.value / PAPER_WIDTH)
  if (!isMobileView.value) return 1
  return Math.max(0.3, fitWidthScale * MOBILE_DEFAULT_SCALE_RATIO)
})

const canvasStyle = computed(() => {
  if (!isMobileView.value) {
    return {
      width: '100%',
      minHeight: `${paperHeight.value}px`,
    }
  }
  return {
    width: `${PAPER_WIDTH * mobileScale.value}px`,
    height: `${paperHeight.value * mobileScale.value}px`,
  }
})

const paperStyle = computed(() => {
  if (!isMobileView.value) return {}
  return {
    transform: `scale(${mobileScale.value})`,
    transformOrigin: 'top center',
  }
})

function syncViewportMode() {
  isMobileView.value = window.innerWidth <= MOBILE_BREAKPOINT
}

function sanitizePassword() {
  password.value = password.value.replace(/\D/g, '').slice(0, 6)
  verifyError.value = ''
}

function syncMeasurements() {
  syncViewportMode()
  viewportWidth.value = viewportRef.value?.clientWidth || 0
  const measuredHeight = Math.ceil(paperRef.value?.scrollHeight || paperRef.value?.offsetHeight || 0)
  if (isMobileView.value) {
    paperHeight.value = measuredHeight || 1123
  } else {
    paperHeight.value = Math.max(1123, measuredHeight || 1123)
  }
}

function setupResizeObserver() {
  resizeObserver?.disconnect()
  resizeObserver = null
  if (!viewportRef.value || !paperRef.value) return
  resizeObserver = new ResizeObserver(() => syncMeasurements())
  resizeObserver.observe(viewportRef.value)
  resizeObserver.observe(paperRef.value)
  window.addEventListener('resize', syncMeasurements)
  syncMeasurements()
}

async function loadShare() {
  loading.value = true
  error.value = ''
  expired.value = false
  needPassword.value = false
  resume.value = null
  try {
    const result = await getPublicShare(route.params.shareKey)
    expired.value = !!result?.expired
    needPassword.value = !!result?.needPassword
    resume.value = result?.resume || null
  } catch (e) {
    error.value = e?.message || '分享简历加载失败'
  } finally {
    loading.value = false
  }
}

async function submitPassword() {
  if (password.value.length !== 6 || verifying.value) return
  verifying.value = true
  verifyError.value = ''
  try {
    resume.value = await verifyPublicShare(route.params.shareKey, password.value)
    needPassword.value = false
    expired.value = false
    requestAnimationFrame(() => setupResizeObserver())
  } catch (e) {
    verifyError.value = e?.message || '访问密码错误'
  } finally {
    verifying.value = false
  }
}

onMounted(async () => {
  await loadShare()
  requestAnimationFrame(() => setupResizeObserver())
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  window.removeEventListener('resize', syncMeasurements)
})
</script>

<style scoped>
.share-page {
  min-height: 100vh;
  -webkit-text-size-adjust: 100%;
  background:
    radial-gradient(circle at top, rgba(70, 114, 242, 0.08), transparent 32%),
    linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
  padding: 32px 24px;
}

.share-page-body {
  max-width: 1180px;
  margin: 0 auto;
}

.share-mobile-hint {
  width: fit-content;
  margin: 0 auto 12px;
  padding: 7px 12px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.share-paper-wrap {
  display: flex;
  justify-content: center;
}

.share-paper-wrap.is-mobile {
  display: block;
}

.share-viewport {
  width: 100%;
}

.share-viewport:not(.is-mobile) .share-paper {
  margin: 0 auto;
}

.share-viewport.is-mobile {
  touch-action: pan-x pan-y pinch-zoom;
}

.share-canvas {
  margin: 0 auto;
}

.share-paper {
  width: 794px;
  min-height: 1123px;
  background: #fff;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.14);
  border-radius: 2px;
}

.share-state {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-2);
  font-size: 14px;
}

.share-state-error {
  color: var(--text-3);
}

.share-state-card {
  width: min(420px, calc(100vw - 32px));
  min-height: calc(100vh - 64px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-1);
}

.share-state-card h3 {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 700;
}

.share-state-card p {
  margin: 0 0 18px;
  color: var(--text-3);
  font-size: 14px;
  line-height: 1.6;
}

.share-state-icon {
  width: 52px;
  height: 52px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: var(--primary);
  background: rgba(79, 70, 229, 0.08);
}

.share-state-icon-muted {
  color: #64748b;
  background: rgba(148, 163, 184, 0.14);
}

.share-password-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-1);
  font-size: 14px;
  text-align: center;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.share-password-input:focus {
  border-color: rgba(79, 70, 229, 0.42);
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.08);
}

.share-password-error {
  margin-top: 10px;
  font-size: 12px;
  color: #dc2626;
}

.share-password-btn {
  width: 100%;
  height: 42px;
  margin-top: 16px;
  border: none;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.share-password-btn:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
}

.share-password-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .share-page {
    padding: 16px 12px;
  }
}

@media (max-width: 768px) {
  .share-page {
    padding: 8px 2px 4px;
  }

  .share-page-body {
    display: block;
    min-height: auto;
  }

  .share-paper-wrap {
    padding: 6px 0 0;
  }

  .share-paper-wrap.is-mobile {
    min-height: 0;
  }

  .share-mobile-hint {
    margin: 0 auto 6px;
    padding: 6px 10px;
    font-size: 11px;
  }

  .share-paper {
    min-height: auto;
    box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12);
  }

  .share-viewport.is-mobile {
    padding: 0 2px 1px;
  }
}
</style>
