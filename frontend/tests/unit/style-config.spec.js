import { describe, expect, test } from 'vitest'

import {
  readStyleConfig,
  readSnapshotPresentation,
  stripLegacyStyleFieldsFromBasic,
} from '../../src/views/resume/styleConfig'

describe('styleConfig 兼容迁移', () => {
  test('优先读取 styleConfig 中的新结构字段', () => {
    const result = readStyleConfig(
      JSON.stringify({
        themeColor: '#123456',
        richFontSize: 16,
        richFontFamily: 'Inter',
        richLineHeight: 2,
      }),
      JSON.stringify({
        themeColor: '#abcdef',
        _richFontSize: 14,
        _richFontFamily: 'Old Font',
        _richLineHeight: 1.7,
      }),
    )

    expect(result).toEqual({
      themeColor: '#123456',
      richFontSize: 16,
      richFontFamily: 'Inter',
      richLineHeight: 2,
    })
  })

  test('styleConfig 缺失时回退读取 basic 中的旧字段', () => {
    const result = readStyleConfig(
      '',
      JSON.stringify({
        themeColor: '#0d9488',
        _richFontSize: 14,
        _richFontFamily: "'Noto Sans SC', sans-serif",
        _richLineHeight: 1.7,
      }),
    )

    expect(result).toEqual({
      themeColor: '#0d9488',
      richFontSize: 14,
      richFontFamily: "'Noto Sans SC', sans-serif",
      richLineHeight: 1.7,
    })
  })

  test('保存前会从 basic 中清理旧的全局样式字段', () => {
    const basicJson = JSON.stringify({
      name: '三千',
      themeColor: '#0d9488',
      _richFontSize: 14,
      _richFontFamily: "'Noto Sans SC', sans-serif",
      _richLineHeight: 1.7,
      avatarAlign: false,
    })

    expect(JSON.parse(stripLegacyStyleFieldsFromBasic(basicJson))).toEqual({
      name: '三千',
      avatarAlign: false,
    })
  })

  test('历史快照预览优先读取顶层 styleConfig 和 template', () => {
    const result = readSnapshotPresentation({
      template: 'creative',
      styleConfig: JSON.stringify({
        themeColor: '#10b981',
        richFontSize: 15,
        richFontFamily: 'Test Font',
        richLineHeight: 1.9,
      }),
      contents: [
        {
          moduleType: 'basic',
          contentJson: JSON.stringify({
            themeColor: '#abcdef',
            _richFontSize: 12,
            _richFontFamily: 'Old Font',
            _richLineHeight: 1.5,
          }),
        },
      ],
    })

    expect(result).toEqual({
      template: 'creative',
      themeColor: '#10b981',
      richFontSize: 15,
      richFontFamily: 'Test Font',
      richLineHeight: 1.9,
    })
  })
})
