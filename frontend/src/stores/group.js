import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listGroups, createGroup, updateGroup, deleteGroup } from '../api/group'
import { ElMessage } from 'element-plus'

export const useGroupStore = defineStore('group', () => {
    const groups = ref([])
    const loading = ref(false)

    async function fetchGroups() {
        loading.value = true
        try {
            const res = await listGroups()
            groups.value = res.data || []
        } finally {
            loading.value = false
        }
    }

    async function handleCreate(data) {
        await createGroup(data)
        ElMessage.success('分组创建成功')
        await fetchGroups()
    }

    async function handleUpdate(id, data) {
        await updateGroup(id, data)
        ElMessage.success('分组更新成功')
        await fetchGroups()
    }

    async function handleDelete(id) {
        await deleteGroup(id)
        ElMessage.success('分组已删除')
        await fetchGroups()
    }

    return {
        groups,
        loading,
        fetchGroups,
        handleCreate,
        handleUpdate,
        handleDelete
    }
})
