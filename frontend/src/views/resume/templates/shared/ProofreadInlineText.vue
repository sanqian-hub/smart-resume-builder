<template>
  <template v-for="(segment, index) in segments" :key="`${fieldPath}-${index}-${segment.highlightId || 'plain'}`">
    <span
      v-if="segment.highlighted"
      class="proofread-text-highlight"
      :data-highlight-id="segment.highlightId || undefined"
    >{{ segment.text }}</span>
    <template v-else>{{ segment.text }}</template>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { buildProofreadSegments, matchProofreadHighlights } from '../../proofreadHighlight'

const props = defineProps({
  text: { type: String, default: '' },
  moduleType: { type: String, required: true },
  itemIndex: { type: Number, default: null },
  fieldPath: { type: String, required: true },
  proofreadHighlights: { type: Array, default: () => [] },
})

function normalizeItemIndex(moduleType, itemIndex) {
  if (moduleType === 'basic' || moduleType === 'personalStrengths') {
    return null
  }
  return itemIndex ?? null
}

const fieldHighlights = computed(() => {
  const exactMatches = matchProofreadHighlights(
    props.proofreadHighlights,
    props.moduleType,
    props.itemIndex,
    props.fieldPath,
  )
  if (exactMatches.length || !props.text) {
    return exactMatches
  }

  const fallbackCandidates = props.proofreadHighlights.filter((item) => {
    if (!item?.original) return false
    if (item.moduleType !== props.moduleType) return false
    return item.original === props.text
  })

  if (!fallbackCandidates.length) {
    return []
  }

  const sameItemFallback = fallbackCandidates.filter((item) => {
    if (normalizeItemIndex(item.moduleType, item.itemIndex) !== normalizeItemIndex(props.moduleType, props.itemIndex)) {
      return false
    }
    return true
  })

  if (sameItemFallback.length === 1) {
    return sameItemFallback
  }
  return []
})

const segments = computed(() => buildProofreadSegments(props.text, fieldHighlights.value))
</script>

<style scoped>
.proofread-text-highlight {
  background: rgba(255, 196, 61, 0.5);
  border-radius: 3px;
  box-shadow:
    inset 0 -1px 0 rgba(230, 145, 0, 0.78),
    0 0 0 1px rgba(255, 196, 61, 0.18);
}
</style>
