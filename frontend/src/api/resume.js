import request from './request'

export function addResume(data) {
  return request.post('/resume/add', data)
}

export function updateResume(data) {
  return request.post('/resume/update', data)
}

export function deleteResume(id) {
  return request.post('/resume/delete', { id })
}

export function getResume(id) {
  return request.get(`/resume/get/${id}`)
}

export function pageResume(current, pageSize) {
  return request.post('/resume/list/page', { current, pageSize })
}

export function myList() {
  return request.get('/resume/my/list')
}

export function matchAnalysis(resumeId, jobDescription, moduleData) {
  return request.post('/resume/match', { resumeId, jobDescription, moduleData }, { timeout: 120000 })
}

export function scoreResume(resumeId, moduleData) {
  return request.post('/resume/score', { resumeId, moduleData }, { timeout: 120000 })
}

export function proofreadResume(resumeId, moduleData) {
  return request.post('/resume/proofread', { resumeId, moduleData }, { timeout: 120000 })
}
