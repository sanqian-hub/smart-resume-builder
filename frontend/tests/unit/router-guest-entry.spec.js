import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'

async function loadRouter() {
  vi.resetModules()

  const initMock = vi.fn().mockResolvedValue(false)
  vi.doMock('../../src/stores/user', () => ({
    useUserStore: () => ({
      init: initMock,
    }),
  }))
  vi.doMock('../../src/composables/useRuntimeLoader', () => ({
    useRuntimeLoader: () => ({
      hide: vi.fn(),
    }),
  }))

  const module = await import('../../src/router/index.js')
  return { router: module.default, initMock }
}

describe('guest edit route entry', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  test('lets unauthenticated visitors enter /edit', async () => {
    const { router, initMock } = await loadRouter()

    await router.push('/edit')

    expect(router.currentRoute.value.fullPath).toBe('/edit')
    expect(initMock).not.toHaveBeenCalled()
  })

  test('redirects unauthenticated visitors away from /edit/:id', async () => {
    const { router, initMock } = await loadRouter()

    await router.push('/edit/123')

    expect(router.currentRoute.value.fullPath).toBe('/login')
    expect(initMock).not.toHaveBeenCalled()
  })

  test('redirects unauthenticated visitors away from /profile', async () => {
    const { router, initMock } = await loadRouter()

    await router.push('/profile')

    expect(router.currentRoute.value.fullPath).toBe('/login')
    expect(initMock).not.toHaveBeenCalled()
  })
})
