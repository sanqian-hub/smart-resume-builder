import request from './request'

export function createShare(resumeId, versionId, options = {}) {
  const params = new URLSearchParams({ resumeId })
  if (versionId) params.append('versionId', versionId)
  if (options.password) params.append('password', options.password)
  if (options.expireDays !== undefined && options.expireDays !== null) params.append('expireDays', options.expireDays)
  return request.post(`/resume/share/create?${params.toString()}`)
}

export function getPublicShare(shareKey) {
  return request.get(`/resume/share/public/${shareKey}`)
}

export function verifyPublicShare(shareKey, password) {
  const params = new URLSearchParams({ password })
  return request.post(`/resume/share/public/${shareKey}/verify?${params.toString()}`)
}

export function listShares(resumeId) {
  return request.get(`/resume/share/list?resumeId=${resumeId}`)
}

export function closeShare(shareId) {
  return request.post(`/resume/share/close?shareId=${shareId}`)
}

export function updateSharePassword(shareId, password) {
  const params = new URLSearchParams({ shareId })
  if (password !== undefined && password !== null) params.append('password', password)
  return request.post(`/resume/share/password?${params.toString()}`)
}

export function updateShareExpire(shareId, expireDays) {
  const params = new URLSearchParams({ shareId })
  if (expireDays !== undefined && expireDays !== null) params.append('expireDays', expireDays)
  return request.post(`/resume/share/expire?${params.toString()}`)
}
