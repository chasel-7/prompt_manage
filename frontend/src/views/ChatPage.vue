<template>
  <div class="chat-page">
    <div class="conv-sidebar">
      <div class="conv-header">
        <div class="assistant-info" v-if="assistant">
          <span class="assistant-icon">{{ assistant.icon }}</span>
          <span class="assistant-name">{{ assistant.name }}</span>
        </div>
        <el-button type="primary" size="small" round @click="handleNewConversation">
          <el-icon><Plus /></el-icon> 新对话
        </el-button>
      </div>
      <div class="conv-list">
        <div v-for="c in conversations" :key="c.id" class="conv-item" :class="{ active: currentConvId === c.id }" @click="selectConversation(c.id)">
          <span class="conv-title">{{ c.title }}</span>
          <el-icon class="conv-delete" @click.stop="handleDeleteConv(c.id)"><Close /></el-icon>
        </div>
        <div v-if="conversations.length === 0" class="conv-empty">暂无对话</div>
      </div>
      <div class="conv-footer">
        <el-button text size="small" @click="$router.push('/assistants')">
          <el-icon><Back /></el-icon> 返回助手列表
        </el-button>
      </div>
    </div>

    <div class="chat-main">
      <div v-if="!currentConvId" class="chat-empty">
        <div class="empty-icon">{{ assistant?.icon || '🤖' }}</div>
        <h2>{{ assistant?.name || '助手' }}</h2>
        <p>{{ assistant?.description || '选择或创建一个对话开始' }}</p>
        <el-button type="primary" round @click="handleNewConversation">开始对话</el-button>
      </div>

      <template v-else>
        <div ref="messageListRef" class="message-list">
          <div v-for="msg in messages" :key="msg.id" class="message-item" :class="msg.role">
            <div class="msg-avatar">{{ msg.role === 'user' ? '👤' : (assistant?.icon || '🤖') }}</div>
            <div class="msg-bubble">
              <div class="msg-content" v-html="renderMarkdown(msg.content)"></div>
              <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
          <div v-if="sending" class="message-item assistant">
            <div class="msg-avatar">{{ assistant?.icon || '🤖' }}</div>
            <div class="msg-bubble"><div class="msg-content typing"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div></div>
          </div>
        </div>

        <div class="chat-toolbar">
          <el-button text size="small" @click="handleExport"><el-icon><Download /></el-icon> 导出</el-button>
        </div>

        <div class="input-area">
          <el-input v-model="inputText" type="textarea" :rows="3" :disabled="sending" placeholder="输入消息... (Ctrl+Enter 发送)" class="chat-input" @keydown="handleKeydown" />
          <el-button type="primary" :loading="sending" :disabled="!inputText.trim()" class="send-btn" @click="handleSend">
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssistant } from '../api/assistant'
import { listConversations, createConversation, getMessages, sendMessage, deleteConversation, getExportUrl } from '../api/conversation'

const route = useRoute(); const assistantId = ref(null); const assistant = ref(null); const conversations = ref([]); const currentConvId = ref(null); const messages = ref([]); const inputText = ref(''); const sending = ref(false); const messageListRef = ref(null)

onMounted(async () => { assistantId.value = Number(route.query.assistantId); if(!assistantId.value) return; const res = await getAssistant(assistantId.value); assistant.value = res.data; await fetchConversations() })
async function fetchConversations() { const res = await listConversations(assistantId.value); conversations.value = res.data || [] }
async function selectConversation(id) { currentConvId.value = id; const res = await getMessages(id); messages.value = res.data || []; await nextTick(); scrollToBottom() }
async function handleNewConversation() { const res = await createConversation(assistantId.value, null); conversations.value.unshift(res.data); await selectConversation(res.data.id) }
async function handleSend() { if(!inputText.value.trim()||sending.value)return; const content=inputText.value.trim(); inputText.value=''; messages.value.push({id:Date.now(),conversationId:currentConvId.value,role:'user',content,createdAt:new Date().toISOString()}); await nextTick();scrollToBottom(); sending.value=true; try{const res=await sendMessage(currentConvId.value,content);messages.value.push(res.data);fetchConversations()}catch{}finally{sending.value=false;await nextTick();scrollToBottom()} }
async function handleDeleteConv(id) { await deleteConversation(id);ElMessage.success('已删除');conversations.value=conversations.value.filter(c=>c.id!==id);if(currentConvId.value===id){currentConvId.value=null;messages.value=[]} }
function handleExport() { window.open(getExportUrl(currentConvId.value),'_blank') }
function handleKeydown(e) { if((e.ctrlKey||e.metaKey)&&e.key==='Enter'){e.preventDefault();handleSend()} }
function scrollToBottom() { if(messageListRef.value) messageListRef.value.scrollTop=messageListRef.value.scrollHeight }
function renderMarkdown(text) { if(!text)return''; let h=escHtml(text); h=h.replace(/```(\w*)\n([\s\S]*?)```/g,'<pre class="code-block"><code>$2</code></pre>'); h=h.replace(/`([^`]+)`/g,'<code class="inline-code">$1</code>'); h=h.replace(/\*\*(.+?)\*\*/g,'<strong>$1</strong>'); h=h.replace(/\n/g,'<br/>'); return h }
function escHtml(t) { return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }
function formatTime(time) { if(!time)return''; const d=new Date(time); return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` }
</script>

