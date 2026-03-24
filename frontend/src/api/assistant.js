import request from './request'

/** 获取所有助手 */
export function listAssistants() {
  return request.get('/assistants')
}

/** 获取助手详情 */
export function getAssistant(id) {
  return request.get(`/assistants/${id}`)
}

/** 创建助手 */
export function createAssistant(data) {
  return request.post('/assistants', data)
}

/** 更新助手 */
export function updateAssistant(id, data) {
  return request.put(`/assistants/${id}`, data)
}

/** 删除助手 */
export function deleteAssistant(id) {
  return request.delete(`/assistants/${id}`)
}
