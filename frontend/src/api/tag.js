import request from './request'

/** 获取所有标签 */
export function listTags() {
    return request.get('/tags')
}

/** 创建标签 */
export function createTag(name) {
    return request.post('/tags', { name })
}

/** 删除标签 */
export function deleteTag(id) {
    return request.delete(`/tags/${id}`)
}
