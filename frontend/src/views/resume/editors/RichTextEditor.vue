<template>
  <div class="rte" :class="{ fullscreen: isFullscreen }">
    <div class="rte-toolbar">
      <button class="tb-btn" @click="exec('undo')" title="撤销"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg></button>
      <button class="tb-btn" @click="exec('redo')" title="重做"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.13-9.36L23 10"/></svg></button>

      <span class="tb-sep"></span>

      <div class="tb-dropdown" :class="{ open: openMenu === 'fontSize' }">
        <button class="tb-btn" @click="toggleMenu('fontSize')">{{ fontSizeLabel }} <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M6 9l6 6 6-6"/></svg></button>
        <div v-if="openMenu === 'fontSize'" class="dd-list">
          <div v-for="s in fontSizes" :key="s" class="dd-item" :class="{ active: fontSize === s }" @click="setFontSize(s)">{{ s }}</div>
        </div>
      </div>

      <div class="tb-dropdown" :class="{ open: openMenu === 'lineHeight' }">
        <button class="tb-btn" @click="toggleMenu('lineHeight')">行高 <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M6 9l6 6 6-6"/></svg></button>
        <div v-if="openMenu === 'lineHeight'" class="dd-list">
          <div v-for="h in lineHeights" :key="h" class="dd-item" :class="{ active: lineHeight === h }" @click="setLineHeight(h)">{{ h }}</div>
        </div>
      </div>

      <span class="tb-sep"></span>

      <button class="tb-btn" :class="{ on: isActive('bold') }" @click="exec('bold')" title="加粗"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/><path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/></svg></button>
      <button class="tb-btn" :class="{ on: isActive('italic') }" @click="exec('italic')" title="倾斜"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="4" x2="10" y2="4"/><line x1="14" y1="20" x2="5" y2="20"/><line x1="15" y1="4" x2="9" y2="20"/></svg></button>
      <button class="tb-btn" :class="{ on: isActive('underline') }" @click="exec('underline')" title="下划线"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 3v7a6 6 0 0 0 12 0V3"/><line x1="4" y1="21" x2="20" y2="21"/></svg></button>

      <span class="tb-sep"></span>

      <button class="tb-btn" :class="{ on: isActive('insertUnorderedList') }" @click="exec('insertUnorderedList')" title="无序列表"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><circle cx="4" cy="6" r="1" fill="currentColor"/><circle cx="4" cy="12" r="1" fill="currentColor"/><circle cx="4" cy="18" r="1" fill="currentColor"/></svg></button>
      <button class="tb-btn" :class="{ on: isActive('insertOrderedList') }" @click="exec('insertOrderedList')" title="有序列表"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="10" y1="6" x2="21" y2="6"/><line x1="10" y1="12" x2="21" y2="12"/><line x1="10" y1="18" x2="21" y2="18"/><text x="2" y="8" font-size="8" fill="currentColor" stroke="none" font-family="sans-serif">1</text><text x="2" y="14" font-size="8" fill="currentColor" stroke="none" font-family="sans-serif">2</text><text x="2" y="20" font-size="8" fill="currentColor" stroke="none" font-family="sans-serif">3</text></svg></button>

      <span class="tb-sep"></span>

      <div class="tb-dropdown" :class="{ open: openMenu === 'color' }">
        <button class="tb-btn" @click="toggleMenu('color')" title="字体颜色"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9.9 4.24A.76.76 0 0 0 9.1 4H5.5a.5.5 0 0 0-.44.26L.5 12.43a.5.5 0 0 0 .44.74H3.1a.5.5 0 0 0 .47-.33l.79-2h4.28l.79 2a.5.5 0 0 0 .47.33h2.16a.5.5 0 0 0 .44-.74L9.9 4.24z"/><rect x="2" y="19" width="20" height="3" rx="1.5"/></svg></button>
        <div v-if="openMenu === 'color'" class="dd-colors">
          <div class="color-grid">
            <div v-for="c in colors" :key="c" class="color-dot" :style="{ background: c }" @click="setColor(c)"></div>
          </div>
        </div>
      </div>

      <span class="tb-sep"></span>

      <button class="tb-btn" :class="{ on: isActive('justifyLeft') }" @click="exec('justifyLeft')" title="居左"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="15" y2="12"/><line x1="3" y1="18" x2="18" y2="18"/></svg></button>
      <button class="tb-btn" :class="{ on: isActive('justifyCenter') }" @click="exec('justifyCenter')" title="居中"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="6" y1="12" x2="18" y2="12"/><line x1="4" y1="18" x2="20" y2="18"/></svg></button>
      <button class="tb-btn" :class="{ on: isActive('justifyRight') }" @click="exec('justifyRight')" title="居右"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="9" y1="12" x2="21" y2="12"/><line x1="6" y1="18" x2="21" y2="18"/></svg></button>
      <button class="tb-btn" :class="{ on: isActive('justifyFull') }" @click="exec('justifyFull')" title="两端"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg></button>

      <span class="tb-sep"></span>

      <button class="tb-btn" @click="exec('indent')" title="增加缩进"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/><polyline points="9 4 5 8 9 12"/><polyline points="9 14 5 18 9 22" transform="translate(0,-2)"/></svg></button>
      <button class="tb-btn" @click="exec('outdent')" title="减少缩进"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/><polyline points="15 4 19 8 15 12"/></svg></button>

      <span class="tb-sep"></span>

      <button class="tb-btn" @click="isFullscreen = !isFullscreen" title="全屏"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/><line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/></svg></button>
      <button class="tb-btn" @click="exec('removeFormat')" title="清除样式"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 18L18 6"/><path d="M6 6l12 12"/></svg></button>
    </div>

    <div ref="editor" class="rte-content" contenteditable="true" :style="{ fontFamily: richFontFamily, fontSize: richFontSize + 'px' }" @input="onInput" @keydown="onKeydown"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick, inject } from 'vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

