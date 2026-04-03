<template>
  <div class="polishing-page">
    <div class="polish-section">
      <div class="polish-row">
        <div class="text-panel">
          <div class="panel-top"><span class="panel-label">📝 原始提示词</span></div>
          <el-input v-model="originalText" type="textarea" :rows="14" placeholder="请输入需要润色的提示词..." class="polish-textarea" />
        </div>

        <div class="action-column">
          <el-select v-model="strategy" placeholder="选择策略" class="strategy-sel">
            <el-option value="professional" label="🔬 专业术语增强" />
            <el-option value="structured" label="📋 逻辑结构化" />
            <el-option value="specific" label="🎯 语气具体化" />
          </el-select>
          <el-button type="primary" :loading="polishing" :disabled="!originalText.trim() || !strategy" class="polish-btn" round @click="handlePolish">
            {{ polishing ? '润色中...' : '✨ 开始润色' }}
          </el-button>
          <el-button v-if="polishedText" size="small" round @click="handleCopy">
            <el-icon><CopyDocument /></el-icon> 复制结果
          </el-button>
          <el-button v-if="polishedText" size="small" round @click="showSaveDialog = true">
            <el-icon><FolderAdd /></el-icon> 保存到词库
          </el-button>
        </div>

        <div class="text-panel">
          <div class="panel-top">
            <span class="panel-label">✨ 润色结果</span>
            <el-tag v-if="currentStrategyName" size="small" effect="plain" round>{{ currentStrategyName }}</el-tag>
          </div>
          <div v-if="polishing" class="loading-area">
            <el-icon class="is-loading" :size="28"><Loading /></el-icon>
            <p>AI 正在润色中...</p>
          </div>
          <div v-else-if="polishedText" class="result-text">{{ polishedText }}</div>
          <div v-else class="result-placeholder">
            <span>🤖</span>
            <p>润色结果将在此显示</p>
          </div>
        </div>
      </div>
    </div>

    <div class="history-section">
      <div class="history-bar">
        <span class="history-label"><el-icon><Clock /></el-icon> 润色历史</span>
        <el-button text type="primary" size="small" @click="fetchHistory"><el-icon><Refresh /></el-icon> 刷新</el-button>
      </div>
      <el-table v-loading="historyLoading" :data="historyList" stripe size="small" class="history-table">
        <el-table-column label="策略" width="130">
          <template #default="{ row }"><el-tag size="small" effect="plain" round>{{ row.strategyName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="originalText" label="原始文本" min-width="200" show-overflow-tooltip />
        <el-table-column prop="polishedText" label="润色结果" min-width="200" show-overflow-tooltip />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="loadFromHistory(row)">查看</el-button>
            <el-button text type="primary" size="small" @click="handleCopyText(row.polishedText)">复制</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDeleteHistory(row.id)">
              <template #reference><el-button text type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="historyTotal > 0" class="pagination-area">
        <el-pagination v-model:current-page="historyPage" :total="historyTotal" :page-size="10" layout="total, prev, pager, next" background @current-change="fetchHistory" />
      </div>
    </div>

    <el-dialog v-model="showSaveDialog" title="保存到提示词词库" width="480px" :close-on-click-modal="false">
      <el-form :model="saveForm" label-position="top">
        <el-form-item label="标题"><el-input v-model="saveForm.title" placeholder="请输入标题" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="saveForm.description" type="textarea" :rows="2" placeholder="选填" /></el-form-item>
        <el-form-item label="内容预览"><div class="save-preview">{{ polishedText }}</div></el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showSaveDialog = false">取消</el-button>
        <el-button type="primary" round :loading="saving" @click="handleSaveToLibrary">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { polishText, listPolishingHistory, deletePolishingHistory, saveToLibrary } from '../api/polishing'

const originalText = ref(''); const polishedText = ref(''); const strategy = ref('professional'); const currentStrategyName = ref(''); const polishing = ref(false)
const historyList = ref([]); const historyTotal = ref(0); const historyPage = ref(1); const historyLoading = ref(false)
const showSaveDialog = ref(false); const saving = ref(false); const saveForm = ref({ title: '', description: '' })
const strategyNames = { professional: '专业术语增强', structured: '逻辑结构化', specific: '语气具体化' }

