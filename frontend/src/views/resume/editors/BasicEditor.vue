<template>
  <div class="editor-section">
    <!-- 姓名 / 电话 / 邮箱 -->
    <div class="form-grid cols-3">
      <div class="form-item">
        <label>姓名</label>
        <div class="input-wrap">
          <input v-model="data.name" @input="emitChange" />
          <button v-if="data.name" class="input-clear-btn" @click="data.name = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
      <div class="form-item">
        <label>电话</label>
        <div class="input-wrap">
          <input v-model="data.phone" @input="emitChange" />
          <button v-if="data.phone" class="input-clear-btn" @click="data.phone = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
      <div class="form-item">
        <label>邮箱</label>
        <div class="input-wrap">
          <input v-model="data.email" @input="emitChange" />
          <button v-if="data.email" class="input-clear-btn" @click="data.email = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 头像 + GitHub -->
    <div class="avatar-github-row">
      <div class="avatar-section">
        <div class="avatar-header">
          <label>头像</label>
          <button class="align-toggle" :class="{ on: data.avatarAlign }" @click="data.avatarAlign = !data.avatarAlign; emitChange()">
            <span class="toggle-thumb" />
          </button>
          <span class="align-label">{{ avatarAlignLabel }}</span>
        </div>
        <div class="avatar-body">
          <div class="avatar-box" @click="triggerUpload">
            <img v-if="data.avatar" :src="data.avatar" class="avatar-img" />
            <div v-else class="avatar-placeholder">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="8" r="5"/>
                <path d="M20 21a8 8 0 0 0-16 0"/>
              </svg>
            </div>
            <div v-if="avatarUploading" class="avatar-uploading-mask">
              <span class="mini-loader"></span>
              <span>上传中...</span>
            </div>
          </div>
          <input ref="fileInput" type="file" accept="image/*" class="hidden-input" :disabled="avatarUploading" @change="handleAvatar" />
          <AvatarCropModal :visible="showCropper" :image-src="rawImageSrc" @confirm="onCropConfirm" @cancel="onCropCancel" />
          <div v-if="data.avatar" class="avatar-actions">
            <button class="avatar-btn btn-del" :disabled="avatarUploading" @click="removeAvatar" title="删除头像">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            </button>
            <button class="avatar-btn btn-refresh" :disabled="avatarUploading" @click="triggerUpload" title="重新上传">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 2v6h-6"/><path d="M3 12a9 9 0 0 1 15-6.7L21 8"/><path d="M3 22v-6h6"/><path d="M21 12a9 9 0 0 1-15 6.7L3 16"/></svg>
            </button>
          </div>
        </div>
      </div>
      <div class="form-item github-field">
        <label>GitHub</label>
        <div class="input-wrap">
          <input v-model="data.github" placeholder="https://github.com/username" @input="emitChange" />
          <button v-if="data.github" class="input-clear-btn" @click="data.github = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 求职意向 -->
    <div class="sub-title">求职意向</div>
    <div class="form-grid cols-3">
      <div class="form-item">
        <label>当前状态</label>
        <div class="custom-select" :class="{ open: openDropdown === 'status' }">
          <div class="select-display" @click="toggleDropdown('status')">
            <span class="select-text" :class="{ placeholder: !data.status }">{{ data.status || '' }}</span>
            <button v-if="data.status" class="select-clear-btn" @click.stop="data.status = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
          <div v-if="openDropdown === 'status'" class="select-options">
            <div
              v-for="s in statusOptions" :key="s"
              class="select-option" :class="{ active: data.status === s }"
              @click="pickOption('status', s)"
            >{{ s }}</div>
          </div>
        </div>
      </div>
      <div class="form-item">
        <label>职位名称</label>
        <div class="input-wrap">
          <input v-model="data.jobTitle" @input="emitChange" />
          <button v-if="data.jobTitle" class="input-clear-btn" @click="data.jobTitle = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
      <div class="form-item">
        <label>期望工作地</label>
        <div class="input-wrap">
          <input v-model="data.location" @input="emitChange" />
          <button v-if="data.location" class="input-clear-btn" @click="data.location = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
    </div>
    <div class="salary-row">
      <div class="form-item">
        <label>期望薪资</label>
        <div class="input-wrap">
          <input v-model="data.salary" @input="emitChange" />
          <button v-if="data.salary" class="input-clear-btn" @click="data.salary = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 其他信息 -->
    <div class="sub-title">其他信息</div>
    <div class="form-grid cols-3">
      <div class="form-item">
        <label>最高学历</label>
        <div class="custom-select" :class="{ open: openDropdown === 'education' }">
          <div class="select-display" @click="toggleDropdown('education')">
            <span class="select-text" :class="{ placeholder: !data.education }">{{ data.education || '' }}</span>
            <button v-if="data.education" class="select-clear-btn" @click.stop="data.education = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
          <div v-if="openDropdown === 'education'" class="select-options">
            <div
              v-for="e in educationOptions" :key="e"
              class="select-option" :class="{ active: data.education === e }"
              @click="pickOption('education', e)"
            >{{ e }}</div>
          </div>
        </div>
      </div>
      <div class="form-item">
        <label>个人网站</label>
        <div class="input-wrap">
          <input v-model="data.website" @input="emitChange" />
          <button v-if="data.website" class="input-clear-btn" @click="data.website = ''; emitChange()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 更多 -->
    <div class="sub-title">更多</div>
    <div class="optional-flow">
      <template v-for="opt in optionalFields" :key="opt.key">
        <div v-if="enabledOptionals.has(opt.key)" class="form-item opt-active">
          <div class="opt-label-row">
            <label>{{ opt.label }}</label>
            <button class="opt-close" @click.stop="toggleOptional(opt.key)" title="移除">
              <svg width="14" height="14" viewBox="64 64 896 896" fill="currentColor"><path d="M799.86 166.31c.02 0 .04.02.08.06l57.69 57.7c.04.03.05.05.06.08a.12.12 0 010 .06c0 .03-.02.05-.06.09L569.93 512l287.7 287.7c.04.04.05.06.06.09a.12.12 0 010 .07c0 .02-.02.04-.06.08l-57.7 57.69c-.03.04-.05.05-.07.06a.12.12 0 01-.07 0c-.03 0-.05-.02-.09-.06L512 569.93l-287.7 287.7c-.04.04-.06.05-.09.06a.12.12 0 01-.07 0c-.02 0-.04-.02-.08-.06l-57.69-57.7c-.04-.03-.05-.05-.06-.07a.12.12 0 010-.07c0-.03.02-.05.06-.09L454.07 512l-287.7-287.7c-.04-.04-.05-.06-.06-.09a.12.12 0 010-.07c0-.02.02-.04.06-.08l57.7-57.69c.03-.04.05-.05.07-.06a.12.12 0 01.07 0c.03 0 .05.02.09.06L512 454.07l287.7-287.7c.04-.04.06-.05.09-.06a.12.12 0 01.07 0z"/></svg>
            </button>
          </div>
          <div v-if="opt.key === 'gender'" class="custom-select" :class="{ open: openDropdown === 'gender' }">
            <div class="select-display" @click="toggleDropdown('gender')">
              <span class="select-text" :class="{ placeholder: !data.gender }">{{ data.gender || '' }}</span>
              <button v-if="data.gender" class="select-clear-btn" @click.stop="data.gender = ''; emitChange()">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
            <div v-if="openDropdown === 'gender'" class="select-options">
              <div v-for="g in genderOptions" :key="g" class="select-option" :class="{ active: data.gender === g }" @click="pickOption('gender', g)">{{ g }}</div>
            </div>
          </div>
          <div v-else class="input-wrap">
            <input v-model="data[opt.key]" @input="emitChange" />
            <button v-if="data[opt.key]" class="input-clear-btn" @click="data[opt.key] = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <button v-else class="tag-btn" @click="toggleOptional(opt.key)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          {{ opt.label }}
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import AvatarCropModal from './AvatarCropModal.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  currentTemplate: { type: String, default: 'classic-1' },
})
const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const openDropdown = ref('')
const showCropper = ref(false)
const rawImageSrc = ref('')
const avatarUploading = ref(false)

