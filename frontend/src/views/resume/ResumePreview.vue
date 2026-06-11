<template>
  <div class="resume-preview-page">
    <div class="preview-toolbar">
      <button class="btn-back" @click="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M19 12H5"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        返回编辑
      </button>
      <div class="toolbar-title">
        <span>简历预览</span>
      </div>
      <div class="toolbar-actions">
        <button v-if="resume" class="btn-edit" @click="$router.push(`/resume/edit/${resume.id}`)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          编辑
        </button>
      </div>
    </div>

    <div v-if="resume" class="preview-container">
      <div ref="scrollRef" class="preview-scroll">
        <div ref="paperRef" class="paper">
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
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { getResume } from '../../api/resume'
import { readStyleConfig } from './styleConfig'
import { DEFAULT_TEMPLATE_ID, normalizeTemplateId, TEMPLATE_COMPONENTS } from './templateRegistry'

const route = useRoute()
const resume = ref(null)
const scrollRef = ref(null)
const paperRef = ref(null)
const A4_W = 794
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

function updateScale() {
  if (!scrollRef.value || !paperRef.value) return
  const containerW = scrollRef.value.offsetWidth
  const scale = Math.min(1, containerW / A4_W)
  paperRef.value.style.transform = `scale(${scale})`
  paperRef.value.style.transformOrigin = 'top left'
  scrollRef.value.style.height = Math.ceil(paperRef.value.offsetHeight * scale) + 'px'
}

onMounted(async () => {
  resume.value = await getResume(route.params.id)
  await new Promise(r => setTimeout(r, 100))
  updateScale()
  if (scrollRef.value) {
    resizeObserver = new ResizeObserver(() => updateScale())
    resizeObserver.observe(scrollRef.value)
    if (paperRef.value) {
      resizeObserver.observe(paperRef.value)
    }
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.resume-preview-page {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 56px);
}

.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  margin-bottom: 16px;
  flex-shrink: 0;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 14px;
  color: var(--text-2);
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg-card);
  cursor: pointer;
  transition: var(--transition);
}
.btn-back:hover { color: var(--text-1); background: var(--bg-page); }

.toolbar-title {
  flex: 1;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1);
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 6px;
  background: var(--bg-card);
  cursor: pointer;
  transition: var(--transition);
}
.btn-edit:hover { background: var(--primary-light); }

.preview-container {
  flex: 1;
  overflow-y: auto;
  display: flex;
  justify-content: center;
  background: var(--bg-page);
  padding: 0 24px 24px;
}

@media (max-width: 900px) {
  .preview-container {
    padding: 0 12px 12px;
  }
  .preview-toolbar {
    padding: 6px 12px;
    margin-bottom: 8px;
  }
}

.preview-scroll {
  width: 100%;
  max-width: 794px;
  margin: 0 auto;
  overflow: hidden;
}

.paper {
  width: 794px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  border-radius: 2px;
  min-height: 1123px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}

</style>
