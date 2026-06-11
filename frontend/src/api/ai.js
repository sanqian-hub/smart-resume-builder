const BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api') + '/ai'

export function chatStream(resumeId, message, mode, moduleData, { onMessage, onSuggest, onDone, onError }) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 60000)

  fetch(`${BASE_URL}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    signal: controller.signal,
    body: JSON.stringify({ resumeId, message, mode, moduleData }),
  })
    .then(async (response) => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')

        const parts = buffer.split('\n\n')
        buffer = parts.pop() || ''

        for (const part of parts) {
          for (const line of part.split('\n')) {
            const trimmed = line.trim()
            if (!trimmed.startsWith('data:')) continue
            const data = trimmed.slice(5).trim()
            if (data === '[DONE]') {
              clearTimeout(timer)
              onDone?.()
              return
            }
            try {
              const parsed = JSON.parse(data)
              if (parsed.type === 'suggest') {
                onSuggest?.(parsed.moduleType, parsed.content, parsed.itemIndex)
              } else if (parsed.error) {
                onError?.(new Error(parsed.error))
                return
              } else if (parsed.content) {
                onMessage?.(parsed.content)
              }
            } catch {}
          }
        }
      }
      onDone?.()
      clearTimeout(timer)
    })
    .catch((err) => {
      clearTimeout(timer)
      if (controller.signal.aborted) return
      onError?.(err)
    })

  return () => {
    clearTimeout(timer)
    controller.abort()
  }
}

export function getChatHistory(resumeId) {
  return fetch(`${BASE_URL}/chat/history?resumeId=${resumeId}`, {
    credentials: 'include',
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json().then(data => {
      if (data.code === 0) return data.data
      throw new Error(data.message || '获取历史失败')
    })
  })
}

export function clearChatHistory(resumeId) {
  return fetch(`${BASE_URL}/chat/clear?resumeId=${resumeId}`, {
    method: 'POST',
    credentials: 'include',
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json().then(data => {
      if (data.code === 0) return
      throw new Error(data.message || '清空失败')
    })
  })
}

export function getMemoryList() {
  return fetch(`${BASE_URL}/memory/list`, {
    credentials: 'include',
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json().then(data => {
      if (data.code === 0) return data.data
      throw new Error(data.message || '获取记忆失败')
    })
  })
}

export function deleteMemory(id) {
  return fetch(`${BASE_URL}/memory/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json().then(data => {
      if (data.code === 0) return
      throw new Error(data.message || '删除失败')
    })
  })
}

export function clearMemory() {
  return fetch(`${BASE_URL}/memory/clear`, {
    method: 'POST',
    credentials: 'include',
  }).then(res => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json().then(data => {
      if (data.code === 0) return
      throw new Error(data.message || '清空失败')
    })
  })
}