const statusOptions = [
  '应届毕业生', '在校', '在职', '实习中',
  '在职-考虑机会', '在职-月内到岗',
  '离职', '离职-随时到岗',
]

const educationOptions = ['初中及以下', '高中/中专', '大专', '本科', '硕士', '博士']

const genderOptions = ['男', '女']

const optionalFields = [
  { key: 'wechat', label: '微信号' },
  { key: 'age', label: '年龄' },
  { key: 'workYears', label: '工作年限' },
  { key: 'gender', label: '性别' },
]

const defaults = {
  name: '', phone: '', email: '', avatar: '', avatarAlign: false,
  status: '', jobTitle: '', location: '', salary: '',
  education: '', website: '',
  themeColor: 'rgb(70, 114, 242)',
  wechat: '', city: '', age: '', workYears: '',
  gender: '', height: '', weight: '', hometown: '', ethnicity: '',
  politics: '', marital: '',
}

const data = reactive({ ...defaults })

const avatarAlignLabel = computed(() => {
  if (
    props.currentTemplate === 'creative-1'
    || props.currentTemplate === 'dual-1'
    || props.currentTemplate === 'dual-2'
  ) {
    return data.avatarAlign ? '右对齐' : '左对齐'
  }
  return data.avatarAlign ? '左对齐' : '右对齐'
})

