const GUEST_RESUME_DRAFT_KEY = 'guest-resume-draft'
const GUEST_RESUME_DRAFT_TTL_MS = 7 * 24 * 60 * 60 * 1000

function clearInvalidGuestResumeDraft() {
  localStorage.removeItem(GUEST_RESUME_DRAFT_KEY)
}

function isExpired(updatedAt) {
  const timestamp = Date.parse(updatedAt || '')
  if (!Number.isFinite(timestamp)) return true
  return Date.now() - timestamp > GUEST_RESUME_DRAFT_TTL_MS
}

export function readGuestResumeDraftRecord() {
  const raw = localStorage.getItem(GUEST_RESUME_DRAFT_KEY)
  if (!raw) return null

  try {
    const parsed = JSON.parse(raw)
    if (!parsed || typeof parsed !== 'object') return null
    if (parsed.version !== 1) return null
    if (!parsed.payload || typeof parsed.payload !== 'object') return null
    if (isExpired(parsed.updatedAt)) {
      clearInvalidGuestResumeDraft()
      return null
    }
    return parsed
  } catch {
    return null
  }
}

export function loadGuestResumeDraft() {
  return readGuestResumeDraftRecord()?.payload || null
}

export function saveGuestResumeDraft(payload) {
  localStorage.setItem(GUEST_RESUME_DRAFT_KEY, JSON.stringify({
    version: 1,
    updatedAt: new Date().toISOString(),
    payload,
  }))
}

export function clearGuestResumeDraft() {
  localStorage.removeItem(GUEST_RESUME_DRAFT_KEY)
}

export function getGuestResumeDraftKey() {
  return GUEST_RESUME_DRAFT_KEY
}
