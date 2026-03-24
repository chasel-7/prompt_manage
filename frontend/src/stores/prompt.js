import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listPrompts, toggleFavorite, deletePrompt } from '../api/prompt'

export const usePromptStore = defineStore('prompt', () => {
    /** 提示词列表 */
    const prompts = ref([])

    /** 分页信息 */
    const pagination = ref({
        total: 0,
        pageNum: 1,
        pageSize: 12
    })

    /** 搜索/过滤条件 */
    const queryParams = ref({
        keyword: '',
        groupId: null,
        tagId: null,
        favorite: null,
        sortBy: 'updated_at',
        sortOrder: 'desc'
    })

    /** 加载状态 */
    const loading = ref(false)

    /** 获取提示词列表 */
    async function fetchPrompts() {
        loading.value = true
        try {
            const params = {
                ...queryParams.value,
                pageNum: pagination.value.pageNum,
                pageSize: pagination.value.pageSize
            }
            // 清理空值
            Object.keys(params).forEach(key => {
                if (params[key] === null || params[key] === '' || params[key] === undefined) {
                    delete params[key]
                }
            })
            const res = await listPrompts(params)
            prompts.value = res.data.records || []
            pagination.value.total = res.data.total || 0
        } finally {
            loading.value = false
        }
    }

    /** 切换收藏 */
    async function handleToggleFavorite(id) {
        await toggleFavorite(id)
        await fetchPrompts()
    }

    /** 删除提示词 */
    async function handleDelete(id) {
        await deletePrompt(id)
        await fetchPrompts()
    }

    /** 设置搜索条件并重置页码 */
    function setQuery(params) {
        Object.assign(queryParams.value, params)
        pagination.value.pageNum = 1
        fetchPrompts()
    }

    /** 切换页码 */
    function setPage(pageNum) {
        pagination.value.pageNum = pageNum
        fetchPrompts()
    }

    /** 设置每页数量 */
    function setPageSize(pageSize) {
        pagination.value.pageSize = pageSize
        pagination.value.pageNum = 1
        fetchPrompts()
    }

    return {
        prompts,
        pagination,
        queryParams,
        loading,
        fetchPrompts,
        handleToggleFavorite,
        handleDelete,
        setQuery,
        setPage,
        setPageSize
    }
})
