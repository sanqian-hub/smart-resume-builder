import { describe, expect, it } from 'vitest'
import { replaceProofreadContent } from '../../src/views/resume/proofreadApply'

describe('语法纠错应用修改', () => {
  it('applies a suggestion that spans two consecutive rich-text list items inside the same field', () => {
    const raw = JSON.stringify([
      {
        content: '<ol><li>熟练使用vue3加速页面开发</li><li>熟悉使用Vue3怠发</li><li>熟悉React语法</li></ol>',
      },
    ])

    const next = replaceProofreadContent(
      raw,
      '熟练使用vue3加速页面开发 熟悉使用Vue3怠发',
      '熟练使用 Vue 3 加速页面开发，熟悉 Vue 3 语法',
    )

    expect(next).not.toBe(raw)
    expect(next).toContain('熟练使用 Vue 3 加速页面开发，熟悉 Vue 3 语法')
    expect(next).not.toContain('熟悉使用Vue3怠发')
  })

  it('applies a suggestion for one whole rich-text list item when only whitespace differs', () => {
    const raw = JSON.stringify([
      {
        content: '<ol><li>完成模板体系抽象，统一\n template props、主题色、字体、字号与行距配置，降低后续扩模板成本</li><li>实现分享管理与快照能力。</li></ol>',
      },
    ])

    const next = replaceProofreadContent(
      raw,
      '完成模板体系抽象，统一 template props、主题色、字体、字号与行距配置，降低后续扩模板成本',
      '完成模板体系抽象，统一 template props、主题色、字体、字号与行距配置，降低后续扩展模板成本',
    )

    expect(next).not.toBe(raw)
    expect(next).toContain('完成模板体系抽象，统一 template props、主题色、字体、字号与行距配置，降低后续扩展模板成本')
    expect(next).not.toContain('降低后续扩模板成本')
  })

  it('applies a suggestion for a rich-text substring inside one list item when whitespace distribution differs', () => {
    const raw = JSON.stringify([
      {
        content: '<ol><li>完成模板体系抽象，统一 template props、主题色、字体、字号与行距配置，降低后续扩模板成本</li></ol>',
      },
    ])

    const next = replaceProofreadContent(
      raw,
      '统一template props、主题色、字体、字号与行距配置，降低后续扩模板成本',
      '统一 template props、主题色、字体、字号与行距配置，降低后续扩展模板成本',
    )

    expect(next).not.toBe(raw)
    expect(next).toContain('统一 template props、主题色、字体、字号与行距配置，降低后续扩展模板成本')
    expect(next).not.toContain('降低后续扩模板成本')
  })
})