<style scoped>
.chat-page { display: flex; height: calc(100vh - 112px); background: var(--color-surface); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-md); border: 1px solid var(--color-border-light); }

.conv-sidebar { width: 260px; border-right: 1px solid var(--color-border-light); display: flex; flex-direction: column; background: var(--color-bg); }
.conv-header { padding: 14px 16px; border-bottom: 1px solid var(--color-border-light); display: flex; justify-content: space-between; align-items: center; }
.assistant-info { display: flex; align-items: center; gap: 6px; }
.assistant-icon { font-size: 18px; }
.assistant-name { font-size: 13px; font-weight: 600; color: var(--color-text-primary); }
.conv-list { flex: 1; overflow-y: auto; padding: 6px; }
.conv-item { padding: 8px 10px; border-radius: var(--radius-sm); cursor: pointer; display: flex; justify-content: space-between; align-items: center; transition: background 0.15s; margin-bottom: 2px; }
.conv-item:hover { background: var(--brand-gradient-soft); }
.conv-item.active { background: var(--brand-gradient-soft); border: 1px solid rgba(99,102,241,0.2); }
.conv-title { font-size: 13px; color: var(--color-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-delete { font-size: 14px; color: #d1d5db; opacity: 0; transition: opacity 0.15s; }
.conv-item:hover .conv-delete { opacity: 1; }
.conv-delete:hover { color: var(--color-danger); }
.conv-empty { text-align: center; padding: 24px; color: var(--color-text-tertiary); font-size: 13px; }
.conv-footer { padding: 8px 12px; border-top: 1px solid var(--color-border-light); }

.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--color-text-tertiary); }
.empty-icon { font-size: 48px; }
.chat-empty h2 { margin: 0; color: var(--color-text-primary); font-size: 16px; }
.chat-empty p { margin: 0; font-size: 13px; }

.message-list { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 14px; }
.message-item { display: flex; gap: 10px; max-width: 80%; }
.message-item.user { align-self: flex-end; flex-direction: row-reverse; }
.msg-avatar { font-size: 24px; flex-shrink: 0; width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; background: var(--color-bg); border-radius: 50%; }
.msg-bubble { display: flex; flex-direction: column; gap: 2px; }
.msg-content { padding: 12px 16px; border-radius: var(--radius-md); font-size: 14px; line-height: 1.7; word-break: break-word; }
.user .msg-content { background: var(--brand-gradient); color: #fff; border-bottom-right-radius: 4px; }
.assistant .msg-content { background: var(--color-bg); color: var(--color-text-primary); border-bottom-left-radius: 4px; border: 1px solid var(--color-border-light); }
.msg-time { font-size: 10px; color: var(--color-text-tertiary); padding: 0 4px; }
.user .msg-time { text-align: right; }

.msg-content :deep(.code-block) { margin: 6px 0; background: #0f172a; color: #e2e8f0; border-radius: var(--radius-sm); overflow-x: auto; }
.msg-content :deep(.code-block code) { display: block; padding: 10px; font-family: 'SF Mono','Consolas',monospace; font-size: 13px; line-height: 1.5; }
.msg-content :deep(.inline-code) { background: rgba(0,0,0,0.06); padding: 1px 5px; border-radius: 4px; font-family: 'SF Mono','Consolas',monospace; font-size: 13px; }
.user .msg-content :deep(.inline-code) { background: rgba(255,255,255,0.2); }

.typing { display: flex; gap: 4px; padding: 14px 18px !important; }
.dot { width: 7px; height: 7px; background: var(--brand-primary); border-radius: 50%; animation: dotBounce 1.4s infinite ease-in-out both; }
.dot:nth-child(1) { animation-delay: 0s; } .dot:nth-child(2) { animation-delay: 0.2s; } .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBounce { 0%,80%,100%{transform:scale(0.6);opacity:0.4} 40%{transform:scale(1);opacity:1} }

.chat-toolbar { padding: 4px 16px; border-top: 1px solid var(--color-border-light); display: flex; justify-content: flex-end; }
.input-area { padding: 12px 16px 16px; display: flex; gap: 8px; align-items: flex-end; }
.chat-input { flex: 1; }
.chat-input :deep(.el-textarea__inner) { border-radius: var(--radius-md); font-size: 14px; line-height: 1.6; resize: none; }
.send-btn { height: 42px; width: 42px; border-radius: 50% !important; padding: 0; font-size: 16px; }
</style>
