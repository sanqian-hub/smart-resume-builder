<template>
  <div class="home">
    <div class="home-inner">
      <div class="home-decoration" aria-hidden="true">
        <div class="home-decoration__note home-decoration__note--left">
          <span class="home-decoration__chip">轻轻提醒</span>
          <strong>今天也把亮点写出来</strong>
          <span class="home-decoration__sub">别让好项目躲在角落</span>
        </div>
        <div class="home-decoration__art home-decoration__art--right">
          <svg class="home-decoration__svg" viewBox="0 0 220 220" fill="none" aria-label="装饰插画" role="img">
            <rect x="66" y="38" width="102" height="138" rx="18" fill="currentColor" fill-opacity="0.12" />
            <rect x="82" y="56" width="70" height="6" rx="3" fill="currentColor" fill-opacity="0.24" />
            <rect x="82" y="74" width="56" height="6" rx="3" fill="currentColor" fill-opacity="0.18" />
            <rect x="82" y="92" width="62" height="6" rx="3" fill="currentColor" fill-opacity="0.18" />
            <rect x="82" y="110" width="48" height="6" rx="3" fill="currentColor" fill-opacity="0.18" />
            <path d="M158 38h10a18 18 0 0 1 18 18v10" stroke="currentColor" stroke-opacity="0.28" stroke-width="5" stroke-linecap="round" />
            <path d="M94 38h-10a18 18 0 0 0-18 18v104" stroke="currentColor" stroke-opacity="0.2" stroke-width="5" stroke-linecap="round" />
            <circle cx="48" cy="64" r="10" fill="currentColor" fill-opacity="0.16" />
            <circle cx="174" cy="150" r="12" fill="currentColor" fill-opacity="0.12" />
            <path d="M182 62l6 10 10 6-10 6-6 10-6-10-10-6 10-6 6-10Z" fill="currentColor" fill-opacity="0.16" />
          </svg>
          <span class="home-decoration__caption">简历也能有一点小脾气</span>
        </div>
      </div>
      <!-- Header -->
      <div class="home-header">
        <div>
          <h1 class="home-title">我的简历</h1>
          <p class="home-desc">创建、编辑和管理你的简历</p>
          <p class="home-welcome">开始创建你的第一份专业简历</p>
        </div>
        <button v-if="list.length > 0" class="btn-new" @click="$router.push('/edit')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新建简历
        </button>
      </div>

      <!-- Content -->
      <!-- Empty -->
      <div v-if="!loading && list.length === 0" class="empty">
        <div class="empty-visual">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </div>
        <p class="empty-text">{{ emptyTitleText }}</p>
        <p class="empty-hint">{{ emptyHintText }}</p>
        <div class="empty-actions">
          <button class="btn-new btn-empty" @click="$router.push('/edit')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.375 2.625a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4Z"/>
            </svg>
            {{ emptyActionText }}
          </button>
          <button
            v-if="isGuestHome"
            class="btn-empty-secondary"
            @click="$router.push('/login')"
          >
            已有账号，去登录
          </button>
        </div>
      </div>

      <!-- List -->
      <div v-else-if="!loading" class="resume-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="resume-item"
          @click="$router.push(`/edit/${item.id}`)"
        >
          <div class="item-main">
            <div class="item-icon" :style="{ background: templateColors[normalizeTemplateId(item.currentTemplate)] || templateColors['classic-1'] }">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="item-info">
              <span class="item-name">{{ item.title }}</span>
              <span class="item-meta">
                {{ templateLabels[normalizeTemplateId(item.currentTemplate)] || item.currentTemplate }}
                <template v-if="item.updateTime"> · {{ formatDate(item.updateTime) }}</template>
              </span>
            </div>
          </div>
          <div class="item-actions" @click.stop>
            <button
              class="item-btn item-btn--share"
              :class="{ copied: copiedShareId === item.id || generatedShareId === item.id, error: errorShareId === item.id, loading: sharingShareId === item.id }"
              :aria-label="shareButtonTitle(item.id)"
              :data-tooltip="shareButtonTitle(item.id)"
              :disabled="sharingShareId === item.id"
              @click="handleShare(item.id)"
              >
                <span class="item-btn-icon" aria-hidden="true">
                  <svg class="item-btn-icon-base" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="18" cy="5" r="3"/>
                    <circle cx="6" cy="12" r="3"/>
                    <circle cx="18" cy="19" r="3"/>
                    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                  </svg>
                  <span v-if="sharingShareId === item.id" class="item-btn-spinner item-btn-overlay"></span>
                  <svg v-else-if="copiedShareId === item.id || generatedShareId === item.id" class="item-btn-overlay" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 6 9 17l-5-5"/>
                  </svg>
                  <svg v-else class="item-btn-overlay" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="18" cy="5" r="3"/>
                    <circle cx="6" cy="12" r="3"/>
                    <circle cx="18" cy="19" r="3"/>
                    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                  </svg>
                </span>
              </button>
            <button class="item-btn item-btn--history" aria-label="历史版本" data-tooltip="历史版本" @click="openVersions(item)">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            </button>
            <button class="item-btn item-btn--preview" aria-label="预览" data-tooltip="预览" @click="$router.push(`/preview/${item.id}`)">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
            <button class="item-btn item-btn--del" aria-label="删除" data-tooltip="删除" @click="askDelete(item)">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

    <!-- Delete modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="deleteTarget" class="modal-overlay" @click.self="closeDeleteModal">
          <div class="modal-box">
            <div class="modal-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
            </div>
            <h3 class="modal-title">删除简历</h3>
            <p class="modal-desc">确定要删除「{{ deleteTarget.title }}」吗？此操作不可撤销。</p>
            <div class="modal-actions">
              <button class="modal-btn modal-btn--cancel" :disabled="deleting" @click="closeDeleteModal">取消</button>
              <button class="modal-btn modal-btn--danger" :disabled="deleting" @click="handleDelete">
                <span v-if="deleting" class="share-confirm-content">
                  <span class="share-confirm-spinner"></span>
                  删除中...
                </span>
                <span v-else>确认删除</span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="toast">
        <div v-if="toastMsg" class="resume-list-toast" :class="`resume-list-toast--${toastType}`">{{ toastMsg }}</div>
      </Transition>
    </Teleport>

    <!-- Version preview panel -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="versionResumeId" class="modal-overlay" @click.self="closeVersionPanel">
          <div class="version-panel">
            <div class="version-panel-header">
              <h3 class="version-panel-title">历史版本</h3>
              <button class="version-panel-close" @click="closeVersionPanel">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="version-panel-body">
              <div class="version-panel-list">
                <div v-if="versionLoading" class="version-panel-loading">加载中...</div>
                <div v-else-if="versionList.length === 0" class="version-panel-empty">暂无版本记录</div>
                <div
                  v-for="v in versionList"
                  :key="v.id"
                  class="version-panel-item"
                  :class="{ active: previewVersion && previewVersion._versionId === v.id }"
                  @click="selectPreviewVersion(v)"
                >
                  <span class="version-panel-num">v{{ v.versionNum }}</span>
                  <span class="version-panel-time">{{ v.createTime?.replace('T', ' ')?.substring(0, 16) }}</span>
                  <span class="version-panel-remark">{{ v.remark || '手动保存' }}</span>
                </div>
              </div>
              <div v-if="previewVersion" class="version-panel-preview">
                <div class="version-preview-paper">
                  <component
                    :is="getSnapshotTemplate(previewVersion)"
                    :contents="previewVersion.contents"
                    :theme-color="getSnapshotPresentation(previewVersion).themeColor"
                    :rich-font-family="getSnapshotPresentation(previewVersion).richFontFamily"
                    :rich-font-size="getSnapshotPresentation(previewVersion).richFontSize"
                    :rich-line-height="getSnapshotPresentation(previewVersion).richLineHeight"
                  />
                </div>
              </div>
              <div v-else class="version-panel-placeholder">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                <p>选择左侧版本查看预览</p>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showShareConfigDialog" class="share-config-overlay" @click.self="closeShareConfigDialog">
          <div class="share-config-panel">
            <div class="share-config-header">
              <div>
                <h3>分享设置</h3>
                <p>为这次分享设置访问密码和链接有效期，生成后会自动复制链接。</p>
              </div>
              <button class="share-config-close" @click="closeShareConfigDialog">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="share-config-body">
              <div class="share-config-group">
                <div class="share-config-label">访问密码</div>
                <div class="share-password-mode">
                  <button class="share-segment-btn" :class="{ active: !shareConfig.usePassword }" @click="setSharePasswordMode(false)">不设置</button>
                  <button class="share-segment-btn" :class="{ active: shareConfig.usePassword }" @click="setSharePasswordMode(true)">设置密码</button>
                </div>
                <div v-if="shareConfig.usePassword" class="share-password-box">
                  <input
                    v-model="shareConfig.password"
                    class="share-config-input"
                    type="text"
                    inputmode="numeric"
                    maxlength="6"
                    placeholder="请输入 6 位数字密码"
                    @input="sanitizeSharePassword"
                  />
                  <div class="share-config-hint">密码为 6 位纯数字，可在分享管理中查看或修改。</div>
                </div>
              </div>
              <div class="share-config-group">
                <div class="share-config-label">有效期</div>
                <div class="share-expire-options">
                  <button
                    v-for="option in expireOptions"
                    :key="option.value"
                    class="share-chip-btn"
                    :class="{ active: shareConfig.expireMode === option.value }"
                    @click="selectShareExpire(option.value)"
                  >{{ option.label }}</button>
                </div>
                <div v-if="shareConfig.expireMode === 'custom'" class="share-custom-expire">
                  <input
                    v-model="shareConfig.customDays"
                    class="share-config-input share-config-input-sm"
                    type="text"
                    inputmode="numeric"
                    placeholder="输入天数"
                    @input="sanitizeCustomDays"
                  />
                  <span class="share-custom-unit">天后过期</span>
                </div>
                <div class="share-config-hint">默认 30 天；永久有效的链接也可在分享管理中手动关闭。</div>
              </div>
            </div>
            <div class="share-config-actions">
              <button class="modal-btn modal-btn--cancel" @click="closeShareConfigDialog">取消</button>
              <button class="modal-btn modal-btn--confirm" :class="{ success: shareConfirmUi.success }" :disabled="sharingShareId !== null || !canSubmitShareConfig || isShareDialogSuccess" @click="confirmShareConfig">
                <span v-if="shareConfirmUi.loading" class="share-confirm-content">
                  <span class="share-confirm-spinner"></span>
                  {{ shareConfirmUi.label }}
                </span>
                <span v-else-if="shareConfirmUi.success" class="share-confirm-content">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 6 9 17l-5-5"/>
                  </svg>
                  {{ shareConfirmUi.label }}
                </span>
                <span v-else>
                  {{ shareConfirmUi.label }}
                </span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { myList, deleteResume } from '../../api/resume'
