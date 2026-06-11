import { describe, expect, test } from 'vitest'
import {
  getAllTemplateFamilyGroups,
  getTemplateFamilyTabs,
  sortTemplatesForDisplay,
  TEMPLATE_FAMILY_META,
} from '../../src/views/resume/templateRegistry'

describe('模板注册表分类派生', () => {
  test('提供固定顺序的模板家族 tab 和中文展示名', () => {
    expect(TEMPLATE_FAMILY_META.classic).toMatchObject({
      label: '经典模板',
      shortLabel: '经典',
      order: 1,
    })
    expect(TEMPLATE_FAMILY_META.creative).toMatchObject({
      label: '创意模板',
      shortLabel: '创意',
      order: 2,
    })
    expect(TEMPLATE_FAMILY_META.dual).toMatchObject({
      label: '双栏模板',
      shortLabel: '双栏',
      order: 3,
    })

    expect(getTemplateFamilyTabs()).toEqual([
      { key: 'all', label: '全部' },
      { key: 'classic', label: '经典' },
      { key: 'creative', label: '创意' },
      { key: 'dual', label: '双栏' },
    ])
  })

  test('模板展示顺序按 featured 优先，再按 order 升序，最后按 id 兜底', () => {
    const ordered = sortTemplatesForDisplay([
      { id: 'dual-3', family: 'dual', label: '双栏模板03', featured: false, order: 1 },
      { id: 'dual-2', family: 'dual', label: '双栏模板02', featured: true, order: 2 },
      { id: 'dual-4', family: 'dual', label: '双栏模板04', featured: true, order: 2 },
      { id: 'dual-1', family: 'dual', label: '双栏模板01', featured: true, order: 1 },
    ])

    expect(ordered.map(item => item.id)).toEqual([
      'dual-1',
      'dual-2',
      'dual-4',
      'dual-3',
    ])
  })

  test('全部视图按模板家族固定顺序分组，并复用组内排序结果', () => {
    const groups = getAllTemplateFamilyGroups([
      { id: 'dual-2', family: 'dual', label: '双栏模板02', featured: false, order: 2 },
      { id: 'classic-2', family: 'classic', label: '经典模板02', featured: false, order: 2 },
      { id: 'creative-1', family: 'creative', label: '创意模板01', featured: true, order: 1 },
      { id: 'classic-1', family: 'classic', label: '经典模板01', featured: true, order: 1 },
      { id: 'dual-1', family: 'dual', label: '双栏模板01', featured: true, order: 1 },
    ])

    expect(groups.map(group => group.family)).toEqual(['classic', 'creative', 'dual'])
    expect(groups.map(group => group.label)).toEqual(['经典模板', '创意模板', '双栏模板'])
    expect(groups[0].templates.map(item => item.id)).toEqual(['classic-1', 'classic-2'])
    expect(groups[1].templates.map(item => item.id)).toEqual(['creative-1'])
    expect(groups[2].templates.map(item => item.id)).toEqual(['dual-1', 'dual-2'])
  })
})
