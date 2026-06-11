<template>
  <div class="proofread-rich-html" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import { buildProofreadRichHtml, matchProofreadHighlights } from '../../proofreadHighlight'

const props = defineProps({
  html: { type: String, default: '' },
  moduleType: { type: String, required: true },
  itemIndex: { type: Number, default: null },
  fieldPath: { type: String, required: true },
  proofreadHighlights: { type: Array, default: () => [] },
})

const fieldHighlights = computed(() =>
  matchProofreadHighlights(props.proofreadHighlights, props.moduleType, props.itemIndex, props.fieldPath),
)

const renderedHtml = computed(() => buildProofreadRichHtml(props.html, fieldHighlights.value))
</script>

<style scoped>
:deep(.proofread-text-highlight) {
  background: rgba(255, 196, 61, 0.5);
  border-radius: 3px;
  box-shadow:
    inset 0 -1px 0 rgba(230, 145, 0, 0.78),
    0 0 0 1px rgba(255, 196, 61, 0.18);
}
</style>
