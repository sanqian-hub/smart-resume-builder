import { afterEach, describe, expect, test } from 'vitest'
import { render, screen } from '@testing-library/vue'
import { nextTick } from 'vue'
import ModuleSelector from '../../src/views/resume/editors/ModuleSelector.vue'

const modules = [
  { key: 'basic', label: '基本信息', enabled: true },
  { key: 'education', label: '教育经历', enabled: true },
]

function setViewportWidth(width) {
  Object.defineProperty(window, 'innerWidth', {
    configurable: true,
    writable: true,
    value: width,
  })
}

describe('模块选择器折叠按钮', () => {
  afterEach(() => {
    setViewportWidth(1280)
  })

  test('宽屏展开态显示收起文案按钮', async () => {
    setViewportWidth(1280)

    render(ModuleSelector, {
      props: { modelValue: modules },
    })

    await nextTick()

    expect(screen.getByRole('button', { name: '收起模块选择' })).toBeVisible()
  })

  test('窄屏自动折叠时显示展开文案按钮', async () => {
    setViewportWidth(900)

    render(ModuleSelector, {
      props: { modelValue: modules },
    })

    await nextTick()

    expect(screen.getByRole('button', { name: '展开模块选择' })).toBeVisible()
  })
})
