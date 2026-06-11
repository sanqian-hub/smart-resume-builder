import { describe, expect, test } from 'vitest'
import { render, screen } from '@testing-library/vue'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import MainLayout from '../../src/layouts/MainLayout.vue'
import Login from '../../src/views/Login.vue'
import Register from '../../src/views/Register.vue'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>首页</div>' } },
      { path: '/login', component: Login },
      { path: '/register', component: Register },
    ],
  })
}

describe('品牌资源统一', () => {
  test('登录页和注册页品牌图标统一使用 logo.png', async () => {
    const router = createTestRouter()
    await router.push('/login')
    await router.isReady()

    render(Login, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    const loginLogo = screen.getAllByAltText('logo')[0]
    expect(loginLogo).toHaveAttribute('src', '/logo.png')

    const registerRouter = createTestRouter()
    await registerRouter.push('/register')
    await registerRouter.isReady()

    render(Register, {
      global: {
        plugins: [registerRouter],
      },
    })

    const registerLogo = screen.getAllByAltText('logo')[1]
    expect(registerLogo).toHaveAttribute('src', '/logo.png')
  })

  test('导航栏 logo 使用 logo.png', async () => {
    const router = createTestRouter()
    await router.push('/')
    await router.isReady()

    render(MainLayout, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    expect(screen.getByAltText('logo')).toHaveAttribute('src', '/logo.png')
  })

  test('favicon 指向 logo.png', () => {
    const indexHtml = readFileSync(join(process.cwd(), 'index.html'), 'utf8')
    expect(indexHtml).toContain('href="/logo.png"')
  })
})
