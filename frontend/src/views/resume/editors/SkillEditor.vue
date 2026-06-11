<template>
  <div class="editor-section">
    <div class="form-item full">
      <label>技能描述</label>
      <RichTextEditor v-model="content" @update:model-value="emitChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import RichTextEditor from './RichTextEditor.vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const content = ref('')

watch(() => props.modelValue, (val) => {
  if (val) {
    try {
      const arr = JSON.parse(val)
      if (Array.isArray(arr) && arr.length) {
        content.value = arr.map(i => i.content || '').filter(Boolean).join('')
      } else if (typeof val === 'string') {
        content.value = val
      }
    } catch {
      content.value = val
    }
  }
}, { immediate: true })

function emitChange() {
  emit('update:modelValue', JSON.stringify([{ name: '专业技能', content: content.value }]))
}
</script>

<style scoped>
.editor-section { padding: 16px 0; font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif; }

.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item.full { grid-column: 1 / -1; }
.form-item label { font-size: 13px; color: var(--text-2); }
</style>
