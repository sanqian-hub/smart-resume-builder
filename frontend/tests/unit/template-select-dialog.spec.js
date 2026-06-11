import { defineComponent, h, markRaw } from 'vue'
import { describe, expect, test } from 'vitest'
import { fireEvent, render, screen, within } from '@testing-library/vue'
import TemplateSelectDialog from '../../src/views/resume/components/TemplateSelectDialog.vue'

const PreviewStub = defineComponent({
  name: 'PreviewStub',
  props: {
    label: { type: String, default: '预览' },
  },
  setup(props) {
    return () => h('div', { class: 'preview-stub' }, props.label)
  },
})
const RawPreviewStub = markRaw(PreviewStub)

function makeOptions() {
  return [
    {
      id: 'classic-2',
      label: '经典模板02',
      family: 'classic',
      description: '经典次选',
      featured: false,
      order: 2,
      component: RawPreviewStub,
    },
    {
      id: 'classic-1',
      label: '经典模板01',
      family: 'classic',
      description: '经典首选',
      featured: true,
      order: 1,
      component: RawPreviewStub,
    },
    {
      id: 'creative-1',
      label: '创意模板01',
      family: 'creative',
      description: '创意模板',
      featured: false,
      order: 1,
      component: RawPreviewStub,
    },
  ]
}

describe('模板选择弹窗', () => {
  test('默认展示全部 tab，并按家族分组和推荐顺序展示模板', () => {
    render(TemplateSelectDialog, {
      props: {
        visible: true,
        selectedTemplate: 'classic-2',
        templateOptions: makeOptions(),
      },
    })

    const allTab = screen.getByRole('tab', { name: '全部' })
    const tablist = screen.getByRole('tablist', { name: '模板分类' })
    expect(tablist).toHaveClass('template-select-tabs--compact')
    expect(allTab).toHaveAttribute('aria-selected', 'true')
    expect(allTab).toHaveClass('template-select-tab--action')
    expect(allTab).toHaveClass('on')
    expect(screen.getByRole('tab', { name: '经典' })).toHaveClass('template-select-tab--action')
    expect(screen.getByRole('tab', { name: '创意' })).toHaveClass('template-select-tab--action')
    expect(screen.getByRole('tab', { name: '双栏' })).toHaveClass('template-select-tab--action')

    const classicGroup = screen.getByRole('heading', { name: '经典模板' }).closest('section')
    const classicCards = within(classicGroup).getAllByRole('button', { name: /经典模板0[12]/ })
    expect(classicCards.map(card => card.getAttribute('aria-label'))).toEqual([
      '经典模板01',
      '经典模板02',
    ])

    expect(within(classicCards[0]).getByText('推荐')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: '创意模板' })).toBeInTheDocument()
  })

  test('切换到空分类时显示暂无模板提示', async () => {
    render(TemplateSelectDialog, {
      props: {
        visible: true,
        selectedTemplate: 'classic-1',
        templateOptions: makeOptions(),
      },
    })

    await fireEvent.click(screen.getByRole('tab', { name: '双栏' }))

    expect(screen.getByText('暂时没有相关模板')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: '经典模板' })).not.toBeInTheDocument()
  })
})
