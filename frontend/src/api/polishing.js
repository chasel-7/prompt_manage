import request from './request'

/** 执行润色 */
export function polishText(text, strategy) {
  return request.post('/polishing', { text, strategy })
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
