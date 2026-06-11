<template>
  <div v-if="visible" class="template-select-backdrop" @click="emit('close')">
    <div class="theme-select-menu template-select-menu" @click.stop>
      <div class="template-select-head">
        <div class="template-select-title-block">
          <div class="template-select-title">选择模板</div>
          <div class="template-select-subtitle">预览不同模板的整体排版效果与视觉气质</div>
        </div>
        <button class="template-select-close" type="button" @click="emit('close')" aria-label="关闭模板选择">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M18 6L6 18" />
            <path d="M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="template-select-body">
        <div class="template-select-tabs template-select-tabs--compact" role="tablist" aria-label="模板分类">
          <button
            v-for="tab in familyTabs"
            :key="tab.key"
            class="template-select-tab template-select-tab--action"
            :class="{ on: activeFamilyTab === tab.key }"
            role="tab"
            type="button"
            :aria-selected="activeFamilyTab === tab.key"
            @click="activeFamilyTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="activeFamilyTab === 'all'" class="template-select-groups">
          <section v-for="group in allGroups" :key="group.family" class="template-group">
            <div class="template-group-head">
              <div class="template-group-meta">
                <h3 class="template-group-title">{{ group.label }}</h3>
                <span class="template-group-dot" aria-hidden="true"></span>
                <span class="template-group-count">{{ group.templates.length }} 套</span>
              </div>
            </div>
            <div class="template-select-grid">
              <div
                v-for="option in group.templates"
                :key="option.id"
                class="template-option-card"
                :class="{ active: selectedTemplate === option.id }"
                role="button"
                tabindex="0"
                :aria-label="option.label"
                @click="handleSelect(option.id)"
                @keydown.enter.prevent="handleSelect(option.id)"
                @keydown.space.prevent="handleSelect(option.id)"
              >
                <div class="template-option-preview" :ref="setPreviewRef">
                  <div class="template-option-scale" :style="{ transform: `scale(${previewScale})` }">
                    <component
                      :is="option.component || TEMPLATE_COMPONENTS[DEFAULT_TEMPLATE_ID]"
                      :contents="getTemplatePreviewContents(option.id)"
                      :theme-color="previewStyle.themeColor"
                      :rich-font-family="previewStyle.richFontFamily"
                      :rich-font-size="previewStyle.richFontSize"
                      :rich-line-height="previewStyle.richLineHeight"
                    />
                  </div>
                </div>
                <div class="template-option-meta">
                  <div v-if="option.featured" class="template-option-inline-badge">推荐</div>
                  <div class="template-option-name">{{ option.label }}</div>
                  <span class="template-option-sep" aria-hidden="true">·</span>
                  <div class="template-option-desc">{{ option.description }}</div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div v-else-if="activeFamilyTemplates.length" class="template-select-single-group">
          <section class="template-group">
            <div v-if="activeFamilyGroup" class="template-group-head">
              <div class="template-group-meta">
                <h3 class="template-group-title">{{ activeFamilyGroup.label }}</h3>
                <span class="template-group-dot" aria-hidden="true"></span>
                <span class="template-group-count">{{ activeFamilyTemplates.length }} 套</span>
              </div>
            </div>
            <div class="template-select-grid">
              <div
                v-for="option in activeFamilyTemplates"
                :key="option.id"
                class="template-option-card"
                :class="{ active: selectedTemplate === option.id }"
                role="button"
                tabindex="0"
                :aria-label="option.label"
                @click="handleSelect(option.id)"
                @keydown.enter.prevent="handleSelect(option.id)"
                @keydown.space.prevent="handleSelect(option.id)"
              >
                <div class="template-option-preview" :ref="setPreviewRef">
                  <div class="template-option-scale" :style="{ transform: `scale(${previewScale})` }">
                    <component
                      :is="option.component || TEMPLATE_COMPONENTS[DEFAULT_TEMPLATE_ID]"
                      :contents="getTemplatePreviewContents(option.id)"
                      :theme-color="previewStyle.themeColor"
                      :rich-font-family="previewStyle.richFontFamily"
                      :rich-font-size="previewStyle.richFontSize"
                      :rich-line-height="previewStyle.richLineHeight"
                    />
                  </div>
                </div>
                <div class="template-option-meta">
                  <div v-if="option.featured" class="template-option-inline-badge">推荐</div>
                  <div class="template-option-name">{{ option.label }}</div>
                  <span class="template-option-sep" aria-hidden="true">·</span>
                  <div class="template-option-desc">{{ option.description }}</div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div v-else class="template-empty-state">
          暂时没有相关模板
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { DEFAULT_TEMPLATE_PREVIEW_STYLE, getTemplatePreviewContents } from '../templatePreview'
import {
  DEFAULT_TEMPLATE_ID,
  getAllTemplateFamilyGroups,
  getTemplateFamilyTabs,
  sortTemplatesForDisplay,
  TEMPLATE_COMPONENTS,
  TEMPLATE_OPTIONS,
} from '../templateRegistry'

const props = defineProps({
  visible: { type: Boolean, default: false },
  selectedTemplate: { type: String, default: DEFAULT_TEMPLATE_ID },
  templateOptions: { type: Array, default: () => TEMPLATE_OPTIONS },
})

const emit = defineEmits(['close', 'select-template'])

