<template>
  <div class="trash-page">
    <div class="trash-banner">
      <div class="banner-icon">🗑️</div>
      <div class="banner-info">
        <p class="banner-text">回收站中的提示词可以恢复或永久删除</p>
        <span class="banner-count" v-if="total > 0">当前共 {{ total }} 条</span>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table v-loading="loading" :data="trashList" stripe class="trash-table">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <span class="title-text">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="desc-text">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="删除时间" width="160">
          <template #default="{ row }"><span class="time-text">{{ formatTime(row.updatedAt) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleRestore(row.id)">
              <el-icon><RefreshLeft /></el-icon> 恢复
            </el-button>
            <el-popconfirm title="永久删除后不可恢复，确定？" @confirm="handleForceDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">
                  <el-icon><Delete /></el-icon> 永久删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="total > 0" class="pagination-area">
      <el-pagination v-model:current-page="pageNum" :total="total" :page-size="10" layout="total, prev, pager, next" background @current-change="fetchTrash" />
    </div>

    <div v-if="!loading && trashList.length === 0" class="empty-state">
      <div class="empty-icon">✨</div>
      <h3>回收站为空</h3>
      <p>所有提示词都安全无恙</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listTrash, restorePrompt, forceDeletePrompt } from '../api/prompt'

const trashList = ref([]); const total = ref(0); const pageNum = ref(1); const loading = ref(false)
onMounted(() => { fetchTrash() })
async function fetchTrash() { loading.value=true; try { const res = await listTrash({pageNum:pageNum.value,pageSize:10}); trashList.value=res.data.records||[]; total.value=res.data.total||0 } finally { loading.value=false } }
async function handleRestore(id) { await restorePrompt(id); ElMessage.success('已恢复'); fetchTrash() }
async function handleForceDelete(id) { await forceDeletePrompt(id); ElMessage.success('已永久删除'); fetchTrash() }
function formatTime(time) { if(!time)return''; const d=new Date(time); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` }
</script>

<style scoped>
.trash-page { min-height: 100%; }

.trash-banner {
  display: flex; align-items: center; gap: 14px;
  background: linear-gradient(135deg, rgba(245,158,11,0.06), rgba(239,68,68,0.04));
  border: 1px solid rgba(245,158,11,0.15);
  border-radius: var(--radius-md); padding: 16px 20px; margin-bottom: 16px;
}
.banner-icon { font-size: 28px; }
.banner-text { margin: 0; font-size: 13px; color: #92400e; font-weight: 500; }
.banner-count { font-size: 12px; color: var(--color-text-tertiary); }

.table-wrapper { background: var(--color-surface); border-radius: var(--radius-md); box-shadow: var(--shadow-sm); border: 1px solid var(--color-border-light); overflow: hidden; }
.title-text { font-weight: 500; color: var(--color-text-primary); }
.desc-text { color: var(--color-text-tertiary); font-size: 13px; }
.time-text { font-size: 12px; color: var(--color-text-tertiary); }
.pagination-area { display: flex; justify-content: center; margin-top: 16px; }

.empty-state { text-align: center; padding: 60px 20px; background: var(--color-surface); border-radius: var(--radius-lg); border: 2px dashed var(--color-border); margin-top: 16px; }
.empty-icon { font-size: 40px; margin-bottom: 8px; }
.empty-state h3 { font-size: 15px; color: var(--color-text-primary); margin: 0 0 4px; }
.empty-state p { font-size: 13px; color: var(--color-text-tertiary); margin: 0; }
</style>
