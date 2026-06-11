<template>
  <Teleport to="body">
    <div v-if="visible" class="crop-overlay" @mousedown.self="$emit('cancel')">
      <div class="crop-dialog">
        <div class="crop-header">
          <span class="crop-title">裁剪头像</span>
          <button class="crop-close-btn" @click="$emit('cancel')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="crop-body">
          <Cropper
            ref="cropperRef"
            class="cropper-area"
            :src="imageSrc"
            :stencil-props="{ aspectRatio: 80 / 94, movable: true, resizable: true }"
            :default-size="defaultSize"
            :resize-image="{ adjustStencil: true }"
            image-restriction="fit-area"
          />
        </div>
        <div class="crop-footer">
          <button class="crop-btn crop-btn-cancel" @click="$emit('cancel')">取消</button>
          <button class="crop-btn crop-btn-confirm" @click="handleConfirm">确认裁剪</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { Cropper } from 'vue-advanced-cropper'
import 'vue-advanced-cropper/dist/style.css'

const props = defineProps({
  visible: { type: Boolean, default: false },
  imageSrc: { type: String, required: true },
})
const emit = defineEmits(['confirm', 'cancel'])

const cropperRef = ref(null)

function defaultSize() {
  return { width: 360, height: 423 }
}

function handleConfirm() {
  const { canvas } = cropperRef.value.getResult()
  const output = document.createElement('canvas')
  output.width = 160
  output.height = 188
  const ctx = output.getContext('2d')
  ctx.drawImage(canvas, 0, 0, 160, 188)
  emit('confirm', output.toDataURL('image/jpeg', 0.92))
}
</script>

<style scoped>
.crop-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
}
.crop-dialog {
  width: 520px;
  max-width: 92vw;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.crop-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  flex-shrink: 0;
}
.crop-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1, #1a1a1a);
}
.crop-close-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-3, #999);
  border-radius: 6px;
  transition: all 0.15s ease;
}
.crop-close-btn:hover {
  color: var(--text-1, #1a1a1a);
  background: rgba(0, 0, 0, 0.06);
}
.crop-body {
  padding: 16px;
  background: #f5f5f5;
  flex: 1;
}
.cropper-area {
  width: 100%;
  height: 380px;
  border-radius: 8px;
  overflow: hidden;
  background: #e0e0e0;
}
.crop-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 18px;
  border-top: 1px solid var(--border, #e5e7eb);
  flex-shrink: 0;
}
.crop-btn {
  padding: 8px 22px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
}
.crop-btn-cancel {
  color: var(--text-2, #666);
  background: #fff;
  border: 1px solid var(--border, #e5e7eb);
}
.crop-btn-cancel:hover {
  color: var(--text-1, #1a1a1a);
  background: var(--bg-page, #f9fafb);
}
.crop-btn-confirm {
  color: #fff;
  background: var(--primary, #4f46e5);
  border: 1px solid var(--primary, #4f46e5);
}
.crop-btn-confirm:hover {
  opacity: 0.85;
}
</style>

<style>
.crop-dialog .vue-advanced-cropper__background {
  background: rgba(0, 0, 0, 0.45) !important;
}
.crop-dialog .vue-advanced-cropper__foreground {
  background: rgba(0, 0, 0, 0.45) !important;
}
</style>