import { listVersions } from '../../api/version'
import { createShare } from '../../api/share'
import { useResumeListCache } from '../../composables/useResumeListCache'
import { useRuntimeLoader } from '../../composables/useRuntimeLoader'
import { useUserStore } from '../../stores/user'
import { readSnapshotPresentation } from './styleConfig'
import { getListShareConfirmUi, shouldKeepListShareDialogOpen } from './listShareDialogState'
import {
  DEFAULT_TEMPLATE_ID,
  normalizeTemplateId,
  TEMPLATE_COLORS,
  TEMPLATE_COMPONENTS,
  TEMPLATE_LABELS,
} from './templateRegistry'

const { cache, refresh, clear } = useResumeListCache()
const { hide: hideRuntimeLoader } = useRuntimeLoader()
const userStore = useUserStore()

const list = ref(userStore.user ? (cache.value ?? []) : [])
const loading = ref(userStore.user ? cache.value == null : false)
const deleteTarget = ref(null)
const deleting = ref(false)
const toastMsg = ref('')
const toastType = ref('success')
const versionResumeId = ref(null)
const versionList = ref([])
const versionLoading = ref(false)
const previewVersion = ref(null)
const sharingShareId = ref(null)
const copiedShareId = ref(null)
const generatedShareId = ref(null)
const errorShareId = ref(null)
const showShareConfigDialog = ref(false)
const shareTargetResumeId = ref(null)
const shareDialogState = ref('idle')
let shareStateTimer = null
let toastTimer = null
const expireOptions = [
  { label: '7天', value: '7' },
  { label: '30天', value: '30' },
  { label: '永久有效', value: 'forever' },
  { label: '自定义', value: 'custom' },
]
const shareConfig = reactive({
  usePassword: false,
  password: '',
  expireMode: '30',
  customDays: '',
})

