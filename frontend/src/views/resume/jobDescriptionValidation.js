const JD_KEYWORDS = [
  '岗位', '职责', '要求', '负责', '熟悉', '经验', '能力', '任职',
  '开发', '设计', '产品', '运营', '测试', '架构', '技术', '学历', '本科'
]

const MEANINGLESS_PATTERNS = [
  /^你好[啊呀吗嘛哈]*$/,
  /^哈{2,}$/,
  /^测试[一下啊呀吗嘛]*$/,
  /^在吗$/,
]

export function validateJobDescription(input, { minLength = 20 } = {}) {
  const value = String(input || '').trim()
  if (!value) {
    return { valid: false, reason: 'empty', message: '请输入岗位描述后再试' }
  }

  const compact = value.replace(/\s+/g, '')
  if (MEANINGLESS_PATTERNS.some(pattern => pattern.test(compact))) {
    return {
      valid: false,
      reason: 'meaningless',
      message: '岗位描述内容过少，请补充岗位职责、任职要求等有效信息',
    }
  }

  if (compact.length < minLength) {
    return {
      valid: false,
      reason: 'too_short',
      message: '岗位描述过短，建议至少补充职责、要求或技能关键词后再试',
    }
  }

  const keywordHits = JD_KEYWORDS.filter(keyword => compact.includes(keyword)).length
  if (keywordHits === 0) {
    return {
      valid: false,
      reason: 'missing_keywords',
      message: '请输入更像岗位 JD 的内容，例如职责、要求、技能、经验等信息',
    }
  }

  return { valid: true, reason: '', message: '' }
}
