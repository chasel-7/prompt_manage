<template>
  <div class="assistant-page">
    <div class="top-bar">
      <p class="intro-text">创建自定义 AI 助手，配置系统提示词定义角色身份，然后开始对话。</p>
      <el-button type="primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 新建助手
      </el-button>
    </div>

    <div v-loading="loading" class="assistant-grid">
      <div v-for="a in assistants" :key="a.id" class="assistant-card" @click="enterChat(a)">
        <div class="card-icon-wrap">
          <span class="card-icon">{{ a.icon || '🤖' }}</span>
        </div>
        <h3 class="card-name">{{ a.name }}</h3>
        <p class="card-desc">{{ a.description || '暂无描述' }}</p>
        <div class="card-prompt-preview">{{ truncate(a.systemPrompt, 80) }}</div>
        <div class="card-actions" @click.stop>
          <el-button text type="primary" size="small" @click="openEditDialog(a)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(a.id)">
            <template #reference><el-button text type="danger" size="small">删除</el-button></template>
          </el-popconfirm>
        </div>
      </div>

      <div v-if="!loading && assistants.length === 0" class="empty-state">
        <div class="empty-icon">🤖</div>
        <h3>还没有助手</h3>
        <p>创建一个自定义助手开始使用</p>
        <el-button type="primary" round @click="openCreateDialog"><el-icon><Plus /></el-icon> 新建助手</el-button>
      </div>
    </div>

    <el-dialog v-model="showDialog" :title="editing ? '编辑助手' : '新建助手'" width="560px" :close-on-click-modal="false">
      <el-form :model="form" label-position="top">
        <el-form-item label="图标">
          <div class="icon-picker">
            <span v-for="icon in iconOptions" :key="icon" class="icon-option" :class="{ active: form.icon === icon }" @click="form.icon = icon">{{ icon }}</span>
          </div>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如：资深剧本分析师" maxlength="100" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" placeholder="简要描述助手用途（选填）" maxlength="500" /></el-form-item>
        <el-form-item label="系统提示词 (System Prompt)">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="8" placeholder="定义助手的角色和行为..." class="prompt-editor" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="showDialog = false">取消</el-button>
        <el-button type="primary" round :loading="submitting" @click="handleSubmit">{{ editing ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listAssistants, createAssistant, updateAssistant, deleteAssistant } from '../api/assistant'

const router = useRouter(); const assistants = ref([]); const loading = ref(false); const showDialog = ref(false); const editing = ref(null); const submitting = ref(false)
const iconOptions = ['🤖','📊','💻','📝','🔍','📚','🎯','🧠','⚡','🎨','🔬','📋']
const form = ref({ name:'', description:'', systemPrompt:'', icon:'🤖' })

onMounted(() => fetchAssistants())
async function fetchAssistants() { loading.value=true; try{const res=await listAssistants();assistants.value=res.data||[]}finally{loading.value=false} }
function openCreateDialog() { editing.value=null; form.value={name:'',description:'',systemPrompt:'',icon:'🤖'}; showDialog.value=true }
function openEditDialog(a) { editing.value=a; form.value={name:a.name,description:a.description||'',systemPrompt:a.systemPrompt,icon:a.icon||'🤖'}; showDialog.value=true }
async function handleSubmit() { if(!form.value.name.trim()){ElMessage.warning('请输入名称');return} if(!form.value.systemPrompt.trim()){ElMessage.warning('请输入提示词');return} submitting.value=true; try{if(editing.value){await updateAssistant(editing.value.id,form.value);ElMessage.success('已更新')}else{await createAssistant(form.value);ElMessage.success('已创建')} showDialog.value=false;fetchAssistants()}finally{submitting.value=false} }
async function handleDelete(id) { await deleteAssistant(id);ElMessage.success('已删除');fetchAssistants() }
function enterChat(a) { router.push({path:'/chat',query:{assistantId:a.id}}) }
function truncate(text, max) { return !text?'':text.length>max?text.substring(0,max)+'...':text }
</script>

<style scoped>
.assistant-page { min-height: 100%; }
.top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.intro-text { margin: 0; color: var(--color-text-tertiary); font-size: 13px; }

.assistant-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }

.assistant-card {
  background: var(--color-surface); border-radius: var(--radius-md); padding: 20px; display: flex; flex-direction: column; gap: 8px;
  cursor: pointer; border: 1px solid var(--color-border-light); box-shadow: var(--shadow-sm); transition: all 0.3s ease; position: relative; overflow: hidden;
}
.assistant-card::before { content:''; position:absolute; top:0; left:0; right:0; height:3px; background: var(--brand-gradient); opacity:0; transition: opacity 0.3s; }
.assistant-card:hover { box-shadow: var(--shadow-lg); border-color: var(--brand-primary); transform: translateY(-3px); }
.assistant-card:hover::before { opacity: 1; }

.card-icon-wrap { width: 48px; height: 48px; border-radius: var(--radius-md); background: var(--brand-gradient-soft); display: flex; align-items: center; justify-content: center; }
.card-icon { font-size: 24px; }
.card-name { margin: 0; font-size: 15px; font-weight: 600; color: var(--color-text-primary); }
.card-desc { margin: 0; font-size: 12px; color: var(--color-text-tertiary); line-height: 1.4; }
.card-prompt-preview { font-size: 11px; color: var(--color-text-tertiary); background: var(--color-bg); border-radius: var(--radius-sm); padding: 8px 10px; line-height: 1.5; word-break: break-all; font-family: 'SF Mono','Consolas',monospace; }
.card-actions { display: flex; gap: 4px; margin-top: auto; padding-top: 8px; border-top: 1px solid var(--color-border-light); }

.empty-state { grid-column: 1/-1; text-align: center; padding: 60px 20px; background: var(--color-surface); border-radius: var(--radius-lg); border: 2px dashed var(--color-border); }
.empty-icon { font-size: 40px; margin-bottom: 8px; }
.empty-state h3 { font-size: 15px; color: var(--color-text-primary); margin: 0 0 4px; }
.empty-state p { font-size: 13px; color: var(--color-text-tertiary); margin: 0 0 16px; }

.icon-picker { display: flex; gap: 6px; flex-wrap: wrap; }
.icon-option { font-size: 24px; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); border: 2px solid transparent; transition: all 0.15s; }
.icon-option:hover { background: var(--color-bg); }
.icon-option.active { border-color: var(--brand-primary); background: var(--brand-gradient-soft); }
.prompt-editor :deep(.el-textarea__inner) { font-family: 'SF Mono','Consolas',monospace; font-size: 13px; line-height: 1.6; border-radius: var(--radius-sm); }
</style>
