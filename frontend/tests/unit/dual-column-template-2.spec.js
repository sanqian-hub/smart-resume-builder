import { describe, expect, test } from 'vitest'
import { render, screen } from '@testing-library/vue'
import DualColumnTemplate2 from '../../src/views/resume/templates/DualColumn/DualColumnTemplate2.vue'
import { TEMPLATE_COMPONENTS, TEMPLATE_LABELS } from '../../src/views/resume/templateRegistry'

function makeContents() {
  return [
    {
      moduleType: 'basic',
      contentJson: JSON.stringify({
        name: '亚历山大',
        jobTitle: '产品经理',
        phone: '123-0000-000',
        email: 'alex@example.com',
        location: '旧金山',
        avatar: 'https://example.com/avatar.jpg',
      }),
    },
    {
      moduleType: 'experience',
      contentJson: JSON.stringify([
        {
          company: 'Rover Games',
          position: '产品经理',
          startDate: '2019/01',
          endDate: '2020/01',
          city: 'San Francisco, CA',
          content: '<ul><li>负责游戏平台增长</li></ul>',
        },
      ]),
    },
    {
      moduleType: 'education',
      contentJson: JSON.stringify([
        {
          school: '加州大学伯克利分校',
          major: '工业工程',
          degree: '硕士',
          startDate: '2013/01',
          endDate: '2015/01',
          city: 'Berkeley, CA',
        },
      ]),
    },
    {
      moduleType: 'skill',
      contentJson: JSON.stringify([
        { content: '<p>Scrum</p>' },
        { content: '<p>Product Development</p>' },
      ]),
    },
    {
        moduleType: 'personalStrengths',
      contentJson: JSON.stringify({ content: '<p>擅长跨团队推进复杂项目。</p>' }),
    },
  ]
}

function makeSkillListContents() {
  const contents = makeContents()
  return contents.map(item => {
    if (item.moduleType !== 'skill') return item
    return {
      ...item,
      contentJson: JSON.stringify([
        {
          content: '<ul><li>Vue 3</li><li>TypeScript</li></ul>',
        },
      ]),
    }
  })
}

describe('双栏模板02', () => {
  test('注册表已接入 dual-2 模板', () => {
    expect(TEMPLATE_LABELS['dual-2']).toBe('双栏模板02')
    expect(TEMPLATE_COMPONENTS['dual-2']).toBeTruthy()
  })

  test('渲染参考图对应的双栏骨架和核心模块', () => {
    render(DualColumnTemplate2, {
      props: {
        contents: makeContents(),
        themeColor: '#ff7439',
      },
    })

    expect(screen.getByText('亚历山大')).toBeInTheDocument()
    expect(screen.getAllByText('产品经理').length).toBeGreaterThan(0)
    expect(screen.getByText('123-0000-000')).toBeInTheDocument()
    expect(screen.getByText('alex@example.com')).toBeInTheDocument()
    expect(screen.getByText('工作经历')).toBeInTheDocument()
    expect(screen.getByText('教育经历')).toBeInTheDocument()
    expect(screen.getByText('专业技能')).toBeInTheDocument()
    expect(screen.getByText('个人优势')).toBeInTheDocument()
    expect(screen.getByText('Rover Games')).toBeInTheDocument()
    expect(screen.getByText('加州大学伯克利分校')).toBeInTheDocument()
    expect(screen.getByText('Scrum')).toBeInTheDocument()
  })

  test('专业技能文字跟随 rich style 配置', () => {
    render(DualColumnTemplate2, {
      props: {
        contents: makeContents(),
        richFontFamily: '"Times New Roman", serif',
        richFontSize: 16,
        richLineHeight: 2,
      },
    })

    const skillTag = screen.getByText('Scrum').closest('.dual-two__rich--skill')
    expect(skillTag).not.toBeNull()
    expect(skillTag).toHaveStyle({
      fontFamily: '"Times New Roman", serif',
      fontSize: '16px',
      lineHeight: '2',
    })
  })

  test('专业技能支持无序列表内容', () => {
    render(DualColumnTemplate2, {
      props: {
        contents: makeSkillListContents(),
      },
    })

    expect(screen.getByText('Vue 3')).toBeInTheDocument()
    expect(screen.getByText('TypeScript')).toBeInTheDocument()
  })

  test('教育经历中的学历和专业使用中点分隔而不是逗号', () => {
    render(DualColumnTemplate2, {
      props: {
        contents: makeContents(),
      },
    })

    expect(screen.getByText('硕士 · 工业工程')).toBeInTheDocument()
    expect(screen.queryByText('硕士，工业工程')).not.toBeInTheDocument()
  })
})
