<template>
  <Teleport to="body">
    <div v-if="visible" class="ai-overlay">
      <div
        class="ai-dialog"
        :class="{ 'is-dragging': dragging }"
        :style="{ left: pos.x + 'px', top: pos.y + 'px', width: size.w + 'px', height: size.h + 'px' }"
        @mousedown.stop
      >
        <!-- 标题栏 -->
        <div class="ai-header" @mousedown="startDrag">
          <div class="ai-title">
            <Sparkles :size="18" class="ai-title-icon" />
            AI 简历助手
          </div>
          <div class="ai-header-actions" @mousedown.stop>
            <button class="ai-header-btn ai-header-btn-danger" title="清空对话" :disabled="clearing" @mousedown.stop @click.stop="handleClear">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            </button>
            <button class="ai-header-btn" title="关闭" @mousedown.stop @click.stop="$emit('close')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="msgListRef" class="ai-messages" @scroll="onScroll">
          <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.role">
            <div class="msg-bubble">
              <div v-if="msg.loadingLabel" class="msg-loading-label">
                <span>正在整理修改内容</span>
                <span class="inline-typing"><span></span><span></span><span></span></span>
              </div>
              <div v-else-if="msg.streaming" class="msg-text msg-text-streaming">{{ msg.displayContent || msg.content }}</div>
              <div v-else class="msg-text" v-html="renderText(msg.content)"></div>
              <div v-if="msg.role === 'assistant' && !msg.content && !msg.displayContent && !msg.loadingLabel && sending" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
          <div v-if="!messages.length" class="ai-empty">
            <Sparkles :size="40" class="ai-empty-icon" />
            <p>有什么可以帮助你的？</p>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="ai-input-area" style="border-top-style: none; border-top-width: 0px;">
          <div class="ai-input-shell">
            <img
              class="ai-input-mascot"
              src="/robot.png"
              alt="AI 助手机器人挂件"
              draggable="false"
            />
            <textarea
              ref="inputRef"
              v-model="inputText"
              :placeholder="inputPlaceholder"
              rows="1"
              @keydown="onKeydown"
              @input="autoResize"
              @paste="onPaste"
            ></textarea>
            <div class="ai-input-toolbar" @mousedown.stop>
              <div class="ai-mode-switch">
                <button
                  type="button"
                  class="ai-mode-btn"
                  :class="{ active: chatMode === 'chat' }"
                  @click="chatMode = 'chat'"
                >
                  聊一聊
                </button>
                <button
                  type="button"
                  class="ai-mode-btn"
                  :class="{ active: chatMode === 'modify' }"
                  @click="chatMode = 'modify'"
                >
                  直接修改
                </button>
              </div>
              <button
                class="ai-send-btn"
                :class="{ active: !!inputText.trim() && !sending }"
                :disabled="!inputText.trim() || sending"
                @click="send"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/></svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 四角缩放手柄 -->
        <div class="ai-resize ai-resize-tl" @mousedown.stop="startResize($event, 'tl')"></div>
        <div class="ai-resize ai-resize-tr" @mousedown.stop="startResize($event, 'tr')"></div>
        <div class="ai-resize ai-resize-bl" @mousedown.stop="startResize($event, 'bl')"></div>
        <div class="ai-resize ai-resize-br" @mousedown.stop="startResize($event, 'br')"></div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, reactive, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { Sparkles } from 'lucide-vue-next'
import { chatStream, getChatHistory, clearChatHistory } from '../../../api/ai'

