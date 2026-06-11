import request from './request'

export function saveVersion(resumeId, remark) {
  const params = new URLSearchParams()
  params.append('resumeId', resumeId)
  if (remark) params.append('remark', remark)
  return request.post('/resume/version/save?' + params.toString())
}

export function listVersions(resumeId) {
  return request.get('/resume/version/list', { params: { resumeId } })
}
