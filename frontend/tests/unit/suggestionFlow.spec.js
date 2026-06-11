import { describe, it, expect } from 'vitest'
import { applySuggestionDraft, rejectSuggestionDraft } from '../../src/views/resume/utils/suggestionFlow.js'

describe('suggestion flow', () => {
  it('enables a disabled module before applying an AI suggestion and restores it on reject', () => {
    const modules = [
      { key: 'basic', label: '基本信息', enabled: true },
      { key: 'skill', label: '专业技能', enabled: false },
    ]
    const moduleData = {
      basic: '',
      skill: '',
    }

    const applied = applySuggestionDraft(modules, moduleData, {
      moduleType: 'skill',
      content: '<ul><li>Vue 3</li></ul>',
    })

    expect(applied.modules.find(m => m.key === 'skill')?.enabled).toBe(true)
    expect(applied.moduleData.skill).toBe('<ul><li>Vue 3</li></ul>')
    expect(applied.pendingSuggestion.wasEnabled).toBe(false)

    const reverted = rejectSuggestionDraft(applied.modules, applied.moduleData, applied.pendingSuggestion)
    expect(reverted.modules.find(m => m.key === 'skill')?.enabled).toBe(false)
    expect(reverted.moduleData.skill).toBe('')
  })

  it('replaces only the targeted array item when an AI suggestion includes itemIndex', () => {
    const modules = [
      { key: 'experience', label: '工作经历', enabled: true },
    ]
    const originalItems = [
      {
        company: '第一家公司',
        position: '前端开发',
        content: '<ol><li>第一段内容</li></ol>',
      },
      {
        company: '第二家公司',
        position: '高级前端开发',
        content: '<ol><li>第二段旧内容</li></ol>',
      },
    ]
    const moduleData = {
      experience: JSON.stringify(originalItems),
    }

    const applied = applySuggestionDraft(modules, moduleData, {
      moduleType: 'experience',
      itemIndex: 1,
      content: JSON.stringify({
        company: '第二家公司',
        position: '高级前端开发',
        content: '<ol><li>第二段新内容</li></ol>',
      }),
    })

    expect(JSON.parse(applied.moduleData.experience)).toEqual([
      originalItems[0],
      {
        company: '第二家公司',
        position: '高级前端开发',
        content: '<ol><li>第二段新内容</li></ol>',
      },
    ])
    expect(applied.pendingSuggestion.wasEnabled).toBe(true)

    const reverted = rejectSuggestionDraft(applied.modules, applied.moduleData, applied.pendingSuggestion)
    expect(JSON.parse(reverted.moduleData.experience)).toEqual(originalItems)
  })
})
