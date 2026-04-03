<template>
  <el-dialog
    :model-value="visible"
    title="管理分组"
    width="500px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <!-- 新建分组 -->
    <div class="add-group-row">
      <el-input v-model="newGroupName" placeholder="输入新分组名称" class="add-input" @keyup.enter="handleAdd" />
      <el-button type="primary" @click="handleAdd">添加</el-button>
    </div>

    <!-- 分组列表 -->
    <el-table :data="groups" class="group-table" max-height="400">
      <el-table-column label="分组名称" min-width="160">
        <template #default="{ row }">
          <template v-if="editingId === row.id">
            <el-input v-model="editingName" size="small" @keyup.enter="handleSaveEdit(row.id)" />
          </template>
          <template v-else>
            {{ row.name }}
          </template>
        </template>
      </el-table-column>
      <el-table-column label="提示词数" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.promptCount || 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <template v-if="editingId === row.id">
            <el-button text type="primary" size="small" @click="handleSaveEdit(row.id)">保存</el-button>
            <el-button text size="small" @click="editingId = null">取消</el-button>
          </template>
          <template v-else>
            <el-button text type="primary" size="small" @click="startEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该分组？" @confirm="handleDeleteGroup(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createGroup, updateGroup, deleteGroup } from '../api/group'

const props = defineProps({
  visible: Boolean,
  groups: Array
})

const emit = defineEmits(['update:visible', 'refresh'])

const newGroupName = ref('')
const editingId = ref(null)
const editingName = ref('')

async function handleAdd() {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }
  await createGroup({ name: newGroupName.value.trim() })
  ElMessage.success('分组创建成功')
  newGroupName.value = ''
  emit('refresh')
}

function startEdit(row) {
  editingId.value = row.id
  editingName.value = row.name
}

async function handleSaveEdit(id) {
  if (!editingName.value.trim()) {
    ElMessage.warning('分组名称不能为空')
    return
  }
  await updateGroup(id, { name: editingName.value.trim() })
  ElMessage.success('分组已更新')
  editingId.value = null
  emit('refresh')
}

async function handleDeleteGroup(id) {
  await deleteGroup(id)
  ElMessage.success('分组已删除')
  emit('refresh')
}
</script>

<style scoped>
.add-group-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.add-input {
  flex: 1;
}

.group-table {
  border-radius: var(--radius-sm);
}
</style>
