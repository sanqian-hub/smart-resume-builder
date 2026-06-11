import { beforeEach, describe, expect, test, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/vue'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { flushPromises } from '@vue/test-utils'
import Login from '../../src/views/Login.vue'

const { loginMock } = vi.hoisted(() => ({
  loginMock: vi.fn(),
}))

vi.mock('../../src/api/user', async () => ({
  login: loginMock,
  register: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
  updateMyInfo: vi.fn(),
  uploadAvatar: vi.fn(),
  uploadImage: vi.fn(),
}))

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>首页</div>' } },
      { path: '/login', component: Login },
      { path: '/register', component: { template: '<div>注册页</div>' } },
    ],
  })
}

async function renderLoginPage() {
  return renderLoginPageAt('/login')
}

async function renderLoginPageAt(path = '/login') {
  const router = createTestRouter()
  await router.push(path)
  await router.isReady()

  const utils = render(Login, {
    global: {
      plugins: [createPinia(), router],
    },
  })

  return { router, ...utils }
}

describe('登录页', () => {
  beforeEach(() => {
    localStorage.clear()
    loginMock.mockReset()
    vi.useRealTimers()
  })

  test('展示账号、密码、登录按钮和注册入口', async () => {
    await renderLoginPage()

    expect(screen.getByLabelText('账号')).toBeVisible()
    expect(screen.getByLabelText('密码')).toBeVisible()
    expect(screen.getByRole('button', { name: '登录' })).toBeVisible()
    expect(screen.getByRole('link', { name: '立即注册' })).toBeVisible()
  })

  test('普通登录入口不展示功能受限提示', async () => {
    await renderLoginPage()

    expect(screen.queryByText('登录后即可保存当前简历')).not.toBeInTheDocument()
  })

  test('带 save intent 的登录页展示保存提示', async () => {
    await renderLoginPageAt('/login?redirect=%2Fedit&intent=save')

    expect(screen.getByText('登录后即可保存当前简历')).toBeVisible()
  })

  test('带 ai-chat intent 的登录页展示 AI 提示', async () => {
    await renderLoginPageAt('/login?redirect=%2Fedit&intent=ai-chat')

    expect(screen.getByText('登录后即可使用智能助手')).toBeVisible()
  })

  test('点击密码显隐按钮后切换输入框类型', async () => {
    await renderLoginPage()

    const passwordInput = screen.getByLabelText('密码')
    expect(passwordInput).toHaveAttribute('type', 'password')

    const toggleButton = document.querySelector('.pw-toggle')
    await fireEvent.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'text')

    await fireEvent.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'password')
  })

  test('登录表单字段保留必填约束', async () => {
    await renderLoginPage()

    expect(screen.getByLabelText('账号')).toBeRequired()
    expect(screen.getByLabelText('密码')).toBeRequired()
  })

  test('展示自动登录选项且默认未选中', async () => {
    await renderLoginPage()

    expect(screen.getByLabelText('自动登录')).toBeVisible()
    expect(screen.getByLabelText('自动登录')).not.toBeChecked()
  })

  test('登录成功后保存用户信息并跳转首页', async () => {
    vi.useFakeTimers()
    loginMock.mockResolvedValue({ id: 1, userName: '测试用户' })

    const { router } = await renderLoginPage()

    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByRole('button', { name: '登录' }))
    await flushPromises()
    await vi.advanceTimersByTimeAsync(800)
    await flushPromises()

    expect(loginMock).toHaveBeenCalledWith({
      userAccount: 'tester',
      userPassword: 'password123',
      rememberMe: false,
    })
    expect(JSON.parse(localStorage.getItem('user'))).toEqual({ id: 1, userName: '测试用户' })
    expect(router.currentRoute.value.path).toBe('/')
  })

  test('勾选自动登录后登录请求体带 rememberMe true', async () => {
    vi.useFakeTimers()
    loginMock.mockResolvedValue({ id: 1, userName: '测试用户' })

    await renderLoginPage()

    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByLabelText('自动登录'))
    await fireEvent.click(screen.getByRole('button', { name: '登录' }))
    await flushPromises()
    await vi.advanceTimersByTimeAsync(800)
    await flushPromises()

    expect(loginMock).toHaveBeenCalledWith({
      userAccount: 'tester',
      userPassword: 'password123',
      rememberMe: true,
    })
  })

  test('登录按钮至少保持 800ms loading 后再跳转首页', async () => {
    vi.useFakeTimers()
    loginMock.mockResolvedValue({ id: 1, userName: '测试用户' })

    const { router } = await renderLoginPage()

    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByRole('button', { name: '登录' }))

    expect(screen.getByRole('button', { name: '登录中...' })).toBeDisabled()

    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/login')

    await vi.advanceTimersByTimeAsync(799)
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/login')

    await vi.advanceTimersByTimeAsync(1)
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/')
  })

  test('登录失败时展示错误提示', async () => {
    vi.useFakeTimers()
    loginMock.mockRejectedValue(new Error('账号或密码错误'))

    await renderLoginPage()

    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), 'wrong-password')
    await fireEvent.click(screen.getByRole('button', { name: '登录' }))
    await flushPromises()
    await vi.advanceTimersByTimeAsync(800)
    await flushPromises()

    expect(loginMock).toHaveBeenCalled()
    expect(screen.getByText('账号或密码错误')).toBeVisible()
  })

  test('带 redirect 的登录页前往注册时保留回跳参数', async () => {
    const router = createTestRouter()
    await router.push('/login?redirect=%2Fedit')
    await router.isReady()

    render(Login, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    const registerLink = screen.getByRole('link', { name: '立即注册' })
    const registerHref = registerLink.getAttribute('href') || ''
    const registerUrl = new URL(registerHref, 'http://localhost')
    expect(registerUrl.pathname).toBe('/register')
    expect(registerUrl.searchParams.get('redirect')).toBe('/edit')
  })
})
