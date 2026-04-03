<template>
  <div class="prompt-list-page">
    <!-- 统计概览条 -->
    <div class="stats-bar">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #6366f1, #818cf8);">
          <el-icon :size="18"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ promptStore.pagination.total || 0 }}</span>
          <span class="stat-label">提示词总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
          <el-icon :size="18"><Star /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ favoriteCount }}</span>
          <span class="stat-label">收藏数量</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #06b6d4, #22d3ee);">
          <el-icon :size="18"><Folder /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ groupStore.groups.length }}</span>
          <span class="stat-label">分组数量</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
          <el-icon :size="18"><PriceTag /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ tags.length }}</span>
          <span class="stat-label">标签数量</span>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="search-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索提示词..."
            clearable
            prefix-icon="Search"
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </div>
        <el-select
          v-model="selectedGroupId"
          placeholder="全部分组"
          clearable
          class="filter-select"
          @change="handleGroupFilter"
        >
          <el-option
            v-for="g in groupStore.groups"
            :key="g.id"
            :label="`${g.name} (${g.promptCount || 0})`"
            :value="g.id"
          />
        </el-select>
        <el-select
          v-model="selectedTagId"
          placeholder="全部标签"
          clearable
          class="filter-select"
          @change="handleTagFilter"
        >
          <el-option
            v-for="t in tags"
            :key="t.id"
            :label="t.name"
            :value="t.id"
          />
        </el-select>
        <el-button
          :class="{ 'fav-active': showFavoritesOnly }"
          round
          size="small"
          @click="toggleFavoritesFilter"
        >
          <el-icon><StarFilled v-if="showFavoritesOnly" /><Star v-else /></el-icon>
          {{ showFavoritesOnly ? '显示全部' : '仅收藏' }}
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" round @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建提示词
        </el-button>
        <el-dropdown trigger="click">
          <el-button round>
            <el-icon><Operation /></el-icon>
            更多
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="showGroupDialog = true">
                <el-icon><FolderAdd /></el-icon>管理分组
              </el-dropdown-item>
              <el-dropdown-item @click="showTagDialog = true">
                <el-icon><PriceTag /></el-icon>管理标签
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleExport">
                <el-icon><Download /></el-icon>导出 Excel
              </el-dropdown-item>
              <el-dropdown-item @click="showImportDialog = true">
                <el-icon><Upload /></el-icon>导入
              </el-dropdown-item>
              <el-dropdown-item @click="handleDownloadTemplate">
                <el-icon><Document /></el-icon>下载模板
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 提示词卡片列表 -->
    <div v-loading="promptStore.loading" class="card-grid">
      <div
        v-for="prompt in promptStore.prompts"
        :key="prompt.id"
        class="prompt-card"
        @click="openEditDialog(prompt)"
      >
        <div class="card-top">
          <div class="card-title-row">
            <h3 class="card-title">{{ prompt.title }}</h3>
            <el-icon
              class="fav-icon"
              :class="{ 'is-fav': prompt.isFavorite === 1 }"
              @click.stop="handleFavorite(prompt.id)"
            >
              <StarFilled v-if="prompt.isFavorite === 1" />
              <Star v-else />
            </el-icon>
          </div>
          <p class="card-desc">{{ prompt.description || '暂无描述' }}</p>
        </div>
        <div class="card-preview">{{ truncate(prompt.content, 140) }}</div>
        <div class="card-bottom">
          <div class="card-tags">
            <el-tag
              v-for="tagName in (prompt.tagNames || []).slice(0, 3)"
              :key="tagName"
              size="small"
              effect="plain"
              round
              class="tag-chip"
            >
              {{ tagName }}
            </el-tag>
            <span v-if="(prompt.tagNames || []).length > 3" class="tag-more">+{{ prompt.tagNames.length - 3 }}</span>
          </div>
          <div class="card-meta">
            <span class="card-time">{{ formatTime(prompt.updatedAt) }}</span>
          </div>
        </div>
        <!-- 悬浮操作条 -->
        <div class="card-hover-actions" @click.stop>
          <el-tooltip content="编辑" placement="top">
            <el-button circle size="small" @click="openEditDialog(prompt)">
              <el-icon :size="14"><Edit /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="复制内容" placement="top">
            <el-button circle size="small" @click="handleCopyContent(prompt.content)">
              <el-icon :size="14"><CopyDocument /></el-icon>
            </el-button>
          </el-tooltip>
          <el-popconfirm title="确定删除该提示词？" @confirm="handleDelete(prompt.id)">
            <template #reference>
              <el-tooltip content="删除" placement="top">
                <el-button circle size="small" type="danger">
                  <el-icon :size="14"><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!promptStore.loading && promptStore.prompts.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>还没有提示词</h3>
        <p>点击「新建提示词」开始创建你的第一个提示词吧</p>
        <el-button type="primary" round @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建提示词
        </el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="promptStore.pagination.total > 0" class="pagination-area">
      <el-pagination
        v-model:current-page="promptStore.pagination.pageNum"
        v-model:page-size="promptStore.pagination.pageSize"
        :total="promptStore.pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="promptStore.setPage"
        @size-change="promptStore.setPageSize"
      />
    </div>

    <!-- 新建/编辑对话框 -->
    <PromptEditDialog
      v-model:visible="showEditDialog"
      :prompt="editingPrompt"
      :groups="groupStore.groups"
      :tags="tags"
      @saved="handleSaved"
    />
    <GroupManageDialog v-model:visible="showGroupDialog" :groups="groupStore.groups" @refresh="groupStore.fetchGroups" />
    <TagManageDialog v-model:visible="showTagDialog" :tags="tags" @refresh="fetchTags" />

    <!-- 导入对话框 -->
    <el-dialog v-model="showImportDialog" title="导入提示词" width="500px" :close-on-click-modal="false">
      <div class="import-body">
        <el-alert title="导入说明" type="info" :closable="false" style="margin-bottom:14px;border-radius:10px;">
          <template #default>
            <ul style="margin:4px 0 0;padding-left:16px;font-size:13px;line-height:1.8">
              <li>请先下载模板，按格式填写后上传（.xlsx 格式）</li>
              <li><strong>标题</strong>和<strong>内容</strong>为必填项</li>
              <li>标签多个用英文逗号分隔</li>
              <li>分组名称不存在时将自动创建</li>
            </ul>
          </template>
        </el-alert>
        <el-upload class="import-upload" drag :auto-upload="false" accept=".xlsx" :limit="1" :on-change="onImportFileChange" :file-list="importFileList">
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">将 Excel 文件拖到此处，或 <em>点击选择</em></div>
          <template #tip><div class="el-upload__tip">仅支持 .xlsx 格式</div></template>
        </el-upload>
      </div>
      <template #footer>
        <el-button round @click="handleDownloadTemplate"><el-icon><Download /></el-icon> 下载模板</el-button>
        <el-button round @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" round :loading="importing" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { usePromptStore } from '../stores/prompt'
