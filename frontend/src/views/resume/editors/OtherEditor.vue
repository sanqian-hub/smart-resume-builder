<template>
  <div class="editor-section">
    <div v-for="(item, idx) in list" :key="idx" class="other-block">
      <div v-if="list.length > 1" class="block-header">
        <span class="block-index">其他经历 {{ idx + 1 }}</span>
        <button class="btn-del" @click="removeItem(idx)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          删除
        </button>
      </div>

      <!-- 经历名称 / 经历时间 -->
      <div class="form-grid cols-3-2">
        <div class="form-item">
          <label>经历名称</label>
          <div class="input-wrap">
            <input v-model="item.name" @input="emitChange" />
            <button v-if="item.name" class="input-clear-btn" @click="item.name = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <div class="form-item">
          <label>经历时间</label>
          <div class="date-range">
            <div class="custom-select" :class="{ open: openDropdown === sdKey(idx) }">
              <div class="select-display" @click="toggleDropdown(sdKey(idx), item.startDate)">
                <span class="select-text" :class="{ placeholder: !item.startDate }">{{ item.startDate || '' }}</span>
                <button v-if="item.startDate" class="select-clear-btn" @click.stop="item.startDate = ''; openDropdown = ''; emitChange()">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
                </button>
              </div>
              <div v-if="openDropdown === sdKey(idx)" class="select-options date-options">
                <div class="date-picker-panel">
                  <div class="dp-nav">
                    <button class="dp-nav-btn" @click.stop="changeYear(sdKey(idx), -1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M15 18l-6-6 6-6"/></svg></button>
                    <span class="dp-year">{{ pickerYear[sdKey(idx)] }}</span>
                    <button class="dp-nav-btn" @click.stop="changeYear(sdKey(idx), 1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg></button>
                  </div>
                  <div class="dp-months">
                    <div v-for="m in months" :key="m" class="dp-month" :class="{ active: item.startDate === fmtDate(sdKey(idx), m) }" @click="pickDate(item, 'startDate', sdKey(idx), m)">{{ m }}月</div>
                  </div>
                </div>
              </div>
            </div>
            <span class="date-sep">-</span>
            <div class="custom-select" :class="{ open: openDropdown === edKey(idx) }">
              <div class="select-display" @click="toggleDropdown(edKey(idx), item.endDate)">
                <span class="select-text" :class="{ placeholder: !item.endDate }">{{ item.endDate || '' }}</span>
                <button v-if="item.endDate" class="select-clear-btn" @click.stop="item.endDate = ''; openDropdown = ''; emitChange()">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
                </button>
              </div>
              <div v-if="openDropdown === edKey(idx)" class="select-options date-options">
                <div class="date-picker-panel">
                  <div class="dp-nav">
                    <button class="dp-nav-btn" @click.stop="changeYear(edKey(idx), -1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M15 18l-6-6 6-6"/></svg></button>
                    <span class="dp-year">{{ pickerYear[edKey(idx)] }}</span>
                    <button class="dp-nav-btn" @click.stop="changeYear(edKey(idx), 1)"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg></button>
                  </div>
                  <div class="dp-months">
                    <div v-for="m in months" :key="m" class="dp-month" :class="{ active: item.endDate === fmtDate(edKey(idx), m) }" @click="pickDate(item, 'endDate', edKey(idx), m)">{{ m }}月</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 所属角色 / 所属部门 / 所在城市 -->
      <div class="form-grid cols-3" style="margin-top: 4px;">
        <div class="form-item">
          <label>所属角色</label>
          <div class="input-wrap">
            <input v-model="item.role" @input="emitChange" />
            <button v-if="item.role" class="input-clear-btn" @click="item.role = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <div class="form-item">
          <label>所属部门</label>
          <div class="input-wrap">
            <input v-model="item.department" @input="emitChange" />
            <button v-if="item.department" class="input-clear-btn" @click="item.department = ''; emitChange()">
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

      <!-- 经历详情 -->
      <div class="form-item full" style="margin-top: 4px;">
        <label>经历详情</label>
        <RichTextEditor v-model="item.content" @update:model-value="emitChange" />
      </div>

      <div class="block-divider"></div>
    </div>

    <div v-if="list.length === 0" class="empty-hint">暂无其他经历，点击下方添加</div>
    <button class="btn-add" @click="addItem">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      添加其他经历
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

