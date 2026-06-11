import { render } from '@testing-library/vue'
import { describe, expect, test } from 'vitest'
import { buildProofreadRichHtml } from '../../src/views/resume/proofreadHighlight'
import ClassicTemplate1 from '../../src/views/resume/templates/Classic/ClassicTemplate1.vue'
import CreativeTemplate1 from '../../src/views/resume/templates/Creative/CreativeTemplate1.vue'
import DualColumnTemplate1 from '../../src/views/resume/templates/DualColumn/DualColumnTemplate1.vue'
import DualColumnTemplate2 from '../../src/views/resume/templates/DualColumn/DualColumnTemplate2.vue'

describe('语法纠错富文本高亮', () => {
  test('ClassicTemplate1 highlights matched text inside experience rich html without breaking markup', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              {
                company: '某某科技有限公司',
                content: '<p>负责项目开发和设计，并且独立完成前后端</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-1',
            moduleType: 'experience',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '独立完成前后端',
          },
        ],
      },
    })

    const richContent = container.querySelector('.experienceListContent .rich-content')
    expect(richContent?.querySelector('p')).not.toBeNull()
    expect(richContent?.querySelector('.proofread-text-highlight')?.textContent).toBe('独立完成前后端')
  })

  test('ClassicTemplate1 rich text highlight keeps the same visible highlight styling as plain text highlights', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              {
                company: '某某科技有限公司',
                content: '<p>可用型</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-2',
            moduleType: 'experience',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    const highlight = container.querySelector('.experienceListContent .rich-content .proofread-text-highlight')
    expect(highlight).not.toBeNull()
    expect(highlight?.getAttribute('style')).toContain('background: rgba(255, 196, 61, 0.5)')
  })

  test('ClassicTemplate1 highlights rich text suggestions even when the original phrase is split by inline tags', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              {
                company: '某某科技有限公司',
                content: '<p>复杂表单场景的<strong>可用</strong>型需要修正。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-3',
            moduleType: 'experience',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    const highlight = container.querySelector('.experienceListContent .rich-content .proofread-text-highlight')
    expect(highlight?.textContent).toBe('可用型')
    expect(highlight?.querySelector('strong')?.textContent).toBe('可用')
  })

  test('ClassicTemplate1 highlights matched text inside project rich html', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'project',
            contentJson: JSON.stringify([
              {
                name: '智能简历生成系统',
                content: '<p>优化复杂表单场景的<strong>可用</strong>型和易用型。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-4',
            moduleType: 'project',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    const highlight = container.querySelector('.projectListContent .rich-content .proofread-text-highlight')
    expect(highlight?.textContent).toBe('可用型')
    expect(highlight?.querySelector('strong')?.textContent).toBe('可用')
  })

  test('ClassicTemplate1 highlights a whole rich-text list item even when only whitespace differs', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'project',
            contentJson: JSON.stringify([
              {
                name: '智能简历生成系统',
                content: '<ol><li>完成模板体系抽象，统一\n template props、主题色、字体、字号与行距配置，降低后续扩模板成本</li><li>实现分享管理与快照能力。</li></ol>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-whole-li-1',
            moduleType: 'project',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '完成模板体系抽象，统一 template props、主题色、字体、字号与行距配置，降低后续扩模板成本',
          },
        ],
      },
    })

    const highlight = container.querySelector('.projectListContent .rich-content .proofread-text-highlight')
    expect(highlight).not.toBeNull()
    expect(highlight?.textContent?.replace(/\s+/g, '')).toBe('完成模板体系抽象，统一templateprops、主题色、字体、字号与行距配置，降低后续扩模板成本')
  })

  test('ClassicTemplate1 highlights a rich-text substring inside one list item even when whitespace distribution differs', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'project',
            contentJson: JSON.stringify([
              {
                name: '智能简历生成系统',
                content: '<ol><li>完成模板体系抽象，统一 template props、主题色、字体、字号与行距配置，降低后续扩模板成本</li></ol>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-substring-space-1',
            moduleType: 'project',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '统一template props、主题色、字体、字号与行距配置，降低后续扩模板成本',
          },
        ],
      },
    })

    const highlight = container.querySelector('.projectListContent .rich-content .proofread-text-highlight')
    expect(highlight).not.toBeNull()
    expect(highlight?.textContent?.replace(/\s+/g, '')).toBe('统一templateprops、主题色、字体、字号与行距配置，降低后续扩模板成本')
  })



  test('ClassicTemplate1 still highlights duplicated typos across different rich-text modules when AI returns a global occurrenceIndex', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              {
                company: '某某科技有限公司',
                content: '<p>负责复杂表单体验的可用型打磨。</p>',
              },
            ]),
          },
          {
            moduleType: 'skill',
            contentJson: JSON.stringify([
              {
                content: '<p>擅长复杂表单的可用型优化。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-global-occurrence-1',
            moduleType: 'experience',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
          {
            id: 'proofread-rich-global-occurrence-2',
            moduleType: 'skill',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 1,
            original: '可用型',
          },
        ],
      },
    })

    expect(container.querySelector('.experienceListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
    expect(container.querySelector('.skillListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
  })

  test('ClassicTemplate1 highlights the same rich-text typo across skill and experience modules at the same time', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              {
                company: '某某科技有限公司',
                content: '<ol><li>负责复杂表单体验的一致性和可用型打磨。</li></ol>',
              },
            ]),
          },
          {
            moduleType: 'skill',
            contentJson: JSON.stringify([
              {
                content: '<p>擅长复杂表单的可用型优化。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-same-1',
            moduleType: 'experience',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
          {
            id: 'proofread-rich-same-2',
            moduleType: 'skill',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    expect(container.querySelector('.experienceListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
    expect(container.querySelector('.skillListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
  })

  test('ClassicTemplate1 highlights the remaining rich text fields across modules', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'education',
            contentJson: JSON.stringify([
              {
                school: '东南大学',
                description: '<p>主要课程：复杂系统场景的<strong>可用</strong>型研究。</p>',
              },
            ]),
          },
          {
            moduleType: 'skill',
            contentJson: JSON.stringify([
              {
                content: '<p>擅长复杂表单的<strong>可用</strong>型优化。</p>',
              },
            ]),
          },
          {
            moduleType: 'personalStrengths',
            contentJson: JSON.stringify({
              content: '<p>关注复杂业务场景下交互的一<strong>至</strong>性。</p>',
            }),
          },
          {
            moduleType: 'award',
            contentJson: JSON.stringify([
              {
                name: '优秀毕业生',
                content: '<p>在答辩中表现出很强的<strong>可用</strong>型分析能力。</p>',
              },
            ]),
          },
          {
            moduleType: 'portfolio',
            contentJson: JSON.stringify([
              {
                name: '交互案例集',
                content: '<p>总结了提升页面<strong>一</strong>至性的设计方法。</p>',
              },
            ]),
          },
          {
            moduleType: 'other',
            contentJson: JSON.stringify([
              {
                name: '校园组织',
                content: '<p>负责活动物料和现场体验的<strong>可用</strong>型复盘。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-rich-5',
            moduleType: 'education',
            itemIndex: 0,
            fieldPath: 'description',
            occurrenceIndex: 0,
            original: '可用型',
          },
          {
            id: 'proofread-rich-6',
            moduleType: 'skill',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
          {
            id: 'proofread-rich-7',
            moduleType: 'personalStrengths',
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '一至性',
          },
          {
            id: 'proofread-rich-8',
            moduleType: 'award',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
          {
            id: 'proofread-rich-9',
            moduleType: 'portfolio',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '一至性',
          },
          {
            id: 'proofread-rich-10',
            moduleType: 'other',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    expect(container.querySelector('.educationListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
    expect(container.querySelector('.skillListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
    expect(container.querySelector('.aboutMe .rich-content .proofread-text-highlight')?.textContent).toBe('一至性')
    expect(container.querySelector('.awardListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
    expect(container.querySelector('.portfolioListContent .rich-content .proofread-text-highlight')?.textContent).toBe('一至性')
    expect(container.querySelector('.otherExpListContent .rich-content .proofread-text-highlight')?.textContent).toBe('可用型')
  })

  test('other templates highlight representative rich text fields', () => {
    const creativeView = render(CreativeTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'project',
            contentJson: JSON.stringify([
              {
                name: '智能简历生成系统',
                content: '<p>优化复杂表单场景的<strong>可用</strong>型和易用型。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'creative-rich-1',
            moduleType: 'project',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    expect(creativeView.container.querySelector('.c-entry .c-rich .proofread-text-highlight')?.textContent).toBe('可用型')

    const dualOneView = render(DualColumnTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'personalStrengths',
            contentJson: JSON.stringify({
              content: '<p>关注复杂业务场景下交互的一<strong>至</strong>性。</p>',
            }),
          },
        ],
        proofreadHighlights: [
          {
            id: 'dual-one-rich-1',
            moduleType: 'personalStrengths',
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '一至性',
          },
        ],
      },
    })

    expect(dualOneView.container.querySelector('.dual-one__side-section .dual-one__rich .proofread-text-highlight')?.textContent).toBe('一至性')

    const dualTwoView = render(DualColumnTemplate2, {
      props: {
        contents: [
          {
            moduleType: 'award',
            contentJson: JSON.stringify([
              {
                name: '优秀毕业生',
                content: '<p>在答辩中表现出很强的<strong>可用</strong>型分析能力。</p>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'dual-two-rich-1',
            moduleType: 'award',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '可用型',
          },
        ],
      },
    })

    expect(dualTwoView.container.querySelector('.dual-two__side-entry .dual-two__rich .proofread-text-highlight')?.textContent).toBe('可用型')
  })

  test('ClassicTemplate1 highlights a proofread suggestion that spans two consecutive list items in the same rich text field', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'skill',
            contentJson: JSON.stringify([
              {
                content: '<ol><li>熟练使用vue3加速页面开发</li><li>熟悉使用Vue3怠发</li><li>熟悉React语法</li></ol>',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-cross-block-1',
            moduleType: 'skill',
            itemIndex: 0,
            fieldPath: 'content',
            occurrenceIndex: 0,
            original: '熟练使用vue3加速页面开发 熟悉使用Vue3怠发',
          },
        ],
      },
    })

    const highlights = container.querySelectorAll('.skillListContent .rich-content .proofread-text-highlight')
    expect(highlights).toHaveLength(2)
    expect(highlights[0]?.textContent).toBe('熟练使用vue3加速页面开发')
    expect(highlights[1]?.textContent).toBe('熟悉使用Vue3怠发')
  })

  test('buildProofreadRichHtml keeps multiple normalized matches inside one list item without Range overflow', () => {
    const html = '<ol><li>统一 template props、主题色、字体、字号与行距配置，降低后续扩展 模板 成本</li></ol>'
    const highlightedHtml = buildProofreadRichHtml(html, [
      {
        id: 'rich-multi-1',
        moduleType: 'project',
        itemIndex: 0,
        fieldPath: 'content',
        occurrenceIndex: 0,
        original: '统一 template props、主题色、字体、字号与行距配置，降低后续扩展模板成本',
      },
      {
        id: 'rich-multi-2',
        moduleType: 'project',
        itemIndex: 0,
        fieldPath: 'content',
        occurrenceIndex: 0,
        original: '扩展模板成本',
      },
    ])

    const container = document.createElement('div')
    container.innerHTML = highlightedHtml
    const highlights = container.querySelectorAll('.proofread-text-highlight')
    expect(highlights.length).toBeGreaterThan(0)
    expect(container.textContent).toContain('统一 template props、主题色、字体、字号与行距配置，降低后续扩展 模板 成本')
  })

  test('buildProofreadRichHtml does not crash when one rich-text list item receives both typo and whole-sentence matches', () => {
    const html = '<ol><li>优化复杂弹窗、加载反馈和边界交互，增强多步骤操作过程中的状态可感知性与一致性。</li></ol>'
    const highlightedHtml = buildProofreadRichHtml(html, [
      {
        id: 'rich-overlap-1',
        moduleType: 'project',
        itemIndex: 0,
        fieldPath: 'content',
        occurrenceIndex: 0,
        original: '一致性',
      },
      {
        id: 'rich-overlap-2',
        moduleType: 'project',
        itemIndex: 0,
        fieldPath: 'content',
        occurrenceIndex: 0,
        original: '优化复杂弹窗、加载反馈和边界交互，增强多步骤操作过程中的状态可感知性与一致性',
      },
    ])

    const container = document.createElement('div')
    container.innerHTML = highlightedHtml
    expect(container.querySelectorAll('.proofread-text-highlight').length).toBeGreaterThan(0)
    expect(container.textContent).toContain('状态可感知性与一致性')
  })
})