const templateLabels = TEMPLATE_LABELS

const templateColors = TEMPLATE_COLORS
const isGuestHome = computed(() => !userStore.user)
const emptyTitleText = computed(() =>
  isGuestHome.value ? '还没有简历，先去体验编辑器' : '还没有简历，开始创建你的第一份',
)
const emptyHintText = computed(() =>
  isGuestHome.value ? '无需登录，先看看编辑、预览和导出的流程' : '创建简历只需 3 分钟',
)
const emptyActionText = computed(() => (isGuestHome.value ? '去体验编辑器' : '新建简历'))

const canSubmitShareConfig = computed(() => {
  if (shareConfig.usePassword && shareConfig.password.length !== 6) return false
  if (shareConfig.expireMode === 'custom') {
    const days = Number(shareConfig.customDays)
    return Number.isInteger(days) && days > 0
  }
  return true
})

const shareConfirmUi = computed(() => getListShareConfirmUi(shareDialogState.value, shareConfig.usePassword))
const isShareDialogSuccess = computed(() => shareDialogState.value === 'copied' || shareDialogState.value === 'generated')

function formatDate(str) {
  if (!str) return ''
  return str.replace('T', ' ')
}

async function loadList() {
  try {
    if (!userStore.user) {
      list.value = []
      clear()
      return
    }
    list.value = await myList()
    cache.value = list.value
  } finally {
    loading.value = false
    hideRuntimeLoader()
  }
}

