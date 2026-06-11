function tryParseJson(value) {
  if (typeof value !== 'string' || !value.trim()) return null
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

function applyArrayItemSuggestion(currentContent, suggestion) {
  if (!Number.isInteger(suggestion.itemIndex) || suggestion.itemIndex < 0) return null

  const currentItems = tryParseJson(currentContent)
  if (!Array.isArray(currentItems) || suggestion.itemIndex >= currentItems.length) return null

  const parsedSuggestion = tryParseJson(suggestion.content)
  if (!parsedSuggestion) return null

  const replacementItem = Array.isArray(parsedSuggestion)
    ? parsedSuggestion[0]
    : parsedSuggestion
  if (!replacementItem || typeof replacementItem !== 'object' || Array.isArray(replacementItem)) return null

  const nextItems = currentItems.map((item, index) => (
    index === suggestion.itemIndex ? replacementItem : item
  ))

  return JSON.stringify(nextItems)
}

export function applySuggestionDraft(modules, moduleData, suggestion) {
  const nextModules = modules.map(module => ({ ...module }))
  const nextModuleData = { ...moduleData }
  const targetIndex = nextModules.findIndex(module => module.key === suggestion.moduleType)
  const targetModule = targetIndex >= 0 ? nextModules[targetIndex] : null

  if (targetModule) {
    nextModules[targetIndex] = {
      ...targetModule,
      enabled: true,
    }
  }

  nextModuleData[suggestion.moduleType] = applyArrayItemSuggestion(
    moduleData[suggestion.moduleType],
    suggestion,
  ) ?? suggestion.content

  return {
    modules: nextModules,
    moduleData: nextModuleData,
    pendingSuggestion: {
      moduleType: suggestion.moduleType,
      oldContent: moduleData[suggestion.moduleType] || '',
      newContent: nextModuleData[suggestion.moduleType],
      wasEnabled: !!targetModule?.enabled,
    },
  }
}

export function rejectSuggestionDraft(modules, moduleData, pendingSuggestion) {
  const nextModules = modules.map(module => (
    module.key === pendingSuggestion.moduleType
      ? { ...module, enabled: pendingSuggestion.wasEnabled }
      : { ...module }
  ))
  const nextModuleData = {
    ...moduleData,
    [pendingSuggestion.moduleType]: pendingSuggestion.oldContent,
  }

  return {
    modules: nextModules,
    moduleData: nextModuleData,
  }
}
