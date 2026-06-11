import { markRaw } from 'vue'
import ClassicTemplate1 from './templates/Classic/ClassicTemplate1.vue'
import CreativeTemplate1 from './templates/Creative/CreativeTemplate1.vue'
import DualColumnTemplate1 from './templates/DualColumn/DualColumnTemplate1.vue'
import DualColumnTemplate2 from './templates/DualColumn/DualColumnTemplate2.vue'

export const DEFAULT_TEMPLATE_ID = 'classic-1'
export const TEMPLATE_FAMILY_META = {
  classic: {
    label: '经典模板',
    shortLabel: '经典',
    order: 1,
  },
  creative: {
    label: '创意模板',
    shortLabel: '创意',
    order: 2,
  },
  dual: {
    label: '双栏模板',
    shortLabel: '双栏',
    order: 3,
  },
}

export const TEMPLATE_FAMILY_TABS = [
  { key: 'all', label: '全部' },
  ...Object.entries(TEMPLATE_FAMILY_META)
    .sort(([, left], [, right]) => left.order - right.order)
    .map(([family, meta]) => ({
      key: family,
      label: meta.shortLabel,
    })),
]

export const TEMPLATE_REGISTRY = [
  {
    id: 'classic-1',
    label: '经典模板01',
    family: 'classic',
    description: '稳妥通用的单栏布局',
    color: '#4f72f6',
    previewVariant: 'standard',
    featured: true,
    order: 1,
    component: markRaw(ClassicTemplate1),
  },
  {
    id: 'creative-1',
    label: '创意模板01',
    family: 'creative',
    description: '更强调视觉层次和个性',
    color: '#f59e0b',
    previewVariant: 'compact',
    featured: true,
    order: 1,
    component: markRaw(CreativeTemplate1),
  },
  {
    id: 'dual-1',
    label: '双栏模板01',
    family: 'dual',
    description: '信息密度更高的双栏结构',
    color: '#10b981',
    previewVariant: 'compact',
    featured: true,
    order: 1,
    component: markRaw(DualColumnTemplate1),
  },
  {
    id: 'dual-2',
    label: '双栏模板02',
    family: 'dual',
    description: '横向头部更强、右栏更偏摘要的双栏布局',
    color: '#ff7439',
    previewVariant: 'compact',
    featured: true,
    order: 2,
    component: markRaw(DualColumnTemplate2),
  },
]

export const TEMPLATE_OPTIONS = TEMPLATE_REGISTRY

export const TEMPLATE_LABELS = Object.fromEntries(
  TEMPLATE_REGISTRY.map(option => [option.id, option.label]),
)

export const TEMPLATE_COLORS = Object.fromEntries(
  TEMPLATE_REGISTRY.map(option => [option.id, option.color]),
)

export const TEMPLATE_COMPONENTS = Object.fromEntries(
  TEMPLATE_REGISTRY.map(option => [option.id, option.component]),
)

export const TEMPLATE_PREVIEW_VARIANTS = Object.fromEntries(
  TEMPLATE_REGISTRY.map(option => [option.id, option.previewVariant]),
)

export function normalizeTemplateId(templateId) {
  if (!templateId) return DEFAULT_TEMPLATE_ID
  return templateId
}

export function sortTemplatesForDisplay(templates = []) {
  return [...templates].sort((left, right) => {
    if (Boolean(left?.featured) !== Boolean(right?.featured)) {
      return Number(Boolean(right?.featured)) - Number(Boolean(left?.featured))
    }

    const leftOrder = Number.isFinite(left?.order) ? left.order : Number.MAX_SAFE_INTEGER
    const rightOrder = Number.isFinite(right?.order) ? right.order : Number.MAX_SAFE_INTEGER
    if (leftOrder !== rightOrder) return leftOrder - rightOrder

    return String(left?.id || '').localeCompare(String(right?.id || ''))
  })
}

export function getTemplateFamilyTabs() {
  return TEMPLATE_FAMILY_TABS
}

export function getAllTemplateFamilyGroups(templates = TEMPLATE_REGISTRY) {
  return Object.entries(TEMPLATE_FAMILY_META)
    .sort(([, left], [, right]) => left.order - right.order)
    .map(([family, meta]) => ({
      family,
      label: meta.label,
      shortLabel: meta.shortLabel,
      templates: sortTemplatesForDisplay(
        templates.filter(option => option.family === family),
      ),
    }))
    .filter(group => group.templates.length > 0)
}

export function getTemplatePreviewVariant(templateId) {
  const normalizedId = normalizeTemplateId(templateId)
  return TEMPLATE_PREVIEW_VARIANTS[normalizedId] || 'standard'
}