function askDelete(item) {
  deleteTarget.value = item
}

function closeDeleteModal() {
  if (deleting.value) return
  deleteTarget.value = null
}

function showToast(message, type = 'success', duration = 1800) {
  toastMsg.value = message
  toastType.value = type
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toastMsg.value = ''
  }, duration)
}

async function handleDelete() {
  if (!deleteTarget.value || deleting.value) return
  const id = deleteTarget.value.id
  deleting.value = true
  const minDelay = new Promise(resolve => setTimeout(resolve, 800))
  try {
    await Promise.all([deleteResume(id), minDelay])
    deleteTarget.value = null
    await loadList()
    showToast('删除成功', 'success')
  } catch (e) {
    await minDelay
    showToast('删除失败，请稍后重试', 'error')
  } finally {
    deleting.value = false
  }
}

async function openVersions(item) {
  versionResumeId.value = item.id
  previewVersion.value = null
  versionLoading.value = true
  try {
    versionList.value = await listVersions(item.id)
    if (versionList.value.length > 0) {
      selectPreviewVersion(versionList.value[0])
    }
  } catch {}
  versionLoading.value = false
}

function selectPreviewVersion(v) {
  if (!v.snapshotJson) return
  try {
    const parsed = JSON.parse(v.snapshotJson)
    parsed._versionId = v.id
    previewVersion.value = parsed
  } catch {}
}

