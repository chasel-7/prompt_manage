<template>
  <div class="polishing-page">
    <!-- 润色操作区 -->
    <div class="polish-section">
      <div class="polish-row">
        <!-- 左：原文输入 -->
        <div class="text-panel original-panel">
          <div class="panel-header">
            <h3><el-icon><Edit /></el-icon> 原始提示词</h3>
          </div>
          <el-input
            v-model="originalText"
            type="textarea"
            :rows="14"
            placeholder="请输入需要润色的提示词..."
            class="text-area"
          />
        </div>

        <!-- 中间：策略 + 按钮 -->
        <div class="action-center">
          <el-select v-model="strategy" placeholder="选择策略" class="strategy-select">
            <el-option value="professional" label="🔬 专业术语增强" />
            <el-option value="structured" label="📋 逻辑结构化" />
            <el-option value="specific" label="🎯 语气具体化" />
          </el-select>
          <el-button
            type="primary"
            :loading="polishing"
            :disabled="!originalText.trim() || !strategy"
            class="polish-btn"
            @click="handlePolish"
          >
            <el-icon v-if="!polishing"><MagicStick /></el-icon>
            {{ polishing ? '润色中...' : '开始润色' }}
          </el-button>
          <el-button
            v-if="polishedText"
            type="success"
            plain
            @click="handleCopy"
          >
            <el-icon><CopyDocument /></el-icon>
            复制结果
          </el-button>
          <el-button
            v-if="polishedText"
            type="warning"
            plain
            @click="showSaveDialog = true"
          >
            <el-icon><FolderAdd /></el-icon>
            保存到词库
          </el-button>
        </div>

        <!-- 右：润色结果 -->
        <div class="text-panel result-panel">
          <div class="panel-header">
            <h3><el-icon><MagicStick /></el-icon> 润色结果</h3>
            <el-tag v-if="currentStrategyName" size="small" type="success">{{ currentStrategyName }}</el-tag>
          </div>
          <div v-if="polishing" class="loading-area">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <p>AI 正在润色，请稍候...</p>
          </div>
          <div v-else-if="polishedText" class="result-text">{{ polishedText }}</div>
          <el-empty v-else description="润色结果将在此显示" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 历史记录 -->
    <div class="history-section">
      <div class="section-header">
        <h3><el-icon><Clock /></el-icon> 润色历史</h3>
        <el-button text type="primary" @click="fetchHistory">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-table
        v-loading="historyLoading"
        :data="historyList"
        stripe
        class="history-table"
      >
        <el-table-column label="策略" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ row.strategyName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="originalText" label="原始文本" min-width="200" show-overflow-tooltip />
        <el-table-column prop="polishedText" label="润色结果" min-width="200" show-overflow-tooltip />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="loadFromHistory(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button text type="success" size="small" @click="handleCopyText(row.polishedText)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-popconfirm title="确定删除该记录？" @confirm="handleDeleteHistory(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="historyTotal > 0" class="pagination-area">
        <el-pagination
          v-model:current-page="historyPage"
          :total="historyTotal"
          :page-size="10"
          layout="total, prev, pager, next"
          @current-change="fetchHistory"
        />
      </div>
    </div>

    <!-- 保存到词库对话框 -->
    <el-dialog
      v-model="showSaveDialog"
      title="保存到提示词词库"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="saveForm" label-width="80px" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="saveForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="saveForm.description" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="内容预览">
          <div class="save-preview">{{ polishedText }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSaveDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveToLibrary">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { polishText, listPolishingHistory, deletePolishingHistory, saveToLibrary } from '../api/polishing'

const originalText = ref('')
const polishedText = ref('')
const strategy = ref('professional')
const currentStrategyName = ref('')
const polishing = ref(false)

const historyList = ref([])
const historyTotal = ref(0)
const historyPage = ref(1)
const historyLoading = ref(false)

const showSaveDialog = ref(false)
const saving = ref(false)
const saveForm = ref({ title: '', description: '' })

const strategyNames = {
  professional: '专业术语增强',
  structured: '逻辑结构化',
  specific: '语气具体化'
}

onMounted(() => {
  fetchHistory()
})

async function handlePolish() {
  if (!originalText.value.trim()) {
    ElMessage.warning('请输入原始提示词')
    return
  }
  polishing.value = true
  polishedText.value = ''
  currentStrategyName.value = ''
  try {
    const res = await polishText(originalText.value, strategy.value)
    polishedText.value = res.data.polishedText
    currentStrategyName.value = res.data.strategyName
    ElMessage.success('润色完成')
    fetchHistory()
  } catch {
    // 错误已由拦截器处理
  } finally {
    polishing.value = false
  }
}

async function fetchHistory() {
  historyLoading.value = true
  try {
    const res = await listPolishingHistory({ pageNum: historyPage.value, pageSize: 10 })
    historyList.value = res.data.records || []
    historyTotal.value = res.data.total || 0
  } finally {
    historyLoading.value = false
  }
}

function loadFromHistory(row) {
  originalText.value = row.originalText
  polishedText.value = row.polishedText
  currentStrategyName.value = row.strategyName
  // 回滚策略选择
  for (const [key, name] of Object.entries(strategyNames)) {
    if (name === row.strategyName) {
      strategy.value = key
      break
    }
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function handleDeleteHistory(id) {
  await deletePolishingHistory(id)
  ElMessage.success('已删除')
  fetchHistory()
}

function handleCopy() {
  handleCopyText(polishedText.value)
}

function handleCopyText(text) {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

async function handleSaveToLibrary() {
  if (!saveForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  saving.value = true
  try {
    await saveToLibrary({
      title: saveForm.value.title,
      content: polishedText.value,
      description: saveForm.value.description || null,
      tagIds: []
    })
    ElMessage.success('已保存到词库')
    showSaveDialog.value = false
    saveForm.value = { title: '', description: '' }
  } finally {
    saving.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.polishing-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
}

.polish-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.polish-row {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 20px;
  align-items: start;
}

.text-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 6px;
}

.original-panel .panel-header h3 {
  color: #6366f1;
}

.result-panel .panel-header h3 {
  color: #059669;
}

.text-area :deep(.el-textarea__inner) {
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.7;
  min-height: 280px !important;
}

.action-center {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  justify-content: center;
  padding-top: 40px;
  min-width: 140px;
}

.strategy-select {
  width: 100%;
}

.polish-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}

.loading-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  color: #6366f1;
  gap: 12px;
}

.loading-area p {
  margin: 0;
  font-size: 14px;
  color: #888;
}

.result-text {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  min-height: 280px;
  max-height: 400px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 历史记录 */
.history-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 6px;
}

.history-table {
  border-radius: 8px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* 保存对话框 */
.save-preview {
  background: #f8f8fc;
  border: 1px solid #e8e8f0;
  border-radius: 8px;
  padding: 12px;
  font-size: 13px;
  line-height: 1.6;
  color: #555;
  max-height: 150px;
  overflow-y: auto;
  white-space: pre-wrap;
}
</style>
