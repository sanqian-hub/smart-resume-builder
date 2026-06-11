import request from './request'

export function getNoticeList() {
  return request.get('/notice/list')
}

export function getUnreadCount() {
  return request.get('/notice/unread-count')
}

export function markRead(id) {
  return request.post(`/notice/read/${id}`)
}

export function markAllRead() {
  return request.post('/notice/read-all')
}

export function analyzeAndNotify(resumeId, resumeVersionNum) {
  const params = new URLSearchParams()
  if (resumeVersionNum !== undefined && resumeVersionNum !== null) {
    params.append('resumeVersionNum', resumeVersionNum)
  }
  const suffix = params.toString() ? `?${params.toString()}` : ''
  return request.post(`/notice/analyze/${resumeId}${suffix}`)
}