const enabledOptionals = ref(new Set())

let isFirstLoad = true

watch(() => props.modelValue, (val) => {
  if (val) {
    try {
      const parsed = { ...defaults, ...JSON.parse(val) }
      Object.assign(data, parsed)
      if (isFirstLoad) {
        const active = new Set()
        optionalFields.forEach(f => {
          if (parsed[f.key]) active.add(f.key)
        })
        enabledOptionals.value = active
        isFirstLoad = false
      }
    } catch {}
  }
}, { immediate: true })

function toggleDropdown(key) {
  openDropdown.value = openDropdown.value === key ? '' : key
}

function pickOption(field, value) {
  data[field] = value
  openDropdown.value = ''
  emitChange()
}

function handleClickOutside(e) {
  if (openDropdown.value && !e.target.closest('.custom-select')) {
    openDropdown.value = ''
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onBeforeUnmount(() => document.removeEventListener('click', handleClickOutside))

function triggerUpload() {
  if (avatarUploading.value) return
  fileInput.value?.click()
}

function handleAvatar(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const img = new Image()
  img.onload = () => {
    const MAX = 600
    let w = img.width, h = img.height
    if (w > MAX || h > MAX) {
      if (w > h) { h = Math.round(h * MAX / w); w = MAX }
      else { w = Math.round(w * MAX / h); h = MAX }
    }
    const canvas = document.createElement('canvas')
    canvas.width = w
    canvas.height = h
    canvas.getContext('2d').drawImage(img, 0, 0, w, h)
    rawImageSrc.value = canvas.toDataURL('image/jpeg', 0.92)
    showCropper.value = true
  }
  img.src = URL.createObjectURL(file)
}

async function onCropConfirm(dataUrl) {
  showCropper.value = false
  rawImageSrc.value = ''
  avatarUploading.value = true
  try {
    // Keep the cropped avatar inline in resume content so preview/share/PDF export
    // all render the same asset without depending on cross-origin image capture.
    data.avatar = dataUrl
    emitChange()
  } catch {
    // keep previous avatar on upload failure
  } finally {
    avatarUploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function onCropCancel() {
  showCropper.value = false
  rawImageSrc.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

function removeAvatar() { data.avatar = ''; emitChange() }

function toggleOptional(key) {
  if (enabledOptionals.value.has(key)) {
    data[key] = ''
    enabledOptionals.value.delete(key)
  } else {
    enabledOptionals.value.add(key)
  }
  enabledOptionals.value = new Set(enabledOptionals.value)
  emitChange()
}

function emitChange() { emit('update:modelValue', JSON.stringify({ ...data })) }
</script>

<style scoped>
.editor-section { padding: 16px 0; font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif; }

.form-grid { display: grid; gap: 10px; }
.form-grid.cols-3 { grid-template-columns: 1fr 1fr 1fr; }
.form-grid.cols-2 { grid-template-columns: 1fr 1fr; }
.form-grid.cols-1 { grid-template-columns: 1fr; }
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item label { font-size: 13px; color: var(--text-2); }
.form-item input {
  padding: 7px 10px; font-size: 13px;
  height: 34px;
  border: 1px solid var(--border); border-radius: 6px;
  color: var(--text-1); background: #fff; transition: var(--transition);
  box-sizing: border-box;
  font-family: 'Noto Sans SC', 'Source Han Sans SC', sans-serif;
}
.form-item input:focus {
  outline: none; border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
.input-wrap {
  position: relative;
}
.input-wrap input {
  width: 100%;
  padding-right: 28px;
}
.input-clear-btn {
  position: absolute; right: 4px; top: 50%; transform: translateY(-50%);
  width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; color: var(--text-3);
  border-radius: 4px; flex-shrink: 0;
}
.input-clear-btn:hover { color: var(--danger); background: #fef2f2; }

/* 自定义下拉 */
.custom-select { position: relative; }
.select-display {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 10px; font-size: 13px; height: 34px;
  position: relative;
  border: 1px solid var(--border); border-radius: 6px;
  background: #fff; cursor: pointer; transition: var(--transition);
  box-sizing: border-box;
}
.custom-select.open .select-display {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
.select-text { color: var(--text-1); }
.select-text.placeholder { color: var(--text-3); }

.select-options {
  position: absolute; top: calc(100% + 4px); left: 0; right: 0;
  background: #fff; border: 1px solid var(--border); border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 50;
  max-height: 200px; overflow-y: auto;
  scrollbar-width: none;
}
.select-options::-webkit-scrollbar { display: none; }

.select-option {
  padding: 8px 10px; font-size: 13px; color: var(--text-1);
  cursor: pointer; transition: background 0.1s ease;
}
.select-option:hover { background: var(--bg-page); }
.select-option.active { color: var(--primary); font-weight: 500; }
.select-clear-btn {
  width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; color: var(--text-3);
  border-radius: 4px; flex-shrink: 0; margin-left: 4px;
}
.select-clear-btn:hover { color: var(--danger); background: #fef2f2; }

/* 头像 + GitHub 同行 */
.avatar-github-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-top: 16px;
}
.avatar-github-row .avatar-section { margin-top: 0; }
.github-field {
  width: calc((100% - 20px) / 3);
  margin-left: auto;
  flex-shrink: 0;
}

/* 头像 */
.avatar-section { }
.avatar-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
}
.avatar-header label { font-size: 13px; color: var(--text-2); }
.align-label { font-size: 12px; color: var(--text-3); }

.align-toggle {
  width: 36px; height: 20px; border-radius: 10px;
  border: none; background: var(--border); cursor: pointer;
  position: relative; transition: background 0.2s ease; padding: 0;
}
.align-toggle.on { background: var(--primary); }
.toggle-thumb {
  position: absolute; top: 2px; left: 2px;
  width: 16px; height: 16px; border-radius: 50%;
  background: #fff; box-shadow: 0 1px 2px rgba(0,0,0,0.15);
  transition: transform 0.2s ease;
}
.align-toggle.on .toggle-thumb { transform: translateX(16px); }

.avatar-body { display: flex; align-items: flex-start; gap: 12px; }
.avatar-box {
  width: 80px; height: 94px; border-radius: 8px;
  overflow: hidden; cursor: pointer; flex-shrink: 0;
  border: 1px dashed var(--border); transition: border-color 0.15s ease;
}
.avatar-box:hover { border-color: var(--primary); }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-3); background: var(--bg-page);
}
.avatar-uploading-mask {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.78);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-2);
  font-size: 12px;
  font-weight: 500;
}
.hidden-input { display: none; }

.avatar-actions {
  display: flex; flex-direction: column; gap: 4px; padding-top: 8px;
}
.avatar-btn {
  width: 28px; height: 28px; display: flex;
  align-items: center; justify-content: center;
  border: 1px solid var(--border); border-radius: 4px;
  background: #fff; cursor: pointer; color: var(--text-3);
  transition: all 0.15s ease;
}
.avatar-btn:hover { color: var(--text-1); border-color: var(--text-3); }
.avatar-btn.btn-del:hover { color: var(--danger); border-color: var(--danger); background: #fef2f2; }
.avatar-btn.btn-refresh:hover { color: var(--primary); border-color: var(--primary); }
.avatar-btn:disabled { cursor: default; opacity: 0.6; }

.mini-loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(148, 163, 184, 0.35);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.salary-row { max-width: 33.33%; margin-top: 8px; }

/* 分节标题 */
.sub-title {
  font-size: 14px; font-weight: 700; color: var(--text-1);
  margin: 18px 0 10px;
}

/* 可选字段流式布局 */
.optional-flow {
  display: flex; flex-wrap: wrap; gap: 10px; align-items: flex-end;
}
.opt-active {
  flex: 0 0 calc(33.33% - 7px);
  max-width: calc(33.33% - 7px);
}
.opt-label-row {
  display: flex; align-items: center; justify-content: space-between;
}
.opt-close {
  width: 22px; height: 22px; display: flex;
  align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer;
  color: var(--text-3); border-radius: 4px; padding: 0;
}
.opt-close:hover { color: var(--danger); background: #fef2f2; }

.tag-btn {
  display: flex; align-items: center; gap: 4px;
  padding: 5px 12px; font-size: 13px;
  height: 34px;
  color: var(--text-3); background: #fff;
  border: 1px solid var(--border); border-radius: 6px;
  cursor: pointer; transition: all 0.15s ease;
  white-space: nowrap;
}
.tag-btn:hover {
  color: var(--primary); border-color: var(--primary);
}
</style>