function getSnapshotPresentation(snapshot) {
  return readSnapshotPresentation(snapshot)
}

function getSnapshotTemplate(snapshot) {
  return TEMPLATE_COMPONENTS[getSnapshotPresentation(snapshot).template] || TEMPLATE_COMPONENTS[DEFAULT_TEMPLATE_ID]
}

function closeVersionPanel() {
  versionResumeId.value = null
  versionList.value = []
  previewVersion.value = null
}

function clearShareState() {
  copiedShareId.value = null
  generatedShareId.value = null
  errorShareId.value = null
}

function setShareState(type, resumeId) {
  clearShareState()
  if (type === 'copied') copiedShareId.value = resumeId
  if (type === 'generated') generatedShareId.value = resumeId
  if (type === 'error') errorShareId.value = resumeId
  clearTimeout(shareStateTimer)
  shareStateTimer = setTimeout(clearShareState, 1800)
}

function shareButtonTitle(resumeId) {
  if (sharingShareId.value === resumeId) return '复制中'
  if (copiedShareId.value === resumeId) return '已复制'
  if (generatedShareId.value === resumeId) return '已生成'
  if (errorShareId.value === resumeId) return '分享失败'
  return '分享'
}

function resetShareConfig() {
  shareConfig.usePassword = false
  shareConfig.password = ''
  shareConfig.expireMode = '30'
  shareConfig.customDays = ''
  shareDialogState.value = 'idle'
}

function setSharePasswordMode(enabled) {
  shareConfig.usePassword = enabled
  if (!enabled) shareConfig.password = ''
}

function sanitizeSharePassword() {
  shareConfig.password = shareConfig.password.replace(/\D/g, '').slice(0, 6)
}

function selectShareExpire(value) {
  shareConfig.expireMode = value
  if (value !== 'custom') shareConfig.customDays = ''
}

function sanitizeCustomDays() {
  shareConfig.customDays = shareConfig.customDays.replace(/\D/g, '').slice(0, 4)
}

function currentShareExpireDays() {
  if (shareConfig.expireMode === 'forever') return 0
  if (shareConfig.expireMode === 'custom') return Number(shareConfig.customDays)
  return Number(shareConfig.expireMode)
}

function closeShareConfigDialog() {
  if (sharingShareId.value !== null) return
  showShareConfigDialog.value = false
  shareTargetResumeId.value = null
  resetShareConfig()
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    window.prompt('请复制以下链接', text)
    return false
  }
}

async function handleShare(resumeId) {
  if (sharingShareId.value) return
  shareTargetResumeId.value = resumeId
  resetShareConfig()
  showShareConfigDialog.value = true
}

async function confirmShareConfig() {
  if (shareTargetResumeId.value == null || sharingShareId.value || !canSubmitShareConfig.value || isShareDialogSuccess.value) return
  const resumeId = shareTargetResumeId.value
  sharingShareId.value = resumeId
  shareDialogState.value = 'submitting'
  const minDelay = new Promise(r => setTimeout(r, 800))
  let nextState = ''
  try {
    const shareKey = await createShare(resumeId, null, {
      password: shareConfig.usePassword ? shareConfig.password : '',
      expireDays: currentShareExpireDays(),
    })
    const shareUrl = `${window.location.origin}/share/${shareKey}`
    const copied = await copyText(shareUrl)
    nextState = copied ? 'copied' : 'generated'
  } catch (e) {
    nextState = 'error'
  } finally {
    await minDelay
    sharingShareId.value = null
    shareDialogState.value = nextState || 'idle'
    setShareState(nextState, resumeId)
    if (!shouldKeepListShareDialogOpen(nextState)) {
      showShareConfigDialog.value = false
      shareTargetResumeId.value = null
      resetShareConfig()
    }
  }
}

onMounted(() => {
  if (userStore.user && cache.value != null) {
    requestAnimationFrame(() => hideRuntimeLoader())
  }
  if (!userStore.user) {
    list.value = []
    loading.value = false
    clear()
    hideRuntimeLoader()
    return
  }
  loadList()
})
</script>

