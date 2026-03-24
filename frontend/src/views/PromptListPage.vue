<template>
  <div class="prompt-list-page">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索标题、描述、内容..."
          clearable
          prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="selectedGroupId"
          placeholder="按分组筛选"
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
          placeholder="按标签筛选"
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
          :type="showFavoritesOnly ? 'warning' : 'default'"
          @click="toggleFavoritesFilter"
        >
          <el-icon><Star /></el-icon>
          {{ showFavoritesOnly ? '全部' : '收藏' }}
        </el-button>
      </div>
      <div class="action-area">
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建提示词
        </el-button>
        <el-button @click="showGroupDialog = true">
          <el-icon><FolderAdd /></el-icon>
          管理分组
        </el-button>
        <el-button @click="showTagDialog = true">
          <el-icon><PriceTag /></el-icon>
          管理标签
        </el-button>
      </div>
    </div>

    <!-- 提示词卡片列表 -->
    <div v-loading="promptStore.loading" class="card-grid">
      <div
        v-for="prompt in promptStore.prompts"
        :key="prompt.id"
        class="prompt-card"
      >
        <div class="card-header">
          <h3 class="card-title" @click="openEditDialog(prompt)">{{ prompt.title }}</h3>
          <el-icon
            class="fav-icon"
            :class="{ 'is-fav': prompt.isFavorite === 1 }"
            @click="handleFavorite(prompt.id)"
          >
            <StarFilled v-if="prompt.isFavorite === 1" />
            <Star v-else />
          </el-icon>
        </div>
        <p class="card-desc">{{ prompt.description || '暂无描述' }}</p>
        <div class="card-content-preview">{{ truncate(prompt.content, 120) }}</div>
        <div class="card-tags">
          <el-tag
            v-for="tagName in prompt.tagNames"
            :key="tagName"
            size="small"
            type="info"
            class="tag-item"
          >
            {{ tagName }}
          </el-tag>
        </div>
        <div class="card-footer">
          <span class="card-time">{{ formatTime(prompt.updatedAt) }}</span>
          <div class="card-actions">
            <el-button text type="primary" size="small" @click="openEditDialog(prompt)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button text type="primary" size="small" @click="handleCopyContent(prompt.content)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-popconfirm title="确定删除该提示词？" @confirm="handleDelete(prompt.id)">
              <template #reference>
                <el-button text type="danger" size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!promptStore.loading && promptStore.prompts.length === 0"
        description="暂无提示词，点击右上角新建"
        class="empty-state"
      />
    </div>

    <!-- 分页 -->
    <div v-if="promptStore.pagination.total > 0" class="pagination-area">
      <el-pagination
        v-model:current-page="promptStore.pagination.pageNum"
        v-model:page-size="promptStore.pagination.pageSize"
        :total="promptStore.pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
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

    <!-- 分组管理对话框 -->
    <GroupManageDialog
      v-model:visible="showGroupDialog"
      :groups="groupStore.groups"
      @refresh="groupStore.fetchGroups"
    />

    <!-- 标签管理对话框 -->
    <TagManageDialog
      v-model:visible="showTagDialog"
      :tags="tags"
      @refresh="fetchTags"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { usePromptStore } from '../stores/prompt'
import { useGroupStore } from '../stores/group'
import { listTags } from '../api/tag'
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

onMounted(async () => {
  await Promise.all([
    promptStore.fetchPrompts(),
    groupStore.fetchGroups(),
    fetchTags()
  ])
})

async function fetchTags() {
  const res = await listTags()
  tags.value = res.data || []
}

function handleSearch() {
  promptStore.setQuery({ keyword: searchKeyword.value })
}

function handleGroupFilter(val) {
  promptStore.setQuery({ groupId: val || null })
}

function handleTagFilter(val) {
  promptStore.setQuery({ tagId: val || null })
}

function toggleFavoritesFilter() {
  showFavoritesOnly.value = !showFavoritesOnly.value
  promptStore.setQuery({ favorite: showFavoritesOnly.value ? true : null })
}

function handleFavorite(id) {
  promptStore.handleToggleFavorite(id)
}

function handleDelete(id) {
  promptStore.handleDelete(id)
}

function openCreateDialog() {
  editingPrompt.value = null
  showEditDialog.value = true
}

function openEditDialog(prompt) {
  editingPrompt.value = { ...prompt }
  showEditDialog.value = true
}

function handleSaved() {
  showEditDialog.value = false
  promptStore.fetchPrompts()
  fetchTags()
}

function handleCopyContent(content) {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

function truncate(text, maxLen) {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.prompt-list-page {
  min-height: 100%;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 280px;
}

.filter-select {
  width: 160px;
}

.action-area {
  display: flex;
  gap: 8px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.prompt-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: all 0.25s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid transparent;
}

.prompt-card:hover {
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.12);
  border-color: #c7d2fe;
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  cursor: pointer;
  line-height: 1.4;
  flex: 1;
  margin-right: 8px;
}

.card-title:hover {
  color: #6366f1;
}

.fav-icon {
  font-size: 18px;
  color: #ccc;
  cursor: pointer;
  transition: color 0.2s;
  flex-shrink: 0;
}

.fav-icon:hover,
.fav-icon.is-fav {
  color: #f59e0b;
}

.card-desc {
  margin: 0;
  font-size: 13px;
  color: #888;
  line-height: 1.4;
}

.card-content-preview {
  font-size: 13px;
  color: #555;
  background: #f8f8fc;
  border-radius: 8px;
  padding: 10px 12px;
  line-height: 1.6;
  word-break: break-all;
  max-height: 100px;
  overflow: hidden;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-item {
  border-radius: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid #f0f0f5;
}

.card-time {
  font-size: 12px;
  color: #aaa;
}

.card-actions {
  display: flex;
  gap: 2px;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}
</style>
