import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true,
})

request.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 0) {
      return data.data
    }
    if (data.code === 40100) {
      localStorage.removeItem('user')
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    const responseMessage = error.response?.data?.message
    if (responseMessage) {
      return Promise.reject(new Error(responseMessage))
    }
    if (error.response?.status === 400) {
      return Promise.reject(new Error('请求参数错误'))
    }
    return Promise.reject(new Error(error.message || '请求失败'))
  },
)

export default request