<style scoped>
.home {
  min-height: calc(100vh - 56px);
  position: relative;
  overflow: hidden;
}

.home-inner {
  max-width: 960px;
  margin: 0 auto;
  padding-top: 40px;
  position: relative;
  z-index: 1;
}

.home-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  color: var(--primary);
}

.home-decoration__note,
.home-decoration__art {
  position: absolute;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
}

.home-decoration__note {
  top: 334px;
  left: calc(50% - 720px);
  width: 172px;
  transform: rotate(-4deg);
  animation: home-float-left 8s ease-in-out infinite;
}

.home-decoration__note strong {
  font-size: 17px;
  line-height: 1.25;
  color: rgba(23, 43, 90, 0.7);
  letter-spacing: -0.2px;
}

.home-decoration__chip {
  width: fit-content;
  padding: 3px 7px;
  border-radius: 999px;
  background: rgba(70, 114, 242, 0.06);
  color: rgba(70, 114, 242, 0.5);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.home-decoration__sub,
.home-decoration__caption {
  font-size: 12px;
  line-height: 1.45;
  color: rgba(71, 85, 105, 0.52);
}

.home-decoration__art {
  top: 410px;
  right: calc(50% - 728px);
  width: 180px;
  align-items: center;
  gap: 8px;
  transform: rotate(6deg);
  animation: home-float-right 9s ease-in-out infinite;
}

.home-decoration__svg {
  width: 134px;
  height: 134px;
  color: var(--primary);
  opacity: 0.42;
}

@keyframes home-float-left {
  0%, 100% { transform: translateY(0) rotate(-4deg); }
  50% { transform: translateY(-8px) rotate(-3deg); }
}

@keyframes home-float-right {
  0%, 100% { transform: translateY(0) rotate(6deg); }
  50% { transform: translateY(10px) rotate(7deg); }
}

@media (max-width: 900px) {
  .home-inner {
    padding-top: 24px;
  }
  .home-header {
    flex-wrap: wrap;
    gap: 12px;
    align-items: flex-start;
  }
  .home-decoration {
    display: none;
  }
}

@media (max-width: 1280px) {
  .home-decoration {
    display: none;
  }
}

.home-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
}

.home-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-1);
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.home-desc {
  margin-top: 6px;
  font-size: 14px;
  color: var(--text-3);
}

.home-welcome {
  margin-top: 2px;
  font-size: 13px;
  color: var(--text-3);
}

.btn-new {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: var(--primary);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s, box-shadow 0.15s;
  flex-shrink: 0;
}

.btn-new:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(70, 114, 242, 0.25);
}

.btn-new:active {
  transform: translateY(0);
}

/* Empty */
.empty {
  text-align: center;
  padding: 64px 0;
}

.empty-visual {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: var(--primary-light);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.empty-text {
  font-size: 14px;
  color: var(--text-3);
}

.empty-hint {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-3);
  opacity: 0.7;
}

.btn-empty {
  margin-top: 20px;
}

.empty-actions {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-actions .btn-new,
.empty-actions .btn-new {
  width: 138px;
  justify-content: center;
}

.btn-empty-secondary {
  padding: 8px 18px;
  border: none;
  border-radius: 8px;
  background: #34b3a0;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s, box-shadow 0.15s;
}

.btn-empty-secondary:hover {
  background: #2fa290;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 179, 160, 0.25);
}

.btn-empty-secondary:active {
  transform: translateY(0);
}

/* List */
.resume-list {
  display: flex;
  flex-direction: column;
}

.resume-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
  margin-bottom: 10px;
}

.resume-item:first-child {
  border-radius: 10px;
}

.resume-item:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 16px rgba(70, 114, 242, 0.12);
}

.item-main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.item-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 12px;
  color: var(--text-3);
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 30px;
  min-height: 30px;
  opacity: 0.5;
  transition: opacity 0.15s;
  flex-shrink: 0;
}