const activeFamilyTab = ref('all')
const previewRef = ref(null)
const familyTabs = getTemplateFamilyTabs()
const previewStyle = DEFAULT_TEMPLATE_PREVIEW_STYLE
const previewScale = ref(0.592)
let resizeObserver = null

const allGroups = computed(() => getAllTemplateFamilyGroups(props.templateOptions))
const activeFamilyTemplates = computed(() => {
  if (activeFamilyTab.value === 'all') return []
  return sortTemplatesForDisplay(
    props.templateOptions.filter(option => option.family === activeFamilyTab.value),
  )
})
const activeFamilyGroup = computed(() => {
  if (activeFamilyTab.value === 'all') return null
  return allGroups.value.find(group => group.family === activeFamilyTab.value) || null
})

watch(() => props.visible, visible => {
  if (visible) activeFamilyTab.value = 'all'
})

watch(() => props.visible, visible => {
  if (!visible) return
  nextTick(() => {
    updatePreviewScale()
    setupPreviewScaleObserver()
  })
})

watch(activeFamilyTab, () => {
  nextTick(updatePreviewScale)
})

function setPreviewRef(el) {
  if (!el) return
  previewRef.value = el
}

function updatePreviewScale() {
  const preview = previewRef.value
  if (!preview) return
  const width = preview.getBoundingClientRect().width
  if (!width) return
  previewScale.value = width / 794
}

function setupPreviewScaleObserver() {
  resizeObserver?.disconnect()
  resizeObserver = null
  if (!previewRef.value) return
  resizeObserver = new ResizeObserver(() => updatePreviewScale())
  resizeObserver.observe(previewRef.value)
}

onMounted(() => {
  if (!props.visible) return
  nextTick(() => {
    updatePreviewScale()
    setupPreviewScaleObserver()
  })
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})

function handleSelect(templateId) {
  emit('select-template', templateId)
}
</script>

<style scoped>
.template-select-backdrop {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px;
  background: rgba(15, 23, 42, 0.24);
  backdrop-filter: blur(6px);
}

.template-select-menu {
  position: relative;
  width: min(1040px, calc(100vw - 64px));
  min-width: 0;
  max-height: min(925px, calc(100vh - 32px));
  padding: 22px;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  box-shadow: 0 28px 72px rgba(15, 23, 42, 0.16);
  overflow: hidden;
}

.template-select-body {
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow-y: auto;
  padding-right: 6px;
  scrollbar-gutter: stable;
}

.template-select-body > * {
  min-width: 0;
}

.template-select-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.template-select-title-block {
  display: grid;
  gap: 4px;
}

.template-select-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-1);
}

.template-select-subtitle {
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-3);
}

.template-select-close {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: none;
  color: var(--text-3);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}

.template-select-close:hover {
  color: var(--text-1);
  background: rgba(0, 0, 0, 0.06);
}

.template-select-tabs {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
}

.template-select-tabs--compact {
  width: calc((100% - 18px) / 2);
  min-width: 0;
}

.template-select-tab {
  min-width: 64px;
  flex: 1 1 0;
}

.template-select-tab--action {
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  color: var(--text-2);
  font-size: 13px;
  line-height: 1;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.16s ease;
  white-space: nowrap;
}

.template-select-tab--action:hover {
  border-color: var(--text-3);
}

.template-select-tab--action.on {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light, #eef1ff);
}

.template-select-groups {
  display: grid;
  margin-top: 10px;
  gap: 22px;
}

.template-select-single-group {
  margin-top: 10px;
}

.template-group {
  display: grid;
  gap: 12px;
}

.template-group-head {
  display: flex;
  align-items: center;
}

.template-group-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.template-group-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-1);
}

.template-group-count {
  font-size: 12px;
  color: var(--text-3);
}

.template-group-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.9);
}

.template-select-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.template-option-card {
  position: relative;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: box-shadow 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.template-option-card:hover {
  border-color: rgba(70, 114, 242, 0.28);
}

.template-option-card.active {
  border-color: var(--primary);
  background: #fff;
}

.template-option-preview {
  width: 100%;
  aspect-ratio: 794 / 1123;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: #fff;
}

.template-option-scale {
  position: relative;
  z-index: 0;
  width: 794px;
  height: 1123px;
  flex: none;
  transform-origin: center center;
  pointer-events: none;
}

.template-option-meta {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  text-align: center;
  padding: 0 10px 10px;
}

.template-option-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
}

.template-option-inline-badge {
  height: 20px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.1);
  color: var(--primary);
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

.template-option-sep {
  font-size: 13px;
  color: var(--text-3);
  line-height: 1;
}

.template-option-desc {
  font-size: 12px;
  line-height: 1.55;
  color: var(--text-3);
}

.template-empty-state {
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  color: var(--text-3);
  font-size: 13px;
  background: rgba(248, 250, 252, 0.72);
}

@media (max-width: 900px) {
  .template-select-menu {
    width: min(520px, calc(100vw - 24px));
    max-height: min(82vh, calc(100vh - 24px));
    padding: 14px;
    gap: 12px;
  }

  .template-select-backdrop {
    padding: 12px;
  }

  .template-select-head {
    gap: 10px;
  }

  .template-select-title {
    font-size: 16px;
  }

  .template-select-subtitle {
    font-size: 12px;
  }

  .template-select-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .template-select-tabs {
    flex-wrap: wrap;
  }

  .template-select-tabs--compact {
    width: 100%;
  }
}
</style>
