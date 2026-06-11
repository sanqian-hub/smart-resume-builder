import { beforeEach, describe, expect, test, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/vue'
import { createRouter, createMemoryHistory } from 'vue-router'
import { flushPromises } from '@vue/test-utils'
import Register from '../../src/views/Register.vue'

const { registerMock } = vi.hoisted(() => ({
  registerMock: vi.fn(),
}))

vi.mock('../../src/api/user', async () => ({
  login: vi.fn(),
  register: registerMock,
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
      { path: '/login', component: { template: '<div>登录页</div>' } },
      { path: '/register', component: Register },
    ],
  })
}

async function renderRegisterPage() {
  const router = createTestRouter()
  await router.push('/register')
  await router.isReady()

  const utils = render(Register, {
    global: {
      plugins: [router],
    },
  })

  return { router, ...utils }
}

async function renderRegisterPageAt(path = '/register') {
  const router = createTestRouter()
  await router.push(path)
  await router.isReady()

  const utils = render(Register, {
    global: {
      plugins: [router],
    },
  })

  return { router, ...utils }
}

describe('注册页', () => {
  beforeEach(() => {
    registerMock.mockReset()
  })

  async function fillValidRegisterForm() {
    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.update(screen.getByLabelText('手机号'), '13800000000')
    await fireEvent.update(screen.getByLabelText('邮箱'), 'test@example.com')
  }

  async function submitRegisterForm() {
    await fireEvent.submit(document.querySelector('.auth-form'))
    await flushPromises()
  }

  test('展示完整字段、注册按钮和登录入口', async () => {
    await renderRegisterPage()

    expect(screen.getByLabelText('用户名')).toBeVisible()
    expect(screen.getByLabelText('账号')).toBeVisible()
    expect(screen.getByLabelText('密码')).toBeVisible()
    expect(screen.getByLabelText('手机号')).toBeVisible()
    expect(screen.getByLabelText('邮箱')).toBeVisible()
    expect(screen.getByRole('button', { name: '注册' })).toBeVisible()
    expect(screen.getByRole('link', { name: '立即登录' })).toBeVisible()
  })

  test('点击密码显隐按钮后切换输入框类型', async () => {
    await renderRegisterPage()

    const passwordInput = screen.getByLabelText('密码')
    expect(passwordInput).toHaveAttribute('type', 'password')

    const toggleButton = document.querySelector('.pw-toggle')
    await fireEvent.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'text')

    await fireEvent.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'password')
  })

  test('用户名为空时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await submitRegisterForm()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('用户名不能为空')).toBeVisible()
  })

  test('账号为空时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await submitRegisterForm()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('账号不能为空')).toBeVisible()
  })

  test('账号长度不足时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'ab')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('账号至少 4 位')).toBeVisible()
  })

  test('账号包含空格时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'test er')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('账号包含非法字符')).toBeVisible()
  })

  test('账号包含 @ 时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'test@er')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('账号包含非法字符')).toBeVisible()
  })

  test('账号包含 、 时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'test、er')
    await fireEvent.update(screen.getByLabelText('密码'), 'password123')
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('账号包含非法字符')).toBeVisible()
  })

  test('密码为空时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await submitRegisterForm()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('密码不能为空')).toBeVisible()
  })

  test('密码长度不足时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fireEvent.update(screen.getByLabelText('用户名'), '测试用户')
    await fireEvent.update(screen.getByLabelText('账号'), 'tester')
    await fireEvent.update(screen.getByLabelText('密码'), '1234567')
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('密码至少 8 位')).toBeVisible()
  })

  test('手机号过长时展示错误且不发起注册请求', async () => {
    await renderRegisterPage()

    await fillValidRegisterForm()
    await fireEvent.update(screen.getByLabelText('手机号'), '123456789012345678901')
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).not.toHaveBeenCalled()
    expect(screen.getByText('手机号长度不能超过 20 位')).toBeVisible()
  })

  test('注册成功后跳转到登录页', async () => {
    registerMock.mockResolvedValue({})

    const { router } = await renderRegisterPage()

    await fillValidRegisterForm()
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(registerMock).toHaveBeenCalledWith({
      username: '测试用户',
      userAccount: 'tester',
      userPassword: 'password123',
      phone: '13800000000',
      email: 'test@example.com',
    })
    expect(router.currentRoute.value.path).toBe('/login')
  })

  test('带 redirect 的注册页返回登录时保留回跳参数', async () => {
    await renderRegisterPageAt('/register?redirect=%2Fedit')

    const loginLink = screen.getByRole('link', { name: '立即登录' })
    const loginHref = loginLink.getAttribute('href') || ''
    const loginUrl = new URL(loginHref, 'http://localhost')
    expect(loginUrl.pathname).toBe('/login')
    expect(loginUrl.searchParams.get('redirect')).toBe('/edit')
  })

  test('带 redirect 的注册成功后跳回登录页时保留回跳参数', async () => {
    registerMock.mockResolvedValue({})

    const { router } = await renderRegisterPageAt('/register?redirect=%2Fedit')

    await fillValidRegisterForm()
    await fireEvent.click(screen.getByRole('button', { name: '注册' }))
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/edit')
  })
})