.resume-item:hover .item-actions {
  opacity: 1;
}

.item-btn {
  position: relative;
  width: 30px;
  height: 30px;
  min-width: 30px;
  min-height: 30px;
  padding: 0;
  box-sizing: border-box;
  appearance: none;
  -webkit-appearance: none;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  vertical-align: middle;
  font: inherit;
  color: var(--text-2);
  transition: all 0.15s;
  flex: 0 0 30px;
}

.item-btn > svg {
  display: block;
  flex-shrink: 0;
}

.item-btn-icon {
  position: relative;
  width: 15px;
  height: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 15px;
  overflow: hidden;
}

.item-btn-icon > svg {
  display: block;
  flex-shrink: 0;
}

.item-btn-icon-base {
  visibility: hidden;
}

.item-btn-overlay {
  position: absolute;
  inset: 0;
  margin: auto;
}

.item-btn-spinner {
  width: 14px;
  height: 14px;
  display: block;
  flex-shrink: 0;
  box-sizing: border-box;
  border-radius: 50%;
  border: 2px solid rgba(8, 145, 178, 0.22);
  border-top-color: currentColor;
  animation: share-spin 0.5s linear infinite;
}

.item-btn::after {
  content: attr(data-tooltip);
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  z-index: 20;
  padding: 7px 10px;
  color: var(--text-2);
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 8px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12), 0 1px 4px rgba(15, 23, 42, 0.04);
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transform: translateX(-50%) translateY(4px);
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.item-btn:hover::after,
.item-btn:focus-visible::after {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.item-btn:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.item-btn--share:hover {
  color: #0891b2;
  border-color: rgba(8, 145, 178, 0.34);
  background: #ecfeff;
}

.item-btn--history:hover {
  color: #d97706;
  border-color: rgba(217, 119, 6, 0.38);
  background: #fffbeb;
}

.item-btn--preview:hover {
  color: #4f46e5;
  border-color: rgba(79, 70, 229, 0.34);
  background: #eef2ff;
}

.item-btn.copied {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.item-btn.error {
  color: var(--danger);
  border-color: rgba(239, 68, 68, 0.28);
  background: #fef2f2;
}

.item-btn.loading {
  cursor: default;
}

.item-btn--share.loading {
  color: #0891b2;
  border-color: rgba(8, 145, 178, 0.34);
  background: #ecfeff;
}

.item-btn--share.loading::after,
.item-btn--share.loading:focus-visible::after {
  opacity: 0;
  transform: translateX(-50%) translateY(4px);
}

.share-confirm-content {
  min-height: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  line-height: 1;
}

.share-confirm-spinner {
  width: 14px;
  height: 14px;
  box-sizing: border-box;
  display: block;
  flex-shrink: 0;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.36);
  border-top-color: #fff;
  animation: share-spin 0.8s linear infinite;
}

@keyframes share-spin {
  to { transform: rotate(360deg); }
}

.item-btn--del:hover {
  color: var(--danger);
  border-color: var(--danger);
  background: #fef2f2;
}

/* Delete modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
}

.modal-box {
  width: 360px;
  padding: 28px 24px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  text-align: center;
}

.modal-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #fef2f2;
  color: var(--danger);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 8px;
}

.modal-desc {
  font-size: 13px;
  color: var(--text-3);
  line-height: 1.5;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 10px;
}

.modal-btn {
  flex: 1;
  height: 38px;
  min-height: 38px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  box-sizing: border-box;
  transition: all 0.15s;
}

.modal-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-btn--cancel {
  color: var(--text-2);
  background: var(--bg-page);
}

.modal-btn--cancel:hover:not(:disabled) {
  background: #e5e7eb;
  color: var(--text-1);
}

.modal-btn--danger {
  color: #fff;
  background: var(--danger);
}

.modal-btn--danger:hover:not(:disabled) {
  background: #dc2626;
}

.modal-btn--confirm {
  color: #fff;
  background: var(--primary);
}

.modal-btn--confirm.success {
  background: #059669;
}

.modal-btn--confirm:hover:not(:disabled) {
  background: var(--primary-hover);
}

.modal-btn--confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal animation */
.modal-enter-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal-box {
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-leave-active {
  transition: opacity 0.15s ease;
}

.modal-leave-active .modal-box {
  transition: all 0.12s ease-in;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-box,
.modal-leave-to .modal-box {
  opacity: 0;
  transform: scale(0.95) translateY(8px);
}

.resume-list-toast {
  position: fixed;
  top: 120px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10020;
  padding: 10px 28px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  line-height: 1;
  box-shadow: var(--shadow-md);
  white-space: nowrap;
}

.resume-list-toast--success {
  background: var(--success);
}

.resume-list-toast--error {
  background: var(--danger);
}

.toast-enter-active {
  transition: all 0.25s ease;
}

.toast-leave-active {
  transition: all 0.18s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

/* Version preview panel */
.version-panel {
  width: 900px;
  max-height: 80vh;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.version-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.version-panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.version-panel-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: none;
  cursor: pointer;
  color: var(--text-3);
  transition: all 0.12s;
}
.version-panel-close:hover {
  background: var(--bg-page);
  color: var(--text-1);
}

.version-panel-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.version-panel-list {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid var(--border);
  overflow-y: auto;
  padding: 8px;
}

.version-panel-loading,
.version-panel-empty {
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-3);
}

.version-panel-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.12s;
}
.version-panel-item:hover {
  background: var(--bg-page);
}
.version-panel-item.active {
  background: var(--primary-light);
}

.version-panel-num {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary);
  flex-shrink: 0;
}

.version-panel-time {
  font-size: 11px;
  color: var(--text-3);
  flex-shrink: 0;
}

.version-panel-remark {
  font-size: 12px;
  color: var(--text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-panel-preview {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  justify-content: center;
}

.version-preview-paper {
  width: 794px;
  transform-origin: top center;
  transform: scale(0.9);
}

.version-panel-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-3);
}
.version-panel-placeholder p {
  font-size: 13px;
  margin: 0;
}

.share-config-overlay {
  position: fixed;
  inset: 0;
  z-index: 10003;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.36);
  backdrop-filter: blur(2px);
}

