<template>
  <div class="editor-section">
    <div v-for="(item, idx) in list" :key="idx" class="edu-block">
      <div v-if="list.length > 1" class="block-header">
        <span class="block-index">教育经历 {{ idx + 1 }}</span>
        <button class="btn-del" @click="removeItem(idx)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          删除
        </button>
      </div>
      <!-- 学校 / 专业 / 学历 -->
      <div class="form-grid cols-3">
        <div class="form-item">
          <label>学校</label>
          <div class="input-wrap">
            <input v-model="item.school" @input="emitChange" />
            <button v-if="item.school" class="input-clear-btn" @click="item.school = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <div class="form-item">
          <label>专业</label>
          <div class="input-wrap">
            <input v-model="item.major" @input="emitChange" />
            <button v-if="item.major" class="input-clear-btn" @click="item.major = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <div class="form-item">
          <label>学历</label>
          <div class="custom-select" :class="{ open: openDropdown === 'edu-' + idx }">
            <div class="select-display" @click="toggleDropdown('edu-' + idx)">
              <span class="select-text" :class="{ placeholder: !item.degree }">{{ item.degree || '' }}</span>
              <button v-if="item.degree" class="select-clear-btn" @click.stop="item.degree = ''; emitChange()">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
            <div v-if="openDropdown === 'edu-' + idx" class="select-options">
              <div
                v-for="d in degreeOptions" :key="d"
                class="select-option" :class="{ active: item.degree === d }"
                @click="pickField(item, 'degree', d, 'edu-' + idx)"
              >{{ d }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 在读时间 -->
      <div class="form-grid cols-3" style="margin-top: 4px;">
        <div class="form-item">
          <label>在读时间</label>
          <div class="custom-select" :class="{ open: openDropdown === drKey(idx) }">
            <div class="select-display" @click="toggleDropdown(drKey(idx), item)">
              <span class="select-text" :class="{ placeholder: !item.startDate && !item.endDate }">
                {{ item.startDate ? (item.startDate + (item.endDate ? ' - ' + item.endDate : '')) : (item.endDate ? item.endDate : '') }}
              </span>
              <button v-if="item.startDate || item.endDate" class="date-clear-btn" title="清除时间" @click.stop="clearDate(item)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
            <div v-if="openDropdown === drKey(idx)" class="select-options date-options" @click.stop>
              <div class="dp-section">
                <div class="dp-label">开始</div>
                <div class="date-picker-panel">
                  <div class="dp-nav">
                    <button class="dp-nav-btn" @click.stop="changeYear(drKey(idx) + '-s', -1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M15 18l-6-6 6-6"/></svg></button>
                    <span class="dp-year">{{ pickerYear[drKey(idx) + '-s'] }}</span>
                    <button class="dp-nav-btn" @click.stop="changeYear(drKey(idx) + '-s', 1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg></button>
                  </div>
                  <div class="dp-months">
                    <div v-for="m in months" :key="m" class="dp-month" :class="{ active: item.startDate === fmtDate(drKey(idx) + '-s', m) }" @click="pickDate(item, 'startDate', drKey(idx) + '-s', m)">{{ m }}月</div>
                  </div>
                </div>
              </div>
              <div class="dp-divider"></div>
              <div class="dp-section">
                <div class="dp-label">结束</div>
                <div class="date-picker-panel">
                  <div class="dp-nav">
                    <button class="dp-nav-btn" @click.stop="changeYear(drKey(idx) + '-e', -1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M15 18l-6-6 6-6"/></svg></button>
                    <span class="dp-year">{{ pickerYear[drKey(idx) + '-e'] }}</span>
                    <button class="dp-nav-btn" @click.stop="changeYear(drKey(idx) + '-e', 1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg></button>
                  </div>
                  <div class="dp-months">
                    <div v-for="m in months" :key="m" class="dp-month" :class="{ active: item.endDate === fmtDate(drKey(idx) + '-e', m) }" @click="pickDate(item, 'endDate', drKey(idx) + '-e', m)">{{ m }}月</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 学历类型 / 学院 / 所在城市 -->
      <div class="form-grid cols-3">
        <div class="form-item">
          <label>学历类型</label>
          <div class="custom-select" :class="{ open: openDropdown === 'dtype-' + idx }">
            <div class="select-display" @click="toggleDropdown('dtype-' + idx)">
              <span class="select-text" :class="{ placeholder: !item.degreeType }">{{ item.degreeType || '' }}</span>
              <button v-if="item.degreeType" class="select-clear-btn" @click.stop="item.degreeType = ''; emitChange()">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
            <div v-if="openDropdown === 'dtype-' + idx" class="select-options">
              <div
                v-for="t in degreeTypeOptions" :key="t"
                class="select-option" :class="{ active: item.degreeType === t }"
                @click="pickField(item, 'degreeType', t, 'dtype-' + idx)"
              >{{ t }}</div>
            </div>
          </div>
        </div>
        <div class="form-item">
          <label>学院</label>
          <div class="input-wrap">
            <input v-model="item.academy" @input="emitChange" />
            <button v-if="item.academy" class="input-clear-btn" @click="item.academy = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <div class="form-item">
          <label>所在城市</label>
          <div class="input-wrap">
            <input v-model="item.city" @input="emitChange" />
            <button v-if="item.city" class="input-clear-btn" @click="item.city = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 在校经历 -->
      <div class="form-item full">
        <label>在校经历</label>
        <RichTextEditor v-model="item.description" @update:model-value="emitChange" />
      </div>

      <div class="block-divider"></div>
    </div>

    <div v-if="list.length === 0" class="empty-hint">暂无教育经历，点击下方添加</div>
    <button class="btn-add" @click="addItem">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      添加教育经历
    </button>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import RichTextEditor from './RichTextEditor.vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const openDropdown = ref('')
const months = Array.from({ length: 12 }, (_, i) => i + 1)
const currentYear = new Date().getFullYear()
const pickerYear = reactive({})

function getPickerYear(key) {
  if (!(key in pickerYear)) {
    const match = (props.modelValue || '').match(new RegExp(`"${key === 'sd' ? 'startDate' : 'endDate'}":"(\\d{4})"`))
    pickerYear[key] = match ? parseInt(match[1]) : currentYear
  }
  return pickerYear[key]
}

function changeYear(key, dir) {
  pickerYear[key] = (pickerYear[key] || currentYear) + dir
}

const degreeOptions = ['大专', '本科', '硕士', '博士']
const degreeTypeOptions = ['全日制', '非全日制', '自考', '成人教育', '网络教育', '专升本']

const emptyItem = {
  school: '', major: '', degree: '', startDate: '', endDate: '',
  degreeType: '', academy: '', city: '', description: '',
}

const list = reactive([{ ...emptyItem }])

watch(() => props.modelValue, (val) => {
  if (val) {
    try {
      const arr = JSON.parse(val)
      if (arr.length) {
        list.splice(0, list.length, ...arr.map(i => ({ ...emptyItem, ...i })))
      }
    } catch {}
  }
}, { immediate: true })

function addItem() {
  list.push({ ...emptyItem })
  emitChange()
}

function removeItem(idx) {
  list.splice(idx, 1)
  emitChange()
}

function getYearFromDate(value) {
  const year = Number.parseInt(String(value || '').slice(0, 4), 10)
  return Number.isFinite(year) ? year : currentYear
}

function toggleDropdown(key, item) {
  if (openDropdown.value !== key) {
    const startKey = key + '-s'
    const endKey = key + '-e'
    if (!(startKey in pickerYear)) pickerYear[startKey] = getYearFromDate(item?.startDate)
    if (!(endKey in pickerYear)) pickerYear[endKey] = getYearFromDate(item?.endDate)
  }
  openDropdown.value = openDropdown.value === key ? '' : key
}

function pickField(item, field, value, dropdownKey) {
  item[field] = value
  openDropdown.value = ''
  emitChange()
}

function sdKey(idx) { return 'sd-' + idx }
function edKey(idx) { return 'ed-' + idx }
function drKey(idx) { return 'edr-' + idx }
function clearDate(item) { item.startDate = ''; item.endDate = ''; emitChange() }
function fmtDate(key, month) {
  return (pickerYear[key] || currentYear) + '/' + String(month).padStart(2, '0')
}
function pickDate(item, field, key, month) {
  item[field] = fmtDate(key, month)
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

function emitChange() { emit('update:modelValue', JSON.stringify([...list])) }
</script>

<style scoped>
.editor-section { padding: 16px 0; font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif; }

.form-grid { display: grid; gap: 14px; }
.form-grid.cols-3 { grid-template-columns: 1fr 1fr 1fr; }
.form-grid + .form-grid { margin-top: 4px; }
.form-grid.cols-2-1 { grid-template-columns: 2fr 1fr; margin-top: 4px; }
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item.full { grid-column: 1 / -1; margin-top: 4px; }
.form-item label { font-size: 13px; color: var(--text-2); }
.form-item input {
  padding: 7px 10px; font-size: 13px;
  height: 34px;
  border: 1px solid var(--border); border-radius: 6px;
  color: var(--text-1); background: #fff; transition: var(--transition);
  box-sizing: border-box;
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
.form-item textarea {
  padding: 8px 10px; font-size: 13px;
  border: 1px solid var(--border); border-radius: 6px;
  color: var(--text-1); background: #fff; transition: var(--transition);
  resize: vertical; line-height: 1.7;
}
.form-item textarea:focus {
  outline: none; border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}

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

/* 日期范围 */
.date-clear-btn {
  width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; color: var(--text-3);
  border-radius: 4px; flex-shrink: 0;
}
.date-clear-btn:hover { color: var(--danger); background: #fef2f2; }
.date-options { width: auto; min-width: 320px; padding: 8px; overflow: visible; display: flex; gap: 0; }
.dp-section { flex: 1; }
.dp-label { font-size: 12px; font-weight: 600; color: var(--text-2); margin-bottom: 6px; }
.dp-divider { width: 1px; background: var(--border); margin: 0 8px; flex-shrink: 0; }
.date-picker-panel { padding: 8px; }
.dp-nav {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 0 8px; border-bottom: 1px solid var(--border);
  margin-bottom: 8px;
}
.dp-year { font-size: 14px; font-weight: 600; color: var(--text-1); }
.dp-nav-btn {
  width: 26px; height: 26px; display: flex;
  align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer;
  color: var(--text-3); border-radius: 4px; transition: all 0.1s ease;
}
.dp-nav-btn:hover { color: var(--text-1); background: var(--bg-page); }
.dp-months {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px;
}
.dp-month {
  text-align: center; padding: 6px 0; font-size: 13px;
  color: var(--text-1); border-radius: 4px; cursor: pointer;
  transition: all 0.1s ease; white-space: nowrap;
}
.dp-month:hover { background: var(--bg-page); }
.dp-month.active {
  color: #fff; background: var(--primary); font-weight: 500;
}

/* 分割线 */
.block-divider {
  border-bottom: 1px dashed var(--border);
  margin: 20px 0 16px;
}

/* 添加按钮 */
.btn-add {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 16px; font-size: 13px;
  color: #fff; background: var(--primary);
  border: none; border-radius: 999px;
  cursor: pointer; transition: all 0.15s ease;
}
.btn-add:hover { opacity: 0.85; }

.block-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 10px;
}
.block-index { font-size: 13px; font-weight: 600; color: var(--text-1); }
.btn-del {
  display: flex; align-items: center; gap: 4px;
  padding: 4px 10px; font-size: 12px; color: var(--text-3);
  border: none; background: none; cursor: pointer;
  border-radius: 4px; transition: all 0.15s ease;
}
.btn-del:hover { color: var(--danger); background: #fef2f2; }

.empty-hint {
  text-align: center; padding: 20px; font-size: 13px; color: var(--text-3);
  background: var(--bg-page); border-radius: 8px; border: 1px dashed var(--border);
}
</style>
