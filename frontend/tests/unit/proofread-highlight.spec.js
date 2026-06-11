import { describe, expect, test } from 'vitest'
import { render } from '@testing-library/vue'
import ClassicTemplate1 from '../../src/views/resume/templates/Classic/ClassicTemplate1.vue'
import CreativeTemplate1 from '../../src/views/resume/templates/Creative/CreativeTemplate1.vue'
import DualColumnTemplate1 from '../../src/views/resume/templates/DualColumn/DualColumnTemplate1.vue'
import DualColumnTemplate2 from '../../src/views/resume/templates/DualColumn/DualColumnTemplate2.vue'
import { buildProofreadSegments } from '../../src/views/resume/proofreadHighlight'

describe('语法纠错结构化文本高亮', () => {
  test('buildProofreadSegments highlights the requested occurrence only', () => {
    const segments = buildProofreadSegments('可感知与一直性，可感知与一直性', [
      {
        id: 'proofread-1',
        original: '可感知与一直性',
        occurrenceIndex: 1,
      },
    ])

    expect(segments).toEqual([
      { text: '可感知与一直性，', highlighted: false, highlightId: null },
      { text: '可感知与一直性', highlighted: true, highlightId: 'proofread-1' },
    ])
  })

  test('ClassicTemplate1 highlights only the targeted item field', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'basic',
            contentJson: JSON.stringify({
              name: 'Alex Chen',
            }),
          },
          {
            moduleType: 'education',
            contentJson: JSON.stringify([
              { school: '东应大学' },
              { school: '东应大学' },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-2',
            moduleType: 'education',
            itemIndex: 1,
            fieldPath: 'school',
            occurrenceIndex: 0,
            original: '东应大学',
          },
        ],
      },
    })

    const schools = Array.from(container.querySelectorAll('.school'))
    expect(schools).toHaveLength(2)
    expect(schools[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(schools[1].querySelector('.proofread-text-highlight')?.textContent).toBe('东应大学')
  })

  test('ClassicTemplate1 highlights targeted structured fields in experience and project modules', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              {
                company: '某某科技有限公司',
                department: '技术部',
                position: '前端开发实习生',
              },
              {
                company: '某某科技有限公司',
                department: '技术部',
                position: '前端开发实习生',
              },
            ]),
          },
          {
            moduleType: 'project',
            contentJson: JSON.stringify([
              {
                name: '智能简历生成系统',
                role: '前端负责人',
                city: '北京',
              },
              {
                name: '智能简历生成系统',
                role: '前端负责人',
                city: '北京',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-3',
            moduleType: 'experience',
            itemIndex: 1,
            fieldPath: 'company',
            occurrenceIndex: 0,
            original: '某某科技有限公司',
          },
          {
            id: 'proofread-4',
            moduleType: 'project',
            itemIndex: 1,
            fieldPath: 'name',
            occurrenceIndex: 0,
            original: '智能简历生成系统',
          },
        ],
      },
    })

    const companies = Array.from(container.querySelectorAll('.company'))
    expect(companies).toHaveLength(2)
    expect(companies[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(companies[1].querySelector('.proofread-text-highlight')?.textContent).toBe('某某科技有限公司')

    const projectNames = Array.from(container.querySelectorAll('.projectName'))
    expect(projectNames).toHaveLength(2)
    expect(projectNames[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(projectNames[1].querySelector('.proofread-text-highlight')?.textContent).toBe('智能简历生成系统')
  })

  test('ClassicTemplate1 highlights targeted structured fields in award and portfolio modules', () => {
    const { container } = render(ClassicTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'award',
            contentJson: JSON.stringify([
              { name: '全国大学生数学建模竞赛' },
              { name: '全国大学生数学建模竞赛' },
            ]),
          },
          {
            moduleType: 'portfolio',
            contentJson: JSON.stringify([
              { name: '个人技术博客' },
              { name: '个人技术博客' },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-5',
            moduleType: 'award',
            itemIndex: 1,
            fieldPath: 'name',
            occurrenceIndex: 0,
            original: '全国大学生数学建模竞赛',
          },
          {
            id: 'proofread-6',
            moduleType: 'portfolio',
            itemIndex: 1,
            fieldPath: 'name',
            occurrenceIndex: 0,
            original: '个人技术博客',
          },
        ],
      },
    })

    const awards = Array.from(container.querySelectorAll('.awardName'))
    expect(awards).toHaveLength(2)
    expect(awards[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(awards[1].querySelector('.proofread-text-highlight')?.textContent).toBe('全国大学生数学建模竞赛')

    const portfolios = Array.from(container.querySelectorAll('.portfolioName'))
    expect(portfolios).toHaveLength(2)
    expect(portfolios[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(portfolios[1].querySelector('.proofread-text-highlight')?.textContent).toBe('个人技术博客')
  })

  test('CreativeTemplate1 highlights targeted structured text fields', () => {
    const { container } = render(CreativeTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'experience',
            contentJson: JSON.stringify([
              { company: '某某科技有限公司', department: '技术部' },
              { company: '某某科技有限公司', department: '技术部' },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-7',
            moduleType: 'experience',
            itemIndex: 1,
            fieldPath: 'company',
            occurrenceIndex: 0,
            original: '某某科技有限公司',
          },
        ],
      },
    })

    const titles = Array.from(container.querySelectorAll('.c-entry-title'))
    expect(titles).toHaveLength(2)
    expect(titles[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(titles[1].querySelector('.proofread-text-highlight')?.textContent).toBe('某某科技有限公司')
  })

  test('CreativeTemplate1 highlights basic location inside contact items', () => {
    const { container } = render(CreativeTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'basic',
            contentJson: JSON.stringify({
              name: 'Elovara',
              location: '背景',
            }),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-7b',
            moduleType: 'basic',
            fieldPath: 'location',
            occurrenceIndex: 0,
            original: '背景',
          },
        ],
      },
    })

    const items = Array.from(container.querySelectorAll('.c-contact-item'))
    expect(items).toHaveLength(1)
    expect(items[0].querySelector('.proofread-text-highlight')?.textContent).toBe('背景')
  })

  test('DualColumnTemplate1 highlights targeted structured text fields', () => {
    const { container } = render(DualColumnTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'project',
            contentJson: JSON.stringify([
              { name: '智能简历生成系统' },
              { name: '智能简历生成系统' },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-8',
            moduleType: 'project',
            itemIndex: 1,
            fieldPath: 'name',
            occurrenceIndex: 0,
            original: '智能简历生成系统',
          },
        ],
      },
    })

    const names = Array.from(container.querySelectorAll('.dual-one__entry-name'))
    expect(names).toHaveLength(2)
    expect(names[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(names[1].querySelector('.proofread-text-highlight')?.textContent).toBe('智能简历生成系统')
  })

  test('DualColumnTemplate1 highlights basic location inside contact items', () => {
    const { container } = render(DualColumnTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'basic',
            contentJson: JSON.stringify({
              name: 'Elovara',
              location: '背景',
            }),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-8b',
            moduleType: 'basic',
            fieldPath: 'location',
            occurrenceIndex: 0,
            original: '背景',
          },
        ],
      },
    })

    const items = Array.from(container.querySelectorAll('.dual-one__contact-item'))
    expect(items).toHaveLength(1)
    expect(items[0].querySelector('.proofread-text-highlight')?.textContent).toBe('背景')
  })

  test('DualColumnTemplate2 highlights targeted structured text fields', () => {
    const { container } = render(DualColumnTemplate2, {
      props: {
        contents: [
          {
            moduleType: 'award',
            contentJson: JSON.stringify([
              { name: '全国大学生数学建模竞赛' },
              { name: '全国大学生数学建模竞赛' },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-9',
            moduleType: 'award',
            itemIndex: 1,
            fieldPath: 'name',
            occurrenceIndex: 0,
            original: '全国大学生数学建模竞赛',
          },
        ],
      },
    })

    const names = Array.from(container.querySelectorAll('.dual-two__side-name'))
    expect(names).toHaveLength(2)
    expect(names[0].querySelector('.proofread-text-highlight')).toBeNull()
    expect(names[1].querySelector('.proofread-text-highlight')?.textContent).toBe('全国大学生数学建模竞赛')
  })

  test('DualColumnTemplate2 highlights basic location inside hero contact chips', () => {
    const { container } = render(DualColumnTemplate2, {
      props: {
        contents: [
          {
            moduleType: 'basic',
            contentJson: JSON.stringify({
              name: 'Elovara',
              location: '背景',
            }),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-10',
            moduleType: 'basic',
            fieldPath: 'location',
            occurrenceIndex: 0,
            original: '背景',
          },
        ],
      },
    })

    const chips = Array.from(container.querySelectorAll('.dual-two__contact-chip'))
    expect(chips).toHaveLength(1)
    expect(chips[0].querySelector('.proofread-text-highlight')?.textContent).toBe('背景')
  })

  test('DualColumnTemplate2 still highlights education school when AI returns a generic fieldPath', () => {
    const { container } = render(DualColumnTemplate2, {
      props: {
        contents: [
          {
            moduleType: 'education',
            contentJson: JSON.stringify([
              {
                degree: '本科',
                major: '信息科学',
                school: '东应大雪',
              },
            ]),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-10b',
            moduleType: 'education',
            itemIndex: 0,
            fieldPath: 'name',
            occurrenceIndex: 0,
            original: '东应大雪',
          },
        ],
      },
    })

    const school = container.querySelector('.dual-two__side-school')
    expect(school?.querySelector('.proofread-text-highlight')?.textContent).toBe('东应大雪')
  })

  test('CreativeTemplate1 still highlights basic location when AI returns basic-prefixed fieldPath and itemIndex 0', () => {
    const { container } = render(CreativeTemplate1, {
      props: {
        contents: [
          {
            moduleType: 'basic',
            contentJson: JSON.stringify({
              name: 'Elovara',
              location: '背景',
            }),
          },
        ],
        proofreadHighlights: [
          {
            id: 'proofread-11',
            moduleType: 'basic',
            itemIndex: 0,
            fieldPath: 'basic.location',
            occurrenceIndex: 0,
            original: '背景',
          },
        ],
      },
    })

    const items = Array.from(container.querySelectorAll('.c-contact-item'))
    expect(items).toHaveLength(1)
    expect(items[0].querySelector('.proofread-text-highlight')?.textContent).toBe('背景')
  })
})
