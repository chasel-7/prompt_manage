import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        // blob 类型（文件下载）直接返回 data
        if (response.config.responseType === 'blob') {
            return response.data
        }
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
        return res
    },
    (error) => {
        const msg = error.response?.data?.msg || error.message || '网络错误'
        ElMessage.error(msg)
        return Promise.reject(error)
    }
)

export default request
