import { describe, expect, test } from 'vitest'
import { render, screen } from '@testing-library/vue'
import { nextTick } from 'vue'
import RichTextEditor from '../../src/views/resume/editors/RichTextEditor.vue'

function selectNodeContents(node) {
  const range = document.createRange()
  range.selectNodeContents(node)
  const selection = window.getSelection()
  selection.removeAllRanges()
  selection.addRange(range)
  document.dispatchEvent(new Event('selectionchange'))
}

describe('富文本工具栏激活态', () => {
  test('当前选中加粗文本时，加粗按钮应高亮', async () => {
    render(RichTextEditor, {
      props: {
        modelValue: '<p><strong>加粗文本</strong><span>普通文本</span></p>',
      },
    })

    await nextTick()

    const content = document.querySelector('.rte-content')
    const boldText = content.querySelector('strong')
    selectNodeContents(boldText)

    await nextTick()

    expect(screen.getByTitle('加粗')).toHaveClass('on')
  })

  test('当前选中普通文本时，加粗按钮不应因为编辑器其他位置有加粗文本而常亮', async () => {
    render(RichTextEditor, {
      props: {
        modelValue: '<p><strong>加粗文本</strong><span>普通文本</span></p>',
      },
    })

    await nextTick()

    const content = document.querySelector('.rte-content')
    const plainText = content.querySelector('span')
    selectNodeContents(plainText)

    await nextTick()

    expect(screen.getByTitle('加粗')).not.toHaveClass('on')
  })
})