const editor = ref(null)
const linkInput = ref(null)
const openMenu = ref('')
const isFullscreen = ref(false)
const showLinkBar = ref(false)
const linkUrl = ref('')

const richFontFamily = inject('richFontFamily', ref("'Noto Sans SC', sans-serif"))
const richFontSize = inject('richFontSize', ref(12))

const fontSizes = [12, 13, 14, 16, 18, 20, 24, 28, 32, 36, 48]
const lineHeights = [1, 1.2, 1.5, 1.75, 2, 2.5, 3]
const colors = [
  '#000000', '#333333', '#666666', '#999999', '#cccccc', '#ffffff',
  '#e74c3c', '#e67e22', '#f1c40f', '#2ecc71', '#3498db', '#9b59b6',
]

const fontSize = ref(null)
const fontSizeLabel = ref('字号')
const lineHeight = ref(1.5)
const activeStates = ref({})
let savedRange = null

function saveRange() {
  const sel = window.getSelection()
  if (sel.rangeCount > 0) savedRange = sel.getRangeAt(0).cloneRange()
}

function restoreRange() {
  if (!savedRange) return false
  const sel = window.getSelection()
  sel.removeAllRanges()
  sel.addRange(savedRange)
  return true
}

function exec(cmd, val) {
  document.execCommand(cmd, false, val || null)
  if (editor.value) editor.value.focus()
  emitInput()
  updateActiveStates()
}

function isActive(cmd) {
  return !!activeStates.value[cmd]
}

function toggleMenu(key) {
  saveRange()
  openMenu.value = openMenu.value === key ? '' : key
}

function normalizeSelectedFontSize(editorEl, size) {
  const selection = window.getSelection()
  if (!selection || !selection.rangeCount) return
  const range = selection.getRangeAt(0)
  let root = range.commonAncestorContainer
  if (root?.nodeType === 3) root = root.parentNode
  if (!root || root === editorEl) {
    root = editorEl
  }
  const targets = []
  if (root.nodeType === 1 && root.matches?.('font[size], span[style*="font-size"]')) {
    targets.push(root)
  }
  if (root.querySelectorAll) {
    targets.push(...root.querySelectorAll('font[size], span[style*="font-size"]'))
  }
  targets.forEach(el => {
    if (el.tagName === 'FONT') {
      el.removeAttribute('size')
    }
    el.style.fontSize = size + 'px'
  })
}

function setFontSize(s) {
  fontSize.value = s
  fontSizeLabel.value = s
  const editorEl = editor.value
  if (!editorEl) return
  if (!restoreRange()) { openMenu.value = ''; return }
  document.execCommand('fontSize', false, '7')
  normalizeSelectedFontSize(editorEl, s)
  editorEl.focus()
  emitInput()
  openMenu.value = ''
}

function setLineHeight(h) {
  lineHeight.value = h
  const editorEl = editor.value
  if (!editorEl) return
  if (!restoreRange()) { openMenu.value = ''; return }
  let node = window.getSelection().anchorNode
  if (node && node.nodeType === 3) node = node.parentNode
  while (node && node !== editorEl) {
    const tag = node.tagName
    if (['DIV', 'P', 'LI', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'UL', 'OL'].includes(tag)) {
      node.style.lineHeight = String(h)
      break
    }
    node = node.parentNode
  }
  if (node === editorEl) editorEl.style.lineHeight = String(h)
  editorEl.focus()
  emitInput()
  openMenu.value = ''
}