import { useGroupStore } from '../stores/group'
import { listTags } from '../api/tag'
import { exportPrompts, importPrompts, downloadImportTemplate } from '../api/prompt'
import PromptEditDialog from '../components/PromptEditDialog.vue'
import GroupManageDialog from '../components/GroupManageDialog.vue'
import TagManageDialog from '../components/TagManageDialog.vue'

const promptStore = usePromptStore()
const groupStore = useGroupStore()
const searchKeyword = ref('')
const selectedGroupId = ref(null)
const selectedTagId = ref(null)
const showFavoritesOnly = ref(false)
const showEditDialog = ref(false)
const editingPrompt = ref(null)
const showGroupDialog = ref(false)
const showTagDialog = ref(false)
const tags = ref([])
const showImportDialog = ref(false)
const importFileList = ref([])
const importFile = ref(null)
const importing = ref(false)
const exporting = ref(false)

const favoriteCount = computed(() => {
  return promptStore.prompts.filter(p => p.isFavorite === 1).length
})

onMounted(async () => {
  await Promise.all([promptStore.fetchPrompts(), groupStore.fetchGroups(), fetchTags()])
})

async function fetchTags() { const res = await listTags(); tags.value = res.data || [] }
function handleSearch() { promptStore.setQuery({ keyword: searchKeyword.value }) }
function handleGroupFilter(val) { promptStore.setQuery({ groupId: val || null }) }
function handleTagFilter(val) { promptStore.setQuery({ tagId: val || null }) }
function toggleFavoritesFilter() {
  showFavoritesOnly.value = !showFavoritesOnly.value
  promptStore.setQuery({ favorite: showFavoritesOnly.value ? true : null })
}
function handleFavorite(id) { promptStore.handleToggleFavorite(id) }
function handleDelete(id) { promptStore.handleDelete(id) }
function openCreateDialog() { editingPrompt.value = null; showEditDialog.value = true }
function openEditDialog(prompt) { editingPrompt.value = { ...prompt }; showEditDialog.value = true }
function handleSaved() { showEditDialog.value = false; promptStore.fetchPrompts(); fetchTags() }
function handleCopyContent(content) {
  navigator.clipboard.writeText(content).then(() => ElMessage.success('已复制到剪贴板')).catch(() => ElMessage.error('复制失败'))
}
function truncate(text, maxLen) { return !text ? '' : text.length > maxLen ? text.substring(0, maxLen) + '...' : text }
function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function handleExport() {
  exporting.value = true
  try {
    const params = { keyword: promptStore.query.keyword||undefined, groupId: promptStore.query.groupId||undefined, tagId: promptStore.query.tagId||undefined, favorite: promptStore.query.favorite||undefined, pageNum:1, pageSize:10000 }
    const blob = await exportPrompts(params)
    const url = window.URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = `提示词导出_${new Date().toLocaleDateString('zh-CN').replace(/\//g,'-')}.xlsx`; a.click(); window.URL.revokeObjectURL(url); ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') } finally { exporting.value = false }
}
async function handleDownloadTemplate() {
  try { const blob = await downloadImportTemplate(); const url = window.URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = '提示词导入模板.xlsx'; a.click(); window.URL.revokeObjectURL(url); ElMessage.success('模板已下载') }
  catch { ElMessage.error('模板下载失败') }
}
function onImportFileChange(file) { importFile.value = file.raw }
async function handleImport() {
  if (!importFile.value) { ElMessage.warning('请先选择 Excel 文件'); return }
  importing.value = true
  try {
    const res = await importPrompts(importFile.value); const { imported, skipped, failed } = res.data || {}
    ElMessage.success(`导入完成：成功 ${imported??0} 条，跳过 ${skipped??0} 条，失败 ${failed??0} 条`)
    showImportDialog.value = false; importFileList.value = []; importFile.value = null
    promptStore.fetchPrompts(); groupStore.fetchGroups(); fetchTags()
  } catch {} finally { importing.value = false }
}
</script>

<style scoped>
.prompt-list-page { min-height: 100%; }

/* 统计条 */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
  transition: all 0.25s ease;
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 22px; font-weight: 800; color: var(--color-text-primary); letter-spacing: -0.5px; }
.stat-label { font-size: 11px; color: var(--color-text-tertiary); font-weight: 500; margin-top: 1px; }

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.search-wrapper { position: relative; }
.search-input { width: 260px; }
.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-left: 16px;
}