const emptyItem = {
  name: '', role: '', department: '', city: '',
  startDate: '', endDate: '', content: '',
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

function addItem() { list.push({ ...emptyItem }); emitChange() }
function removeItem(idx) { list.splice(idx, 1); emitChange() }

function sdKey(idx) { return 'osd-' + idx }
function edKey(idx) { return 'oed-' + idx }
function getYearFromDate(value) {
  const year = Number.parseInt(String(value || '').slice(0, 4), 10)
  return Number.isFinite(year) ? year : currentYear
}
function fmtDate(key, m) { return (pickerYear[key] || currentYear) + '/' + String(m).padStart(2, '0') }
function pickDate(item, field, key, m) { item[field] = fmtDate(key, m); openDropdown.value = ''; emitChange() }

function toggleDropdown(key, value) {
  if (openDropdown.value !== key && !(key in pickerYear)) {
    pickerYear[key] = getYearFromDate(value)
  }
  openDropdown.value = openDropdown.value === key ? '' : key
}
function changeYear(key, dir) { pickerYear[key] = (pickerYear[key] || currentYear) + dir }

function handleClickOutside(e) {
  if (openDropdown.value && !e.target.closest('.custom-select')) openDropdown.value = ''
}
onMounted(() => document.addEventListener('click', handleClickOutside))
onBeforeUnmount(() => document.removeEventListener('click', handleClickOutside))

function emitChange() { emit('update:modelValue', JSON.stringify([...list])) }
</script>

<style scoped>
.editor-section { padding: 16px 0; font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif; }

.form-grid { display: grid; gap: 14px; }
.form-grid.cols-3-2 { grid-template-columns: 1fr 2fr; }
.form-grid.cols-3 { grid-template-columns: 1fr 1fr 1fr; }
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item.full { grid-column: 1 / -1; }
.form-item label { font-size: 13px; color: var(--text-2); }
.form-item input {
  padding: 7px 10px; font-size: 13px; height: 34px;
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

.custom-select { position: relative; }
.select-display {
  display: flex; align-items: center; padding: 0 10px; font-size: 13px; height: 34px;
  position: relative;
  border: 1px solid var(--border); border-radius: 6px;
  background: #fff; cursor: pointer; transition: var(--transition); box-sizing: border-box;
}
.custom-select.open .select-display {
  border-color: var(--primary); box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
.select-text { color: var(--text-1); }
.select-text.placeholder { color: var(--text-3); }
.select-options {
  position: absolute; top: calc(100% + 4px); left: 0; right: 0;
  background: #fff; border: 1px solid var(--border); border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 50;
  max-height: 200px; overflow-y: auto; scrollbar-width: none;
}
.select-options::-webkit-scrollbar { display: none; }
.select-clear-btn {
  width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; color: var(--text-3);
  border-radius: 4px; flex-shrink: 0; margin-left: 4px;
}
.select-clear-btn:hover { color: var(--danger); background: #fef2f2; }

.date-range { display: flex; align-items: center; height: 34px; }
.date-range .custom-select { flex: 1; min-width: 0; }
.date-sep { display: inline-block; width: 24px; text-align: center; font-size: 13px; color: var(--text-3); flex-shrink: 0; }
.date-options { width: 280px; padding: 0; overflow: visible; }
.date-picker-panel { padding: 8px; }
.dp-nav { display: flex; align-items: center; justify-content: space-between; padding: 0 0 8px; border-bottom: 1px solid var(--border); margin-bottom: 8px; }
.dp-year { font-size: 14px; font-weight: 600; color: var(--text-1); }
.dp-nav-btn { width: 26px; height: 26px; display: flex; align-items: center; justify-content: center; border: none; background: none; cursor: pointer; color: var(--text-3); border-radius: 4px; }
.dp-nav-btn:hover { color: var(--text-1); background: var(--bg-page); }
.dp-months { display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px; }
.dp-month { text-align: center; padding: 6px 0; font-size: 13px; color: var(--text-1); border-radius: 4px; cursor: pointer; }
.dp-month:hover { background: var(--bg-page); }
.dp-month.active { color: #fff; background: var(--primary); font-weight: 500; }

.block-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.block-index { font-size: 13px; font-weight: 600; color: var(--text-1); }
.btn-del { display: flex; align-items: center; gap: 4px; padding: 4px 10px; font-size: 12px; color: var(--text-3); border: none; background: none; cursor: pointer; border-radius: 4px; }
.btn-del:hover { color: var(--danger); background: #fef2f2; }

.block-divider { border-bottom: 1px dashed var(--border); margin: 20px 0 16px; }

.btn-add { display: flex; align-items: center; gap: 6px; padding: 7px 16px; font-size: 13px; color: #fff; background: var(--primary); border: none; border-radius: 999px; cursor: pointer; }
.btn-add:hover { opacity: 0.85; }

.empty-hint { text-align: center; padding: 20px; font-size: 13px; color: var(--text-3); background: var(--bg-page); border-radius: 8px; border: 1px dashed var(--border); }
</style>
