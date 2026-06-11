import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function logout() {
  return request.post('/user/logout')
}

export function getCurrentUser() {
  return request.get('/user/current')
}

export function updateMyInfo(data) {
  return request.post('/user/update', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function uploadResumeAvatar(file) {
  return uploadImage(file, 'resume-avatar')
}

export function uploadImage(file, folder = 'portfolio') {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/upload/image', formData, {
    params: { folder },
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