.share-config-panel {
  width: 520px;
  max-width: calc(100vw - 32px);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 26px 70px rgba(15, 23, 42, 0.22);
  overflow: hidden;
}

.share-config-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px 16px;
  border-bottom: 1px solid var(--border);
}

.share-config-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.share-config-header p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.55;
  color: var(--text-3);
}

.share-config-close {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  transition: all 0.15s ease;
}

.share-config-close:hover {
  color: var(--text-1);
  background: var(--bg-page);
}

.share-config-body {
  padding: 18px 22px 8px;
}

.share-config-group {
  margin-bottom: 18px;
}

.share-config-label {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}

.share-password-mode,
.share-expire-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.share-segment-btn,
.share-chip-btn {
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: #fff;
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}

.share-segment-btn:hover,
.share-chip-btn:hover {
  border-color: var(--text-3);
  color: var(--text-1);
}

.share-segment-btn.active,
.share-chip-btn.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.share-password-box,
.share-custom-expire {
  margin-top: 12px;
}

.share-config-input {
  width: 100%;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
  color: var(--text-1);
  font-size: 13px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.share-config-input:focus {
  border-color: rgba(79, 70, 229, 0.42);
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.08);
}

.share-config-input-sm {
  width: 120px;
}

.share-config-hint {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-3);
}

.share-custom-expire {
  display: flex;
  align-items: center;
  gap: 10px;
}

.share-custom-unit {
  font-size: 12px;
  color: var(--text-3);
}

.share-config-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 22px 20px;
  border-top: 1px solid var(--border);
}
</style>
