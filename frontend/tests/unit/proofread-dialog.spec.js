import { fireEvent, render, screen } from '@testing-library/vue'
import { describe, expect, test } from 'vitest'
import { defineComponent, ref } from 'vue'
import ProofreadDialog from '../../src/views/resume/editors/ProofreadDialog.vue'
import { clampProofreadDialogPosition, getCenteredProofreadDialogPosition } from '../../src/views/resume/editors/proofreadDialogPosition'

describe('语法纠错弹窗', () => {
  test('有纠错结果时展示建议卡片和应用操作', async () => {
    const proofreadView = render(ProofreadDialog, {
      props: {
        visible: true,
        resumeId: 1,
        moduleData: {},
        result: {
          summary: '发现 1 处可优化',
          items: [
            {
              id: 'item-1',
              fieldLabel: '项目经历',
              original: '负责项目开发和设计',
              suggestion: '负责项目设计与开发',
              reason: '语序更自然，表达更紧凑',
            },
          ],
        },
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(await screen.findByText('共发现1处可优化')).toBeVisible()
    expect(screen.getByText('项目经历')).toBeVisible()
    expect(screen.getByText('负责项目开发和设计')).toBeVisible()
    expect(screen.getByText('负责项目设计与开发')).toBeVisible()
    expect(screen.getByText('语序更自然，表达更紧凑')).toBeVisible()
    expect(screen.getByRole('button', { name: '应用' })).toBeVisible()
    expect(screen.getByRole('button', { name: '忽略' })).toBeVisible()

    const applyButton = proofreadView.getByRole('button', { name: '应用' })
    const ignoreButton = proofreadView.getByRole('button', { name: '忽略' })

    expect(getComputedStyle(applyButton).height).toBe(getComputedStyle(ignoreButton).height)
    expect(proofreadView.container.querySelector('.proofread-dialog')).not.toHaveClass('is-dragging')
  })

  test('点击关闭按钮时不会误进入拖拽态且会关闭弹窗', async () => {
    const Wrapper = defineComponent({
      components: { ProofreadDialog },
      setup() {
        const visible = ref(true)
        return { visible }
      },
      template: `
        <ProofreadDialog
          :visible="visible"
          :resume-id="1"
          :module-data="{}"
          :result="{ summary: '发现 1 处可优化', items: [] }"
          @close="visible = false"
        />
      `,
    })

    const proofreadView = render(Wrapper, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const closeButton = screen.getByRole('button')
    await fireEvent.mouseDown(closeButton)
    expect(proofreadView.container.querySelector('.proofread-dialog')).not.toHaveClass('is-dragging')

    await fireEvent.click(closeButton)
    expect(screen.queryByText('语法纠错')).not.toBeInTheDocument()
  })

  test('同字段里更大的整句建议包含错别字建议时，弹窗只保留细粒度建议', async () => {
    render(ProofreadDialog, {
      props: {
        visible: true,
        resumeId: 1,
        moduleData: {},
        result: {
          summary: '发现 2 处可优化',
          items: [
            {
              id: 'typo-1',
              moduleType: 'project',
              itemIndex: 0,
              fieldPath: 'content',
              fieldLabel: '项目经历',
              type: 'typo',
              original: '一直性',
              suggestion: '一致性',
              reason: '错别字',
            },
            {
              id: 'style-1',
              moduleType: 'project',
              itemIndex: 0,
              fieldPath: 'content',
              fieldLabel: '项目经历',
              type: 'style',
              original: '增强多步骤操作过程中的状态可感知性与一直性。',
              suggestion: '增强多步骤操作过程中的状态可感知性与一致性。',
              reason: '整句表达优化',
            },
          ],
        },
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(await screen.findByText('一直性')).toBeVisible()
    expect(screen.queryByText('增强多步骤操作过程中的状态可感知性与一直性。')).not.toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: '应用' })).toHaveLength(1)
  })

  test('过滤重叠建议后，顶部 summary 的数量会和实际卡片数保持一致', async () => {
    render(ProofreadDialog, {
      props: {
        visible: true,
        resumeId: 1,
        moduleData: {},
        result: {
          summary: '共发现6处可优化问题，包括错别字、表达冗余和语序调整，已按优先级排序。',
          items: [
            {
              id: 'basic-1',
              moduleType: 'basic',
              fieldPath: 'location',
              fieldLabel: '基本信息',
              type: 'typo',
              original: '背景',
              suggestion: '北京',
              reason: '错别字',
            },
            {
              id: 'typo-1',
              moduleType: 'project',
              itemIndex: 0,
              fieldPath: 'content',
              fieldLabel: '项目经历',
              type: 'typo',
              original: '一直性',
              suggestion: '一致性',
              reason: '错别字',
            },
            {
              id: 'style-1',
              moduleType: 'project',
              itemIndex: 0,
              fieldPath: 'content',
              fieldLabel: '项目经历',
              type: 'style',
              original: '增强多步骤操作过程中的状态可感知性与一直性。',
              suggestion: '增强多步骤操作过程中的状态可感知性与一致性。',
              reason: '整句表达优化',
            },
            {
              id: 'skill-1',
              moduleType: 'skill',
              itemIndex: 0,
              fieldPath: 'content',
              fieldLabel: '专业技能',
              type: 'typo',
              original: '开阀',
              suggestion: '开发',
              reason: '错别字',
            },
          ],
        },
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(await screen.findByText('共发现3处可优化问题，包括错别字、表达冗余和语序调整，已按优先级排序。')).toBeVisible()
    expect(screen.getByText('共发现 3 处可优化内容')).toBeVisible()
  })

  test('拖拽边界和初始位置都基于实际渲染尺寸计算', () => {
    expect(getCenteredProofreadDialogPosition(1400, 900, 620, 500)).toEqual({
      x: 390,
      y: 200,
    })

    expect(clampProofreadDialogPosition(9999, 9999, 1400, 900, 620, 500)).toEqual({
      x: 780,
      y: 400,
    })
  })
})
