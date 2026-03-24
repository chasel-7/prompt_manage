<template>
  <div class="chat-page">
    <!-- 左侧：对话列表 -->
    <div class="conv-sidebar">
      <div class="conv-header">
        <div class="assistant-info" v-if="assistant">
          <span class="assistant-icon">{{ assistant.icon }}</span>
          <span class="assistant-name">{{ assistant.name }}</span>
        </div>
        <el-button type="primary" size="small" @click="handleNewConversation">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>
      <div class="conv-list">
        <div
          v-for="c in conversations"
          :key="c.id"
          class="conv-item"
          :class="{ active: currentConvId === c.id }"
          @click="selectConversation(c.id)"
        >
          <span class="conv-title">{{ c.title }}</span>
          <el-icon class="conv-delete" @click.stop="handleDeleteConv(c.id)"><Close /></el-icon>
        </div>
        <div v-if="conversations.length === 0" class="conv-empty">暂无对话</div>
      </div>
      <div class="conv-footer">
        <el-button text size="small" @click="$router.push('/assistants')">
          <el-icon><Back /></el-icon>
          返回助手列表
        </el-button>
      </div>
    </div>

    <!-- 右侧：对话窗口 -->
    <div class="chat-main">
      <div v-if="!currentConvId" class="chat-empty">
        <div class="empty-icon">{{ assistant?.icon || '🤖' }}</div>
        <h2>{{ assistant?.name || '助手' }}</h2>
        <p>{{ assistant?.description || '选择或创建一个对话开始' }}</p>
        <el-button type="primary" @click="handleNewConversation">开始对话</el-button>
      </div>

      <template v-else>
        <!-- 消息列表 -->
        <div ref="messageListRef" class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="msg.role"
          >
            <div class="msg-avatar">
              {{ msg.role === 'user' ? '👤' : (assistant?.icon || '🤖') }}
            </div>
            <div class="msg-bubble">
              <div class="msg-content" v-html="renderMarkdown(msg.content)"></div>
              <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>

          <!-- AI 加载中 -->
          <div v-if="sending" class="message-item assistant">
            <div class="msg-avatar">{{ assistant?.icon || '🤖' }}</div>
            <div class="msg-bubble">
              <div class="msg-content typing">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="chat-toolbar">
          <el-button text size="small" @click="handleExport" title="导出 Markdown">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            :disabled="sending"
            placeholder="输入消息... (Ctrl+Enter 发送)"
            class="chat-input"
            @keydown="handleKeydown"
          />
          <el-button
            type="primary"
            :loading="sending"
            :disabled="!inputText.trim()"
            class="send-btn"
            @click="handleSend"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssistant } from '../api/assistant'
import {
  listConversations, createConversation, getMessages,
  sendMessage, deleteConversation, getExportUrl
} from '../api/conversation'

const route = useRoute()
const assistantId = ref(null)
const assistant = ref(null)
const conversations = ref([])
const currentConvId = ref(null)
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const messageListRef = ref(null)

onMounted(async () => {
  assistantId.value = Number(route.query.assistantId)
  if (!assistantId.value) return

  const res = await getAssistant(assistantId.value)
  assistant.value = res.data

  await fetchConversations()
})

async function fetchConversations() {
  const res = await listConversations(assistantId.value)
  conversations.value = res.data || []
}

async function selectConversation(id) {
  currentConvId.value = id
  const res = await getMessages(id)
  messages.value = res.data || []
  await nextTick()
  scrollToBottom()
}

async function handleNewConversation() {
  const res = await createConversation(assistantId.value, null)
  conversations.value.unshift(res.data)
  await selectConversation(res.data.id)
}

async function handleSend() {
  if (!inputText.value.trim() || sending.value) return

  const content = inputText.value.trim()
  inputText.value = ''

  // 乐观更新：先显示用户消息
  messages.value.push({
    id: Date.now(),
    conversationId: currentConvId.value,
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  })
  await nextTick()
  scrollToBottom()

  sending.value = true
  try {
    const res = await sendMessage(currentConvId.value, content)
    messages.value.push(res.data)
    // 刷新对话列表（标题可能更新）
    fetchConversations()
  } catch {
    // 错误已由拦截器处理
  } finally {
    sending.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function handleDeleteConv(id) {
  await deleteConversation(id)
  ElMessage.success('对话已删除')
  conversations.value = conversations.value.filter(c => c.id !== id)
  if (currentConvId.value === id) {
    currentConvId.value = null
    messages.value = []
  }
}

function handleExport() {
  const url = getExportUrl(currentConvId.value)
  window.open(url, '_blank')
}

function handleKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault()
    handleSend()
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

function renderMarkdown(text) {
  if (!text) return ''
  // 简单 markdown 渲染：代码块、加粗、换行
  let html = escapeHtml(text)
  // 代码块
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre class="code-block"><code>$2</code></pre>')
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  // 加粗
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // 换行
  html = html.replace(/\n/g, '<br/>')
  return html
}

function escapeHtml(text) {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 100px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

/* 左侧对话列表 */
.conv-sidebar {
  width: 260px;
  border-right: 1px solid #f0f0f5;
  display: flex;
  flex-direction: column;
  background: #fafaff;
}

.conv-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assistant-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.assistant-icon {
  font-size: 20px;
}

.assistant-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conv-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.15s;
  margin-bottom: 2px;
}

.conv-item:hover {
  background: #eef2ff;
}

.conv-item.active {
  background: #e0e7ff;
}

.conv-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conv-delete {
  font-size: 14px;
  color: #ccc;
  opacity: 0;
  transition: opacity 0.15s;
}

.conv-item:hover .conv-delete {
  opacity: 1;
}

.conv-delete:hover {
  color: #ef4444;
}

.conv-empty {
  text-align: center;
  padding: 30px;
  color: #aaa;
  font-size: 13px;
}

.conv-footer {
  padding: 12px;
  border-top: 1px solid #f0f0f5;
}

/* 右侧对话区 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #888;
}

.empty-icon {
  font-size: 64px;
}

.chat-empty h2 {
  margin: 0;
  color: #1a1a2e;
}

.chat-empty p {
  margin: 0;
  font-size: 14px;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-avatar {
  font-size: 28px;
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg-bubble {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.msg-content {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.user .msg-content {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.assistant .msg-content {
  background: #f5f5fa;
  color: #333;
  border-bottom-left-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: #bbb;
  padding: 0 4px;
}

.user .msg-time {
  text-align: right;
}

/* 代码块 */
.msg-content :deep(.code-block) {
  margin: 8px 0;
  background: #1e1e2e;
  color: #cdd6f4;
  border-radius: 8px;
  overflow-x: auto;
}

.msg-content :deep(.code-block code) {
  display: block;
  padding: 12px;
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.msg-content :deep(.inline-code) {
  background: rgba(0, 0, 0, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 13px;
}

.user .msg-content :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.2);
}

/* 打字动画 */
.typing {
  display: flex;
  gap: 4px;
  padding: 14px 20px !important;
}

.dot {
  width: 8px;
  height: 8px;
  background: #6366f1;
  border-radius: 50%;
  animation: dotBounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 工具栏 */
.chat-toolbar {
  padding: 4px 20px;
  border-top: 1px solid #f0f0f5;
  display: flex;
  justify-content: flex-end;
}

/* 输入区 */
.input-area {
  padding: 12px 20px 16px;
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
}

.send-btn {
  height: 44px;
  width: 44px;
  border-radius: 12px;
  padding: 0;
  font-size: 18px;
}
</style>
