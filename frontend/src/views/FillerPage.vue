<template>
  <div class="filler-page">
    <!-- 操作区：前缀 + 输入 + 后缀 -->
    <div class="editor-section">
      <div class="editor-row">
        <!-- 前缀选择 -->
        <div class="segment-block prefix-block">
          <div class="segment-label">
            <el-icon><Top /></el-icon>
            <span>前缀 (Prefix)</span>
          </div>
          <el-select
            v-model="prefixId"
            placeholder="选择提示词作为前缀"
            clearable
            filterable
            class="segment-select"
            @change="onPrefixChange"
          >
            <el-option
              v-for="p in promptList"
              :key="p.id"
              :label="p.title"
              :value="p.id"
            />
          </el-select>
          <el-input
            v-model="prefixText"
            type="textarea"
            :rows="4"
            placeholder="选择模板后自动填充，也可手动编辑"
            class="segment-textarea"
          />
        </div>

        <!-- 中间输入 -->
        <div class="segment-block input-block">
          <div class="segment-label">
            <el-icon><EditPen /></el-icon>
            <span>人工输入</span>
          </div>
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="10"
            placeholder="在此输入补充内容（Ctrl+Enter 刷新预览）"
            class="segment-textarea main-input"
            @keydown="handleKeydown"
          />
        </div>

        <!-- 后缀选择 -->
        <div class="segment-block suffix-block">
          <div class="segment-label">
            <el-icon><Bottom /></el-icon>
            <span>后缀 (Suffix)</span>
          </div>
          <el-select
            v-model="suffixId"
            placeholder="选择提示词作为后缀"
            clearable
            filterable
            class="segment-select"
            @change="onSuffixChange"
          >
            <el-option
              v-for="p in promptList"
              :key="p.id"
              :label="p.title"
              :value="p.id"
            />
          </el-select>
          <el-input
            v-model="suffixText"
            type="textarea"
            :rows="4"
            placeholder="选择模板后自动填充，也可手动编辑"
            class="segment-textarea"
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button @click="handleSwap">
          <el-icon><Sort /></el-icon>
          交换前后缀
        </el-button>
        <el-button @click="handleClear">
          <el-icon><Delete /></el-icon>
          清空全部
        </el-button>
        <el-button type="primary" @click="handleCopy">
          <el-icon><CopyDocument /></el-icon>
          一键复制结果
        </el-button>
      </div>
    </div>

    <!-- 实时预览区 -->
    <div class="preview-section">
      <div class="preview-header">
        <h3>
          <el-icon><View /></el-icon>
          实时预览
        </h3>
        <el-tag size="small" type="info">{{ composedText.length }} 字符</el-tag>
      </div>
      <div v-if="composedText" class="preview-content" v-html="highlightedHtml"></div>
      <el-empty v-else description="请在上方输入内容，预览将自动生成" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listPrompts } from '../api/prompt'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const promptList = ref([])
const prefixId = ref(null)
const suffixId = ref(null)
const prefixText = ref('')
const suffixText = ref('')
const userInput = ref('')

/** 加载所有提示词作为可选模板 */
onMounted(async () => {
  try {
    const res = await listPrompts({ pageNum: 1, pageSize: 500 })
    promptList.value = res.data.records || []
  } catch {
    // 静默处理
  }
})

/** 前缀模板变更 */
function onPrefixChange(id) {
  if (id) {
    const found = promptList.value.find(p => p.id === id)
    prefixText.value = found ? found.content : ''
  } else {
    prefixText.value = ''
  }
}

/** 后缀模板变更 */
function onSuffixChange(id) {
  if (id) {
    const found = promptList.value.find(p => p.id === id)
    suffixText.value = found ? found.content : ''
  } else {
    suffixText.value = ''
  }
}

/** 拼接后的完整文本 */
const composedText = computed(() => {
  const parts = []
  if (prefixText.value.trim()) parts.push(prefixText.value.trim())
  if (userInput.value.trim()) parts.push(userInput.value.trim())
  if (suffixText.value.trim()) parts.push(suffixText.value.trim())
  return parts.join('\n\n')
})

/** 高亮渲染 HTML —— 检测代码块并高亮 */
const highlightedHtml = computed(() => {
  const text = composedText.value
  if (!text) return ''

  // 匹配 ```lang\n...\n``` 代码块
  const codeBlockRegex = /```(\w*)\n([\s\S]*?)```/g
  let result = ''
  let lastIndex = 0

  let match
  while ((match = codeBlockRegex.exec(text)) !== null) {
    // 代码块前的普通文本
    const before = text.substring(lastIndex, match.index)
    result += escapeAndFormat(before)

    // 代码块高亮
    const lang = match[1]
    const code = match[2]
    let highlighted
    try {
      highlighted = lang && hljs.getLanguage(lang)
        ? hljs.highlight(code, { language: lang }).value
        : hljs.highlightAuto(code).value
    } catch {
      highlighted = escapeHtml(code)
    }
    result += `<pre class="code-block"><code class="hljs">${highlighted}</code></pre>`
    lastIndex = match.index + match[0].length
  }

  // 剩余文本
  result += escapeAndFormat(text.substring(lastIndex))
  return result
})

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

function escapeAndFormat(text) {
  if (!text) return ''
  return escapeHtml(text).replace(/\n/g, '<br/>')
}

/** 交换前后缀 */
function handleSwap() {
  const tmpId = prefixId.value
  const tmpText = prefixText.value
  prefixId.value = suffixId.value
  prefixText.value = suffixText.value
  suffixId.value = tmpId
  suffixText.value = tmpText
  ElMessage.success('前后缀已交换')
}

/** 清空全部 */
function handleClear() {
  prefixId.value = null
  suffixId.value = null
  prefixText.value = ''
  suffixText.value = ''
  userInput.value = ''
  ElMessage.success('已清空')
}

/** 一键复制 */
function handleCopy() {
  if (!composedText.value) {
    ElMessage.warning('没有内容可复制')
    return
  }
  navigator.clipboard.writeText(composedText.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

/** Ctrl+Enter 刷新预览（computed 已自动响应，这里给个视觉反馈） */
function handleKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault()
    ElMessage.info('预览已刷新')
  }
}
</script>

<style scoped>
.filler-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 100%;
}

.editor-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.editor-row {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 16px;
}

.segment-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.segment-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.prefix-block .segment-label {
  color: #6366f1;
}

.input-block .segment-label {
  color: #059669;
}

.suffix-block .segment-label {
  color: #d97706;
}

.segment-select {
  width: 100%;
}

.segment-textarea :deep(.el-textarea__inner) {
  border-radius: 8px;
  font-family: 'Inter', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.main-input :deep(.el-textarea__inner) {
  min-height: 200px !important;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f5;
}

/* 预览区 */
.preview-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  flex: 1;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 6px;
}

.preview-content {
  background: #f8f8fc;
  border: 1px solid #e8e8f0;
  border-radius: 10px;
  padding: 20px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  min-height: 120px;
  max-height: 500px;
  overflow-y: auto;
  word-break: break-word;
}

.preview-content :deep(.code-block) {
  margin: 12px 0;
  border-radius: 8px;
  overflow-x: auto;
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
}

.preview-content :deep(.code-block code) {
  display: block;
  padding: 14px 16px;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
}
</style>
