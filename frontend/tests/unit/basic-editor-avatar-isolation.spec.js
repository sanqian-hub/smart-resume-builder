import { fireEvent, render, screen } from '@testing-library/vue'
import { flushPromises } from '@vue/test-utils'
import { defineComponent } from 'vue'
import { beforeEach, describe, expect, test, vi } from 'vitest'

import BasicEditor from '../../src/views/resume/editors/BasicEditor.vue'

const { uploadAvatarMock, uploadImageMock } = vi.hoisted(() => ({
  uploadAvatarMock: vi.fn(),
  uploadImageMock: vi.fn(),
}))

vi.mock('../../src/api/user', async () => ({
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
  updateMyInfo: vi.fn(),
  uploadAvatar: uploadAvatarMock,
  uploadResumeAvatar: uploadImageMock,
  uploadImage: vi.fn(),
}))

vi.mock('../../src/views/resume/editors/AvatarCropModal.vue', async () => ({
  default: defineComponent({
    props: {
      visible: { type: Boolean, default: false },
    },
    emits: ['confirm', 'cancel'],
    template: `
      <button
        v-if="visible"
        type="button"
        aria-label="确认裁剪"
        @click="$emit('confirm', 'data:image/jpeg;base64,QQ==')"
      >
        确认裁剪
      </button>
    `,
  }),
}))

function createImageMock() {
  return class MockImage {
    set src(value) {
      this._src = value
      queueMicrotask(() => {
        this.width = 320
        this.height = 320
        this.onload?.()
      })
    }
  }
}

describe('BasicEditor 简历头像隔离', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    uploadAvatarMock.mockReset()
    uploadImageMock.mockReset()

    uploadAvatarMock.mockResolvedValue('https://mock.cdn.local/account-avatar.jpg')
    uploadImageMock.mockResolvedValue('https://mock.cdn.local/resume-avatar.jpg')

    global.Image = createImageMock()
    global.URL.createObjectURL = vi.fn(() => 'blob:mock-avatar')
    const originalCreateElement = document.createElement.bind(document)

    vi.spyOn(document, 'createElement').mockImplementation((tagName) => {
      if (tagName === 'canvas') {
        return {
          width: 0,
          height: 0,
          getContext: () => ({
            drawImage: vi.fn(),
          }),
          toDataURL: () => 'data:image/jpeg;base64,QQ==',
        }
      }
      return originalCreateElement(tagName)
    })
  })

  test('确认裁剪后只更新简历头像，不调用账号头像上传接口', async () => {
    const { emitted } = render(BasicEditor, {
      props: {
        modelValue: JSON.stringify({
          name: '夜神月',
          avatar: '',
        }),
      },
    })

    const fileInput = document.querySelector('input[type="file"]')
    const file = new File(['avatar'], 'resume-avatar.png', { type: 'image/png' })

    await fireEvent.change(fileInput, { target: { files: [file] } })
    await flushPromises()

    await fireEvent.click(screen.getByRole('button', { name: '确认裁剪' }))
    await flushPromises()

    expect(uploadAvatarMock).not.toHaveBeenCalled()
    expect(uploadImageMock).not.toHaveBeenCalled()

    const updates = emitted()['update:modelValue']
    expect(updates).toBeTruthy()
    expect(JSON.parse(updates.at(-1)[0])).toMatchObject({
      name: '夜神月',
      avatar: 'data:image/jpeg;base64,QQ==',
    })
  })
})
