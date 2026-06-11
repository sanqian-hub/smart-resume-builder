<template>
  <div class="editor-section">
    <div v-for="(item, idx) in list" :key="idx" class="portfolio-block">
      <div v-if="list.length > 1" class="block-header">
        <span class="block-index">个人作品 {{ idx + 1 }}</span>
        <button class="btn-del" @click="removeItem(idx)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          删除
        </button>
      </div>

      <!-- 作品名称 / 作品链接 -->
      <div class="form-grid cols-2">
        <div class="form-item">
          <label>作品名称</label>
          <div class="input-wrap">
            <input v-model="item.name" @input="emitChange" />
            <button v-if="item.name" class="input-clear-btn" @click="item.name = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
        <div class="form-item">
          <label>作品链接</label>
          <div class="input-wrap">
            <input v-model="item.link" @input="emitChange" />
            <button v-if="item.link" class="input-clear-btn" @click="item.link = ''; emitChange()">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 作品详情 -->
      <div class="form-item full" style="margin-top: 4px;">
        <label>作品详情</label>
        <RichTextEditor v-model="item.content" @update:model-value="emitChange" />
      </div>

      <div class="block-divider"></div>
    </div>

    <div v-if="list.length === 0" class="empty-hint">暂无个人作品，点击下方添加</div>
    <button class="btn-add" @click="addItem">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      添加个人作品
    </button>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import RichTextEditor from './RichTextEditor.vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const emptyItem = { name: '', link: '', content: '' }

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

function emitChange() { emit('update:modelValue', JSON.stringify([...list])) }
</script>

<style scoped>
.editor-section { padding: 16px 0; font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif; }

.form-grid { display: grid; gap: 14px; }
.form-grid.cols-2 { grid-template-columns: 1fr 1fr; }
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

.block-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.block-index { font-size: 13px; font-weight: 600; color: var(--text-1); }
.btn-del { display: flex; align-items: center; gap: 4px; padding: 4px 10px; font-size: 12px; color: var(--text-3); border: none; background: none; cursor: pointer; border-radius: 4px; }
.btn-del:hover { color: var(--danger); background: #fef2f2; }

.block-divider { border-bottom: 1px dashed var(--border); margin: 20px 0 16px; }

.btn-add { display: flex; align-items: center; gap: 6px; padding: 7px 16px; font-size: 13px; color: #fff; background: var(--primary); border: none; border-radius: 999px; cursor: pointer; }
.btn-add:hover { opacity: 0.85; }

.empty-hint { text-align: center; padding: 20px; font-size: 13px; color: var(--text-3); background: var(--bg-page); border-radius: 8px; border: 1px dashed var(--border); }
</style>
