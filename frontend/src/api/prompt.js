import request from './request'

/** 分页查询提示词 */
export function listPrompts(params) {
    return request.get('/prompts', { params })
}

/** 查询提示词详情 */
export function getPrompt(id) {
    return request.get(`/prompts/${id}`)
}

/** 创建提示词 */
export function createPrompt(data) {
    return request.post('/prompts', data)
}

/** 更新提示词 */
export function updatePrompt(id, data) {
    return request.put(`/prompts/${id}`, data)
}

/** 逻辑删除提示词 */
export function deletePrompt(id) {
    return request.delete(`/prompts/${id}`)
}

/** 切换收藏 */
export function toggleFavorite(id) {
    return request.put(`/prompts/${id}/favorite`)
}

/** 回收站列表 */
export function listTrash(params) {
    return request.get('/prompts/trash', { params })
}

/** 恢复提示词 */
export function restorePrompt(id) {
    return request.put(`/prompts/${id}/restore`)
}

/** 永久删除 */
export function forceDeletePrompt(id) {
    return request.delete(`/prompts/${id}/force`)
}
