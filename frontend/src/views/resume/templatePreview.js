import {
  DEFAULT_RICH_FONT_FAMILY,
  DEFAULT_RICH_FONT_SIZE,
  DEFAULT_RICH_LINE_HEIGHT,
  DEFAULT_THEME_COLOR,
} from './styleConfig'
import { COMPACT_PLACEHOLDER_DATA, DEFAULT_MODULES, PLACEHOLDER_DATA } from './resumeDefaults'
import { getTemplatePreviewVariant } from './templateRegistry'

export const DEFAULT_TEMPLATE_PREVIEW_STYLE = {
  themeColor: DEFAULT_THEME_COLOR,
  richFontFamily: DEFAULT_RICH_FONT_FAMILY,
  richFontSize: DEFAULT_RICH_FONT_SIZE,
  richLineHeight: DEFAULT_RICH_LINE_HEIGHT,
}

function buildContents(placeholderData) {
  return DEFAULT_MODULES
  .filter(module => module.enabled)
  .map((module, index) => ({
    moduleType: module.key,
    sortOrder: index,
    contentJson: placeholderData[module.key] || null,
  }))
}

export const DEFAULT_TEMPLATE_PREVIEW_CONTENTS = buildContents(PLACEHOLDER_DATA)
export const COMPACT_TEMPLATE_PREVIEW_CONTENTS = buildContents(COMPACT_PLACEHOLDER_DATA)

export function getTemplatePreviewContents(template) {
  if (getTemplatePreviewVariant(template) === 'compact') return COMPACT_TEMPLATE_PREVIEW_CONTENTS
  return DEFAULT_TEMPLATE_PREVIEW_CONTENTS
}
