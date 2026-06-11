import { describe, expect, test } from 'vitest'
import { render, screen } from '@testing-library/vue'
import RecordFooter from '../../src/components/RecordFooter.vue'

describe('备案页脚', () => {
  test('展示 ICP 备案链接、公网安备链接与警徽图片', () => {
    render(RecordFooter)

    const icpLink = screen.getByRole('link', { name: '粤ICP备2026028310号-2' })
    const publicSecurityLink = screen.getByRole('link', { name: /粤公网安备44088202000096号/ })
    const icon = screen.getByAltText('公安网备')

    expect(icpLink).toHaveAttribute('href', 'https://beian.miit.gov.cn/')
    expect(publicSecurityLink).toHaveAttribute('href', 'https://beian.mps.gov.cn/#/query/webSearch?code=44088202000096')
    expect(icon).toHaveAttribute('src', '/beian.png')
  })
})
