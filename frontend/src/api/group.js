import request from './request'

/** 获取所有分组 */
export function listGroups() {
    return request.get('/groups')
}

/** 创建分组 */
export function createGroup(data) {
    return request.post('/groups', data)
}

/** 更新分组 */
export function updateGroup(id, data) {
    return request.put(`/groups/${id}`, data)
}

/** 删除分组 */
export function deleteGroup(id) {
    return request.delete(`/groups/${id}`)
}
