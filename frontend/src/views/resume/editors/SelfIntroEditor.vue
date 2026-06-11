<template>
  <div class="editor-section">
    <div class="form-item full">
      <label>个人简介</label>
      <RichTextEditor v-model="text" @update:model-value="emitChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import RichTextEditor from './RichTextEditor.vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const text = ref('')

watch(() => props.modelValue, (val) => {
  if (val) {
    try { text.value = JSON.parse(val).content || '' } catch { text.value = '' }
  }
}, { immediate: true })

function emitChange() { emit('update:modelValue', JSON.stringify({ content: text.value })) }
</script>

<style scoped>
.editor-section { padding: 16px 0; font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif; }
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item.full { }
.form-item label { font-size: 13px; color: var(--text-2); }
</style>
