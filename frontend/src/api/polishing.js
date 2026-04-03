import request from './request'

/** 执行润色（支持前端传入模型配置） */
export function polishText(text, strategy, modelConfig = null) {
  const body = { text, strategy }
  if (modelConfig && modelConfig.apiKey) {
    body.modelConfig = {
      model: modelConfig.model,
      apiKey: modelConfig.apiKey,
      baseUrl: modelConfig.baseUrl
    }
  }
  return request.post('/polishing', body)
}

/** 查询润色历史 */
export function listPolishingHistory(params) {
  return request.get('/polishing/history', { params })
}

/** 删除历史记录 */
export function deletePolishingHistory(id) {
  return request.delete(`/polishing/history/${id}`)
}

/** 将润色结果保存到词库 */
export function saveToLibrary(data) {
  return request.post('/polishing/save-to-library', data)
}
