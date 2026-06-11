import { describe, expect, test } from 'vitest'
import fs from 'node:fs'
import path from 'node:path'

describe('startup loader fullscreen coverage', () => {
  test('boot loader container itself should not be vertically translated off the viewport', () => {
    const indexHtml = fs.readFileSync(path.resolve(process.cwd(), 'index.html'), 'utf8')
    const loaderStyleBlock = indexHtml.match(/#app-loader\{([^}]*)\}/)?.[1] || ''

    expect(loaderStyleBlock).toContain('position:fixed')
    expect(loaderStyleBlock).toContain('inset:0')
    expect(loaderStyleBlock).not.toContain('transform:translateY(-120px)')
  })
})