const props = defineProps({
  visible: { type: Boolean, default: false },
  resumeId: { type: [Number, String], default: null },
  moduleData: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['close', 'suggest'])

const messages = ref([])
const inputText = ref('')
const chatMode = ref('modify')
const sending = ref(false)
const clearing = ref(false)
const inputRef = ref(null)
const msgListRef = ref(null)
const shouldAutoFollow = ref(true)
const lastKnownScrollTop = ref(0)
let activeStreamAbort = null
let activeAnimationCleanup = null

const pos = reactive({ x: 0, y: 0 })
const size = reactive({ w: 800, h: 580 })

const MIN_W = 420, MIN_H = 440
const BOTTOM_FOLLOW_THRESHOLD = 48

const inputPlaceholder = computed(() => (
  chatMode.value === 'modify'
    ? '描述你想要直接修改的简历内容...'
    : '想聊聊这份简历的什么问题？'
))

// 初始化位置
function initPos() {
  const w = window.innerWidth
  const h = window.innerHeight
  pos.x = Math.max(0, (w - size.w) / 2)
  pos.y = Math.max(80, (h - size.h) / 2)
}

// 加载历史
async function loadHistory() {
  if (!props.resumeId) return
  try {
    const history = await getChatHistory(props.resumeId)
    if (history && history.length) {
      messages.value = history.map(h => ({
        role: h.role,
        content: h.content,
        applyData: tryParseJson(h.content),
      }))
      await nextTick()
      scrollToBottom(true)
    }
  } catch {}
}

watch(() => props.visible, (val) => {
  if (val) {
    initPos()
    loadHistory()
    nextTick(() => inputRef.value?.focus())
  } else {
    cancelActiveStreaming()
  }
})

function renderText(text) {
  return stripModifyTags(text)
    .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function stripModifyTags(text) {
  return text
    .replace(/<!--RESUME_MODIFY-->[\s\S]*?<!--\/RESUME_MODIFY-->/g, '')
    .replace(/^\s*\n+/g, '')
    .replace(/\n{3,}/g, '\n\n')
}

function tryParseJson(text) {
  if (typeof text !== 'string' || !text.trim()) return null
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

function cancelActiveStreaming() {
  if (activeAnimationCleanup) {
    const cleanup = activeAnimationCleanup
    activeAnimationCleanup = null
    cleanup()
  }
  if (activeStreamAbort) {
    const abortStream = activeStreamAbort
    activeStreamAbort = null
    abortStream()
  }
  sending.value = false
}

function send() {
  const text = inputText.value.trim()
  if (!text || sending.value) return
  cancelActiveStreaming()
  const requestMode = chatMode.value

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  resetTextarea()
  shouldAutoFollow.value = true
  scrollToBottom(true)

  messages.value.push({
    role: 'assistant',
    content: '',
    displayContent: '',
    loadingLabel: requestMode === 'modify',
    streaming: true,
  })
  const aiIdx = messages.value.length - 1
  sending.value = true

  let pending = ''
  let rafId = null
  let revealTimer = null
  let pendingDone = false
  let chatTimer = null
  let lastScrollAt = 0
  let closed = false

  function clearRevealTimer() {
    if (revealTimer) {
      clearInterval(revealTimer)
      revealTimer = null
    }
  }

  function clearChatTimer() {
    if (chatTimer) {
      clearInterval(chatTimer)
      chatTimer = null
    }
  }

  function releaseRequest() {
    if (activeAnimationCleanup === cleanupRequest) {
      activeAnimationCleanup = null
    }
    if (activeStreamAbort === abortStream) {
      activeStreamAbort = null
    }
  }

  function cleanupRequest() {
    if (closed) return
    closed = true
    if (rafId) {
      cancelAnimationFrame(rafId)
      rafId = null
    }
    clearRevealTimer()
    clearChatTimer()
    releaseRequest()
  }

  function isActive() {
    return !closed
  }

  function scheduleScroll(force = false) {
    if (!force && !shouldAutoFollow.value) return
    const now = performance.now()
    if (!force && now - lastScrollAt < 32) return
    lastScrollAt = now
    scrollToBottom(force)
  }

  function startChatAnimator() {
    const msg = messages.value[aiIdx]
    if (!msg) return
    clearChatTimer()
    chatTimer = setInterval(() => {
      if (!isActive()) {
        clearChatTimer()
        return
      }
      const current = messages.value[aiIdx]
      if (!current) {
        cleanupRequest()
        clearChatTimer()
        return
      }
      if (pending) {
        const step = pending.slice(0, 1)
        current.content += step
        current.displayContent = stripModifyTags(current.content)
        pending = pending.slice(step.length)
        scheduleScroll()
        return
      }
      clearChatTimer()
      if (pendingDone) {
        finalizeAssistantMessage()
        scheduleScroll()
      }
    }, 24)
  }

  function pushChatChunk(chunk) {
    pending += chunk
    if (!chatTimer) {
      startChatAnimator()
      scheduleScroll()
    }
  }

  function finalizeAssistantMessage() {
    if (!isActive()) return
    clearChatTimer()
    const current = messages.value[aiIdx]
    if (!current) {
      cleanupRequest()
      sending.value = false
      return
    }
    current.streaming = false
    current.loadingLabel = false
    current.displayContent = ''
    releaseRequest()
    sending.value = false
  }

  function flush() {
    if (!isActive()) return
    if (pending) {
      const chunk = pending
      pending = ''
      if (requestMode === 'modify') {
        const current = messages.value[aiIdx]
        if (!current) {
          cleanupRequest()
          return
        }
        clearRevealTimer()
        current.content = chunk
        current.loadingLabel = false
        current.displayContent = ''
        let cursor = 0
        revealTimer = setInterval(() => {
          if (!isActive()) {
            clearRevealTimer()
            return
          }
          const activeMessage = messages.value[aiIdx]
          if (!activeMessage) {
            cleanupRequest()
            clearRevealTimer()
            return
          }
          cursor += 1
          activeMessage.displayContent = chunk.slice(0, cursor)
          scheduleScroll()
          if (cursor >= chunk.length) {
            clearRevealTimer()
            if (pendingDone) {
              finalizeAssistantMessage()
            }
          }
        }, 24)
      } else {
        pushChatChunk(chunk)
      }
    }
  }

  const abortStream = chatStream(props.resumeId, text, requestMode, props.moduleData, {
    onMessage(chunk) {
      if (!isActive()) return
      pending += chunk
      if (!rafId || requestMode === 'modify') {
        rafId = requestAnimationFrame(() => {
          flush()
          rafId = null
        })
      }
    },
    onSuggest(moduleType, content, itemIndex) {
      if (!isActive()) return
      if (requestMode === 'modify') {
        emit('suggest', moduleType, content, itemIndex)
      }
    },
    onDone() {
      if (!isActive()) return
      if (rafId) cancelAnimationFrame(rafId)
      flush()
      if (requestMode === 'modify' && revealTimer) {
        pendingDone = true
        return
      }
      if (requestMode === 'chat' && pending) {
        pendingDone = true
        if (!chatTimer) startChatAnimator()
        return
      }
      finalizeAssistantMessage()
    },
    onError(err) {
      if (!isActive()) return
      if (rafId) cancelAnimationFrame(rafId)
      flush()
      cleanupRequest()
      sending.value = false
      const current = messages.value[aiIdx]
      if (!current) return
      current.content = '抱歉，出现了问题：' + (err.message || '未知错误')
      current.loadingLabel = false
      current.displayContent = ''
      current.streaming = false
    },
  })
  activeAnimationCleanup = cleanupRequest
  activeStreamAbort = abortStream
}

async function handleClear() {
  if (!props.resumeId || clearing.value) return
  cancelActiveStreaming()
  const prevMessages = messages.value
  messages.value = []
  clearing.value = true
  try {
    await clearChatHistory(props.resumeId)
  } catch {
    messages.value = prevMessages
  } finally {
    clearing.value = false
  }
}

function isNearBottom(el) {
  if (!el) return true
  return el.scrollHeight - (el.scrollTop + el.clientHeight) <= BOTTOM_FOLLOW_THRESHOLD
}

function scrollToBottom(force = false) {
  if (!force && !shouldAutoFollow.value) return
  nextTick(() => {
    const el = msgListRef.value
    if (!el) return
    el.scrollTop = el.scrollHeight
    lastKnownScrollTop.value = el.scrollTop
    shouldAutoFollow.value = true
  })
}

function onScroll() {
  const el = msgListRef.value
  if (!el) return
  if (el.scrollTop < lastKnownScrollTop.value) {
    shouldAutoFollow.value = false
  } else if (isNearBottom(el)) {
    shouldAutoFollow.value = true
  }
  lastKnownScrollTop.value = el.scrollTop
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function onPaste(e) {
  e.preventDefault()
  const text = e.clipboardData.getData('text').replace(/\n+$/, '')
  inputText.value += text
  nextTick(autoResize)
}

function resetTextarea() {
  nextTick(() => {
    const el = inputRef.value
    if (el) {
      el.style.height = 'auto'
    }
  })
}

// 拖动
let dragging = false
let dragStartX = 0, dragStartY = 0, dragStartPosX = 0, dragStartPosY = 0

function startDrag(e) {
  dragging = true
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartPosX = pos.x
  dragStartPosY = pos.y
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

function onDrag(e) {
  if (!dragging) return
  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY
  pos.x = Math.max(0, Math.min(window.innerWidth - size.w, dragStartPosX + dx))
  pos.y = Math.max(0, Math.min(window.innerHeight - size.h, dragStartPosY + dy))
}

function stopDrag() {
  dragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 缩放
let resizing = false
let resizeDir = ''
let resizeStartX = 0, resizeStartY = 0
let resizeStartPosX = 0, resizeStartPosY = 0
let resizeStartW = 0, resizeStartH = 0

function startResize(e, dir) {
  resizing = true
  resizeDir = dir
  resizeStartX = e.clientX
  resizeStartY = e.clientY
  resizeStartPosX = pos.x
  resizeStartPosY = pos.y
  resizeStartW = size.w
  resizeStartH = size.h
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

function onResize(e) {
  if (!resizing) return
  const dx = e.clientX - resizeStartX
  const dy = e.clientY - resizeStartY
  let newW = resizeStartW, newH = resizeStartH, newX = resizeStartPosX, newY = resizeStartPosY

  if (resizeDir.includes('r')) newW = Math.max(MIN_W, resizeStartW + dx)
  if (resizeDir.includes('l')) {
    newW = Math.max(MIN_W, resizeStartW - dx)
    newX = resizeStartPosX + resizeStartW - newW
  }
  if (resizeDir.includes('b')) newH = Math.max(MIN_H, resizeStartH + dy)
  if (resizeDir.includes('t')) {
    newH = Math.max(MIN_H, resizeStartH - dy)
    newY = resizeStartPosY + resizeStartH - newH
  }

  size.w = newW
  size.h = newH
  pos.x = newX
  pos.y = newY
}

function stopResize() {
  resizing = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

onBeforeUnmount(() => {
  cancelActiveStreaming()
})
</script>

<style scoped>
.ai-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  pointer-events: none;
}

.ai-dialog {
  position: fixed;
  width: 800px;
  height: 580px;
  min-width: 420px;
  min-height: 440px;
  background: #fff;
  pointer-events: auto;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-dialog.is-dragging {
  cursor: default;
}

/* 标题栏 */
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border);
  user-select: none;
  flex-shrink: 0;
}
.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-1);
}
.ai-title-icon {
  color: var(--primary);
  flex-shrink: 0;
}
.ai-header-actions {
  display: flex;
  gap: 4px;
}
.ai-header-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  background: none;
  cursor: pointer;
  color: var(--text-3);
  border-radius: 6px;
  transition: all 0.15s ease;
}
.ai-header-btn:hover { color: var(--text-1); background: rgba(0,0,0,0.06); }
.ai-header-btn-danger:hover {
  color: var(--danger);
  background: #fef2f2;
  border-color: #fecaca;
}

/* 消息列表 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scrollbar-width: thin;
}
.msg-row { display: flex; flex-direction: column; }
.msg-row.user { align-items: flex-end; }
.msg-row.assistant { align-items: flex-start; }

.msg-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.user .msg-bubble {
  background: var(--primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.assistant .msg-bubble {
  background: var(--bg-page);
  color: var(--text-1);
  border-bottom-left-radius: 4px;
}

.msg-text-streaming {
  white-space: pre-wrap;
}

.msg-loading-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: inherit;
}

.inline-typing {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transform: translateY(1px);
}

.inline-typing span {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.4;
  animation: typing 1.2s infinite;
}

.inline-typing span:nth-child(2) { animation-delay: 0.2s; }
.inline-typing span:nth-child(3) { animation-delay: 0.4s; }

.msg-text :deep(pre) {
  margin: 4px 0;
  padding: 8px 10px;
  background: rgba(0,0,0,0.04);
  border-radius: 6px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
}

.msg-text :deep(ul), .msg-text :deep(ol) {
  margin: 4px 0;
  padding-left: 20px;
}

.msg-text :deep(li) {
  margin: 2px 0;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-3);
  animation: typing 1.2s infinite;
}
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

.ai-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-3);
}
.ai-empty p { font-size: 13px; margin: 0; }

/* 输入区 */
.ai-input-area {
  padding: 12px 16px;
  flex-shrink: 0;
  background: #fff;
  position: relative;
}

.ai-input-shell {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fff;
  border: 1px solid #bfdbfe;
  border-radius: 20px;
  padding: 14px 14px 10px;
  box-shadow: 0 10px 26px rgba(59, 130, 246, 0.08);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.ai-input-mascot {
  position: absolute;
  top: 0;
  right: -20px;
  width: 150px;
  height: auto;
  transform: translateY(-71%);
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(0 6px 14px rgba(15, 23, 42, 0.14));
  z-index: 2;
}

.ai-input-shell:focus-within {
  border-color: #93c5fd;
  box-shadow: 0 12px 30px rgba(59, 130, 246, 0.12);
}

.ai-input-shell textarea {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-1);
  resize: none;
  max-height: 120px;
  min-height: 30px;
  padding: 0 2px;
  font-family: inherit;
}

.ai-input-shell textarea::placeholder {
  color: var(--text-3);
}

.ai-input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 2px;
}

.ai-mode-switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-mode-btn {
  border: 1px solid transparent;
  background: #f8fafc;
  color: var(--text-2);
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  text-align: center;
  transition: all 0.15s ease;
}

.ai-mode-btn:hover {
  color: var(--text-1);
  background: #f1f5f9;
}

.ai-mode-btn.active {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: var(--primary);
  box-shadow: 0 1px 2px rgba(59, 130, 246, 0.12);
}

.ai-resize {
  position: absolute;
  width: 12px;
  height: 12px;
  z-index: 2;
  opacity: 0;
  transition: opacity 0.15s;
}
.ai-dialog:hover .ai-resize {
  opacity: 1;
}
.ai-resize-tl {
  top: 0; left: 0;
  cursor: nwse-resize;
  border-top: 2px solid var(--text-3);
  border-left: 2px solid var(--text-3);
  border-top-left-radius: 12px;
}
.ai-resize-tr {
  top: 0; right: 0;
  cursor: nesw-resize;
  border-top: 2px solid var(--text-3);
  border-right: 2px solid var(--text-3);
  border-top-right-radius: 12px;
}
.ai-resize-bl {
  bottom: 0; left: 0;
  cursor: nesw-resize;
  border-bottom: 2px solid var(--text-3);
  border-left: 2px solid var(--text-3);
  border-bottom-left-radius: 12px;
}
.ai-resize-br {
  bottom: 0; right: 0;
  cursor: nwse-resize;
  border-bottom: 2px solid var(--text-3);
  border-right: 2px solid var(--text-3);
  border-bottom-right-radius: 12px;
}
.ai-send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #e2e8f0;
  border-radius: 999px;
  cursor: not-allowed;
  color: #475569;
  transition: all 0.15s ease;
  flex-shrink: 0;
}
.ai-send-btn.active {
  background: var(--primary);
  cursor: pointer;
  color: #fff;
}
.ai-send-btn.active:hover { opacity: 0.85; }

@media (max-width: 640px) {
  .ai-input-mascot {
    width: 46px;
    right: 14px;
    transform: translateY(-36%);
  }
}
</style>
