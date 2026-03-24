<template>
  <div class="assistant-page">
    <!-- 顶部 -->
    <div class="top-bar">
      <p class="intro-text">创建自定义 AI 助手，配置系统提示词定义角色身份，然后开始对话。</p>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新建助手
      </el-button>
    </div>

    <!-- 助手卡片网格 -->
    <div v-loading="loading" class="assistant-grid">
      <div
        v-for="a in assistants"
        :key="a.id"
        class="assistant-card"
        @click="enterChat(a)"
      >
        <div class="card-icon">{{ a.icon || '🤖' }}</div>
        <h3 class="card-name">{{ a.name }}</h3>
        <p class="card-desc">{{ a.description || '暂无描述' }}</p>
        <div class="card-prompt-preview">{{ truncate(a.systemPrompt, 80) }}</div>
        <div class="card-actions" @click.stop>
          <el-button text type="primary" size="small" @click="openEditDialog(a)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-popconfirm title="确定删除该助手？" @confirm="handleDelete(a.id)">
            <template #reference>
              <el-button text type="danger" size="small">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <el-empty
        v-if="!loading && assistants.length === 0"
        description="还没有助手，点击右上角创建一个"
        class="empty-state"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="editing ? '编辑助手' : '新建助手'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="图标">
          <div class="icon-picker">
            <span
              v-for="icon in iconOptions"
              :key="icon"
              class="icon-option"
              :class="{ active: form.icon === icon }"
              @click="form.icon = icon"
            >{{ icon }}</span>
          </div>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="如：资深剧本分析师" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="简要描述助手用途（选填）" maxlength="500" />
        </el-form-item>
        <el-form-item label="系统提示词 (System Prompt)">
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="8"
            placeholder="定义助手的角色和行为，例如：&#10;你是一位资深剧本分析师。你擅长分析剧本的剧情结构、人物动机和主题思想。&#10;&#10;支持模板变量：{{user_input}}"
            class="prompt-editor"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editing ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listAssistants, createAssistant, updateAssistant, deleteAssistant } from '../api/assistant'

const router = useRouter()
const assistants = ref([])
const loading = ref(false)
const showDialog = ref(false)
const editing = ref(null)
const submitting = ref(false)

const iconOptions = ['🤖', '📊', '💻', '📝', '🔍', '📚', '🎯', '🧠', '⚡', '🎨', '🔬', '📋']

const form = ref({
  name: '',
  description: '',
  systemPrompt: '',
  icon: '🤖'
})

onMounted(() => fetchAssistants())

async function fetchAssistants() {
  loading.value = true
  try {
    const res = await listAssistants()
    assistants.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editing.value = null
  form.value = { name: '', description: '', systemPrompt: '', icon: '🤖' }
  showDialog.value = true
}

function openEditDialog(a) {
  editing.value = a
  form.value = {
    name: a.name,
    description: a.description || '',
    systemPrompt: a.systemPrompt,
    icon: a.icon || '🤖'
  }
  showDialog.value = true
}

async function handleSubmit() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入助手名称')
    return
  }
  if (!form.value.systemPrompt.trim()) {
    ElMessage.warning('请输入系统提示词')
    return
  }
  submitting.value = true
  try {
    if (editing.value) {
      await updateAssistant(editing.value.id, form.value)
      ElMessage.success('助手已更新')
    } else {
      await createAssistant(form.value)
      ElMessage.success('助手已创建')
    }
    showDialog.value = false
    fetchAssistants()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await deleteAssistant(id)
  ElMessage.success('助手已删除')
  fetchAssistants()
}

function enterChat(assistant) {
  router.push({ path: '/chat', query: { assistantId: assistant.id } })
}

function truncate(text, maxLen) {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}
</script>

<style scoped>
.assistant-page {
  min-height: 100%;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.intro-text {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.assistant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.assistant-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid transparent;
}

.assistant-card:hover {
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.15);
  border-color: #c7d2fe;
  transform: translateY(-3px);
}

.card-icon {
  font-size: 40px;
  line-height: 1;
}

.card-name {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
}

.card-desc {
  margin: 0;
  font-size: 13px;
  color: #888;
  line-height: 1.4;
}

.card-prompt-preview {
  font-size: 12px;
  color: #999;
  background: #f8f8fc;
  border-radius: 6px;
  padding: 8px 10px;
  line-height: 1.5;
  word-break: break-all;
}

.card-actions {
  display: flex;
  gap: 4px;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid #f0f0f5;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

/* Icon Picker */
.icon-picker {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.icon-option {
  font-size: 28px;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.15s;
}

.icon-option:hover {
  background: #f0f0ff;
}

.icon-option.active {
  border-color: #6366f1;
  background: #eef2ff;
}

.prompt-editor :deep(.el-textarea__inner) {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>
