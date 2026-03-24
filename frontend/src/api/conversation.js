import request from './request'

/** 获取对话列表 */
export function listConversations(assistantId) {
  return request.get('/conversations', { params: { assistantId } })
}

/** 创建对话 */
export function createConversation(assistantId, title) {
  return request.post('/conversations', { assistantId, title })
}

/** 获取对话消息 */
export function getMessages(conversationId) {
  return request.get(`/conversations/${conversationId}/messages`)
}

/** 发送消息 */
export function sendMessage(conversationId, content) {
  return request.post(`/conversations/${conversationId}/messages`, { content })
}

/** 删除对话 */
export function deleteConversation(id) {
  return request.delete(`/conversations/${id}`)
}

/** 导出 Markdown（返回下载 URL） */
export function getExportUrl(conversationId) {
  return `/api/conversations/${conversationId}/export`
}