function openLinkInput() {
  showLinkBar.value = true
  const sel = window.getSelection()
  linkUrl.value = sel.toString() ? '' : ''
  nextTick(() => linkInput.value?.focus())
}

function applyLink() {
  if (linkUrl.value) {
    exec('createLink', linkUrl.value)
  }
  showLinkBar.value = false
}

function setColor(c) {
  exec('foreColor', c)
  openMenu.value = ''
}

function onInput() {
  emitInput()
  updateActiveStates()
}

function onKeydown(e) {
  if (e.ctrlKey || e.metaKey) {
    if (e.key === 'z') { e.preventDefault(); exec(e.shiftKey ? 'redo' : 'undo') }
  }
}

function emitInput() {
  if (editor.value) {
    emit('update:modelValue', editor.value.innerHTML)
  }
}

function isNodeInsideEditor(node) {
  if (!editor.value || !node) return false
  const target = node.nodeType === Node.TEXT_NODE ? node.parentNode : node
  return !!target && editor.value.contains(target)
}

function isSelectionInsideEditor(selection) {
  if (!selection || !selection.rangeCount || !editor.value) return false
  return isNodeInsideEditor(selection.anchorNode) && isNodeInsideEditor(selection.focusNode)
}

function getRangeTextNodes(range) {
  const root = range.commonAncestorContainer.nodeType === Node.TEXT_NODE
    ? range.commonAncestorContainer.parentNode
    : range.commonAncestorContainer
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      if (!node.textContent?.trim()) return NodeFilter.FILTER_REJECT
      return range.intersectsNode(node) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT
    },
  })
  const nodes = []
  while (walker.nextNode()) {
    nodes.push(walker.currentNode)
  }
  return nodes
}

function isFormattedByTag(node, tags) {
  let current = node?.nodeType === Node.TEXT_NODE ? node.parentNode : node
  while (current && current !== editor.value) {
    if (tags.includes(current.tagName)) {
      return true
    }
    current = current.parentNode
  }
  return false
}

function isFormattedByStyle(node, styleKey, matcher) {
  let current = node?.nodeType === Node.TEXT_NODE ? node.parentNode : node
  while (current && current !== editor.value) {
    const styleValue = current.style?.[styleKey]
    if (styleValue && matcher(styleValue)) {
      return true
    }
    current = current.parentNode
  }
  return false
}

function isMarkActive(range, checkNode) {
  if (range.collapsed) {
    return checkNode(range.startContainer)
  }
  const textNodes = getRangeTextNodes(range)
  if (textNodes.length === 0) {
    return checkNode(range.commonAncestorContainer)
  }
  return textNodes.every(checkNode)
}

function queryCommandStateSafe(cmd) {
  if (typeof document.queryCommandState !== 'function') {
    return false
  }
  try {
    return document.queryCommandState(cmd)
  } catch {
    return false
  }
}

function updateActiveStates() {
  const selection = window.getSelection()
  if (!isSelectionInsideEditor(selection)) {
    activeStates.value = {}
    return
  }

  const range = selection.getRangeAt(0)
  activeStates.value = {
    bold: isMarkActive(range, (node) =>
      isFormattedByTag(node, ['B', 'STRONG']) ||
      isFormattedByStyle(node, 'fontWeight', (value) => Number.parseInt(value, 10) >= 600 || value === 'bold')
    ),
    italic: isMarkActive(range, (node) =>
      isFormattedByTag(node, ['I', 'EM']) ||
      isFormattedByStyle(node, 'fontStyle', (value) => value === 'italic')
    ),
    underline: isMarkActive(range, (node) =>
      isFormattedByTag(node, ['U']) ||
      isFormattedByStyle(node, 'textDecoration', (value) => value.includes('underline'))
    ),
    insertUnorderedList: queryCommandStateSafe('insertUnorderedList'),
    insertOrderedList: queryCommandStateSafe('insertOrderedList'),
    justifyLeft: queryCommandStateSafe('justifyLeft'),
    justifyCenter: queryCommandStateSafe('justifyCenter'),
    justifyRight: queryCommandStateSafe('justifyRight'),
    justifyFull: queryCommandStateSafe('justifyFull'),
  }
}

function setHtml(html) {
  if (editor.value) {
    editor.value.innerHTML = html || ''
  }
}

