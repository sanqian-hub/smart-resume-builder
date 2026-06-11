export const DEFAULT_THEME_COLOR = 'rgb(70, 114, 242)'
export const DEFAULT_RICH_FONT_FAMILY = "'Noto Sans SC', 'Source Han Sans SC', sans-serif"
export const DEFAULT_RICH_FONT_SIZE = 14
export const DEFAULT_RICH_LINE_HEIGHT = 1.7
import { normalizeTemplateId } from './templateRegistry'

function parseJson(input) {
  if (!input) return {}
  if (typeof input === 'object') return input
  if (typeof input !== 'string') return {}
  try {
    return JSON.parse(input)
  } catch {
    return {}
  }
}

function normalizeNumber(value, fallback) {
  const normalized = Number(value)
  return Number.isFinite(normalized) && normalized > 0 ? normalized : fallback
}

export function readStyleConfig(styleConfigInput, basicContentJson = '') {
  const styleConfig = parseJson(styleConfigInput)
  const basic = parseJson(basicContentJson)

  return {
    themeColor: styleConfig.themeColor || basic.themeColor || DEFAULT_THEME_COLOR,
    richFontSize: normalizeNumber(styleConfig.richFontSize ?? basic._richFontSize, DEFAULT_RICH_FONT_SIZE),
    richFontFamily: styleConfig.richFontFamily || basic._richFontFamily || DEFAULT_RICH_FONT_FAMILY,
    richLineHeight: normalizeNumber(styleConfig.richLineHeight ?? basic._richLineHeight, DEFAULT_RICH_LINE_HEIGHT),
  }
}

export function hasStoredStyleConfig(styleConfigInput, basicContentJson = '') {
  const styleConfig = parseJson(styleConfigInput)
  const basic = parseJson(basicContentJson)

  return Boolean(
    styleConfig.themeColor
    || styleConfig.richFontSize != null
    || styleConfig.richFontFamily
    || styleConfig.richLineHeight != null
    || basic.themeColor
    || basic._richFontSize != null
    || basic._richFontFamily
    || basic._richLineHeight != null
  )
}

export function serializeStyleConfig(styleConfigInput, basicContentJson = '') {
  return JSON.stringify(readStyleConfig(styleConfigInput, basicContentJson))
}

export function stripLegacyStyleFieldsFromBasic(basicContentJson) {
  const basic = parseJson(basicContentJson)
  delete basic.themeColor
  delete basic._richFontSize
  delete basic._richFontFamily
  delete basic._richLineHeight
  return JSON.stringify(basic)
}

export function readSnapshotPresentation(snapshot) {
  const basic = snapshot?.contents?.find?.(c => c.moduleType === 'basic')
  const styleConfig = readStyleConfig(snapshot?.styleConfig, basic?.contentJson)

  return {
    template: normalizeTemplateId(snapshot?.template),
    ...styleConfig,
  }
}
