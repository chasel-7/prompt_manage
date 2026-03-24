<template>
  <div class="trash-page">
    <div class="top-bar">
      <p class="trash-hint">
        <el-icon><WarningFilled /></el-icon>
        回收站中的提示词可以恢复或永久删除。
      </p>
    </div>

    <el-table
      v-loading="loading"
      :data="trashList"
      stripe
      class="trash-table"
    >
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="删除时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" text size="small" @click="handleRestore(row.id)">
            <el-icon><RefreshLeft /></el-icon>
            恢复
          </el-button>
          <el-popconfirm title="永久删除后不可恢复，确定？" @confirm="handleForceDelete(row.id)">
            <template #reference>
              <el-button type="danger" text size="small">
                <el-icon><Delete /></el-icon>
                永久删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > 0" class="pagination-area">
      <el-pagination
        v-model:current-page="pageNum"
        :total="total"
        :page-size="10"
        layout="total, prev, pager, next"
        @current-change="fetchTrash"
      />
    </div>

    <el-empty v-if="!loading && trashList.length === 0" description="回收站为空" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listTrash, restorePrompt, forceDeletePrompt } from '../api/prompt'

const trashList = ref([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

onMounted(() => {
  fetchTrash()
})

async function fetchTrash() {
  loading.value = true
  try {
    const res = await listTrash({ pageNum: pageNum.value, pageSize: 10 })
    trashList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function handleRestore(id) {
  await restorePrompt(id)
  ElMessage.success('提示词已恢复')
  fetchTrash()
}

async function handleForceDelete(id) {
  await forceDeletePrompt(id)
  ElMessage.success('已永久删除')
  fetchTrash()
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.trash-page {
  min-height: 100%;
}

.top-bar {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.trash-hint {
  margin: 0;
  color: #e6a23c;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.trash-table {
  border-radius: 12px;
  overflow: hidden;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}
</style>
