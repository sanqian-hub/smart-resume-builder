import { describe, expect, test } from 'vitest'
import { render, screen } from '@testing-library/vue'
import DualColumnTemplate1 from '../../src/views/resume/templates/DualColumn/DualColumnTemplate1.vue'

function makeContents() {
  return [
    {
      moduleType: 'basic',
      contentJson: JSON.stringify({
        name: '夜神月',
        jobTitle: '前端开发工程师',
        phone: '13910421987',
      }),
    },
    {
        moduleType: 'personalStrengths',
      contentJson: JSON.stringify({ content: '<p>总结内容</p>' }),
    },
    {
      moduleType: 'award',
      contentJson: JSON.stringify([{ name: '校级奖学金' }]),
    },
    {
      moduleType: 'experience',
      contentJson: JSON.stringify([{ company: '某科技有限公司', position: '前端开发工程师' }]),
    },
    {
      moduleType: 'project',
      contentJson: JSON.stringify([{ name: '智能简历生成系统' }]),
    },
    {
      moduleType: 'portfolio',
      contentJson: JSON.stringify([{ name: '个人技术博客' }]),
    },
    {
      moduleType: 'education',
      contentJson: JSON.stringify([{ school: '东应大学' }]),
    },
    {
      moduleType: 'skill',
      contentJson: JSON.stringify([{ content: '<ul><li>Vue 3</li></ul>' }]),
    },
    {
      moduleType: 'other',
      contentJson: JSON.stringify([{ name: '开源社区贡献者' }]),
    },
  ]
}

describe('双栏模板01模块标题', () => {
  test('使用中文模块名而不是英文模块名', () => {
    render(DualColumnTemplate1, {
      props: {
        contents: makeContents(),
        themeColor: '#d8892f',
      },
    })

    expect(screen.getByText('基本信息')).toBeInTheDocument()
    expect(screen.getByText('个人优势')).toBeInTheDocument()
    expect(screen.getByText('荣誉奖项')).toBeInTheDocument()
    expect(screen.getByText('工作经历')).toBeInTheDocument()
    expect(screen.getByText('项目经历')).toBeInTheDocument()
    expect(screen.getByText('个人作品')).toBeInTheDocument()
    expect(screen.getByText('教育经历')).toBeInTheDocument()
    expect(screen.getByText('专业技能')).toBeInTheDocument()
    expect(screen.getByText('其他经历')).toBeInTheDocument()
  })
})
