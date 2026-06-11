import request from './request'

export function generateSelfIntro({ resumeId, durationSeconds, style, jobDescription, moduleData }) {
  return request.post('/resume/self-intro', {
    resumeId,
    durationSeconds,
    style,
    jobDescription,
    moduleData,
  }, { timeout: 120000 })
}