.filter-select { width: 140px; }
.filter-select :deep(.el-input__wrapper) { border-radius: 20px; }

.toolbar-right { display: flex; gap: 8px; }

.fav-active {
  background: linear-gradient(135deg, #fef3c7, #fde68a) !important;
  border-color: #f59e0b !important;
  color: #92400e !important;
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.prompt-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.prompt-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--brand-gradient);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.prompt-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: var(--brand-primary);
  transform: translateY(-3px);
}

.prompt-card:hover::before {
  opacity: 1;
}

.card-top { flex: 0; }

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
  flex: 1;
}

.fav-icon {
  font-size: 16px;
  color: #d1d5db;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.fav-icon:hover, .fav-icon.is-fav {
  color: var(--color-favorite);
  transform: scale(1.15);
}

.card-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
  line-height: 1.4;
}

.card-preview {
  font-size: 13px;
  color: var(--color-text-secondary);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  line-height: 1.65;
  word-break: break-all;
  max-height: 90px;
  overflow: hidden;
  flex: 1;
  position: relative;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
}

.card-preview::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 24px;
  background: linear-gradient(transparent, var(--color-bg));
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid var(--color-border-light);
}

.card-tags { display: flex; gap: 4px; flex-wrap: wrap; }

.tag-chip {
  font-size: 11px !important;
  height: 22px !important;
  padding: 0 8px !important;
  border-radius: 11px !important;
}

.tag-more { font-size: 11px; color: var(--color-text-tertiary); padding: 0 4px; }
.card-time { font-size: 11px; color: var(--color-text-tertiary); white-space: nowrap; }

/* 悬浮操作 */
.card-hover-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transform: translateY(4px);
  transition: all 0.25s ease;
  background: var(--color-surface);
  padding: 4px 6px;
  border-radius: 20px;
  box-shadow: var(--shadow-md);
}

.prompt-card:hover .card-hover-actions {
  opacity: 1;
  transform: translateY(0);
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--color-border);
}

.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state h3 { font-size: 16px; color: var(--color-text-primary); margin: 0 0 6px; }
.empty-state p { font-size: 13px; color: var(--color-text-tertiary); margin: 0 0 16px; }

/* 分页 */
.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 导入上传 */
.import-upload { width: 100%; }
.import-upload :deep(.el-upload) { width: 100%; }
.import-upload :deep(.el-upload-dragger) { width: 100%; border-radius: var(--radius-md); }
</style>