onMounted(() => { fetchHistory() })

async function handlePolish() {
  if (!originalText.value.trim()) { ElMessage.warning('请输入'); return }
  polishing.value = true; polishedText.value = ''; currentStrategyName.value = ''
  try { const res = await polishText(originalText.value, strategy.value); polishedText.value = res.data.polishedText; currentStrategyName.value = res.data.strategyName; ElMessage.success('润色完成'); fetchHistory() }
  catch {} finally { polishing.value = false }
}
async function fetchHistory() { historyLoading.value = true; try { const res = await listPolishingHistory({ pageNum: historyPage.value, pageSize: 10 }); historyList.value = res.data.records || []; historyTotal.value = res.data.total || 0 } finally { historyLoading.value = false } }
function loadFromHistory(row) { originalText.value = row.originalText; polishedText.value = row.polishedText; currentStrategyName.value = row.strategyName; for (const [key, name] of Object.entries(strategyNames)) { if (name === row.strategyName) { strategy.value = key; break } } window.scrollTo({ top: 0, behavior: 'smooth' }) }
async function handleDeleteHistory(id) { await deletePolishingHistory(id); ElMessage.success('已删除'); fetchHistory() }
function handleCopy() { handleCopyText(polishedText.value) }
function handleCopyText(text) { navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制')).catch(() => ElMessage.error('失败')) }
async function handleSaveToLibrary() { if (!saveForm.value.title.trim()) { ElMessage.warning('请输入标题'); return } saving.value = true; try { await saveToLibrary({ title: saveForm.value.title, content: polishedText.value, description: saveForm.value.description || null, tagIds: [] }); ElMessage.success('已保存'); showSaveDialog.value = false; saveForm.value = { title: '', description: '' } } finally { saving.value = false } }
function formatTime(time) { if (!time) return ''; const d = new Date(time); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}` }
</script>

<style scoped>
.polishing-page { display: flex; flex-direction: column; gap: 20px; min-height: 100%; }

.polish-section {
  background: var(--color-surface); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow-sm); border: 1px solid var(--color-border-light);
}

.polish-row { display: grid; grid-template-columns: 1fr auto 1fr; gap: 20px; align-items: start; }
.text-panel { display: flex; flex-direction: column; gap: 10px; }
.panel-top { display: flex; justify-content: space-between; align-items: center; }
.panel-label { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); }

.polish-textarea :deep(.el-textarea__inner) { border-radius: var(--radius-sm); font-size: 14px; line-height: 1.7; min-height: 280px !important; }
.action-column { display: flex; flex-direction: column; gap: 10px; align-items: center; justify-content: center; padding-top: 36px; min-width: 140px; }
.strategy-sel { width: 100%; }
.polish-btn { width: 100%; height: 42px; font-size: 14px; }

.loading-area { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 280px; color: var(--brand-primary); gap: 10px; }
.loading-area p { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }

.result-text {
  background: linear-gradient(135deg, rgba(16,185,129,0.04), rgba(6,182,212,0.04));
  border: 1px solid rgba(16,185,129,0.15); border-radius: var(--radius-sm);
  padding: 16px; font-size: 14px; line-height: 1.8; color: var(--color-text-primary);
  min-height: 280px; max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-word;
}

.result-placeholder { text-align: center; padding: 80px 20px; color: var(--color-text-tertiary); min-height: 280px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.result-placeholder span { font-size: 36px; display: block; margin-bottom: 8px; }
.result-placeholder p { margin: 0; font-size: 13px; }

.history-section {
  background: var(--color-surface); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow-sm); border: 1px solid var(--color-border-light);
}
.history-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.history-label { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 600; color: var(--color-text-secondary); }
.history-table { border-radius: var(--radius-sm); }
.pagination-area { display: flex; justify-content: center; margin-top: 14px; }

.save-preview { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-sm); padding: 12px; font-size: 13px; line-height: 1.6; color: var(--color-text-secondary); max-height: 150px; overflow-y: auto; white-space: pre-wrap; }
</style>