watch(() => props.modelValue, (val) => {
  if (editor.value && document.activeElement !== editor.value && editor.value.innerHTML !== val) {
    editor.value.innerHTML = val || ''
  }
})

watch(richFontSize, (newSize) => {
  if (!editor.value || !editor.value.innerHTML.trim()) return
  const blocks = editor.value.querySelectorAll('p, div, li, h1, h2, h3, h4, h5, h6, span, font')
  blocks.forEach(el => { el.style.fontSize = newSize + 'px' })
  editor.value.style.fontSize = newSize + 'px'
  emitInput()
})

watch(richFontFamily, (newFamily) => {
  if (!editor.value || !editor.value.innerHTML.trim()) return
  const blocks = editor.value.querySelectorAll('p, div, li, h1, h2, h3, h4, h5, h6, span, font')
  blocks.forEach(el => { el.style.fontFamily = newFamily })
  editor.value.style.fontFamily = newFamily
  emitInput()
})

function handleClickOutside(e) {
  if (openMenu.value && !e.target.closest('.tb-dropdown')) {
    openMenu.value = ''
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('selectionchange', updateActiveStates)
  if (props.modelValue && editor.value) {
    editor.value.innerHTML = props.modelValue
  }
  updateActiveStates()
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('selectionchange', updateActiveStates)
})

defineExpose({ setHtml })
</script>

<style scoped>
.rte {
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
  transition: var(--transition);
}
.rte:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
.rte.fullscreen {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  z-index: 9999; border-radius: 0;
  display: flex; flex-direction: column;
}
.rte.fullscreen .rte-content { flex: 1; }

.rte-toolbar {
  display: flex; align-items: center; flex-wrap: wrap;
  padding: 4px 6px; gap: 2px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-page);
}
.tb-btn {
  width: 30px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer;
  color: var(--text-2); border-radius: 4px;
  transition: all 0.1s ease; flex-shrink: 0;
}
.tb-btn:hover { color: var(--text-1); background: rgba(0,0,0,0.05); }
.tb-btn.on { color: var(--primary); background: var(--primary-light); }
.tb-btn svg { pointer-events: none; }

.tb-sep {
  width: 1px; height: 18px; background: var(--border);
  margin: 0 3px; flex-shrink: 0;
}

.tb-dropdown { position: relative; }
.tb-dropdown .tb-btn {
  width: auto; padding: 0 6px; gap: 3px; font-size: 12px;
}
.dd-list {
  position: absolute; top: 100%; left: 0;
  background: #fff; border: 1px solid var(--border); border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 100;
  min-width: 70px; max-height: 200px; overflow-y: auto;
  scrollbar-width: none;
}
.dd-list::-webkit-scrollbar { display: none; }
.dd-item {
  padding: 6px 12px; font-size: 13px; color: var(--text-1);
  cursor: pointer; transition: background 0.1s ease; white-space: nowrap;
}
.dd-item:hover { background: var(--bg-page); }
.dd-item.active { color: var(--primary); font-weight: 500; }

.dd-colors {
  position: absolute; top: 100%; left: 0;
  background: #fff; border: 1px solid var(--border); border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 100;
  padding: 8px;
}
.color-grid {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 4px;
}
.color-dot {
  width: 22px; height: 22px; border-radius: 4px;
  cursor: pointer; border: 1px solid rgba(0,0,0,0.1);
  transition: transform 0.1s ease;
}
.color-dot:hover { transform: scale(1.15); }

.link-bar {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 8px; border-bottom: 1px solid var(--border);
  background: var(--bg-page);
}
.link-input {
  flex: 1; padding: 4px 8px; font-size: 13px;
  border: 1px solid var(--border); border-radius: 4px;
  outline: none; color: var(--text-1);
}
.link-input:focus { border-color: var(--primary); }
.link-btn {
  padding: 4px 12px; font-size: 12px;
  border: none; border-radius: 4px;
  background: var(--primary); color: #fff; cursor: pointer;
}
.link-cancel { background: var(--bg-page); color: var(--text-2); border: 1px solid var(--border); }

.rte-content {
  padding: 10px 12px; line-height: 1.7;
  min-height: 120px; outline: none;
  color: var(--text-1); overflow-y: auto;
  overflow-wrap: break-word;
  white-space: break-spaces;
}
.rte-content:empty::before {
  content: '可以列举成绩排名、主修课程、证书奖项等';
  color: var(--text-3); pointer-events: none;
}
.rte-content :deep(ol) { padding-left: 24px; }
.rte-content :deep(ul) { padding-left: 24px; }
</style>
