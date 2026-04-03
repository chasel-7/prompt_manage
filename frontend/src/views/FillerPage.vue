<template>
  <div class="filler-polishing-page">

    <!-- ===== 区域一：前后缀填充 ===== -->
    <section class="section-card">
      <div class="section-header">
        <div class="section-title-group">
          <div class="section-icon filler-ic">
            <el-icon :size="16"><Connection /></el-icon>
          </div>
          <div>
            <h2 class="section-title">提示词组装</h2>
            <p class="section-desc">选择前后缀模板，中间输入自定义内容，实时预览拼接结果</p>
          </div>
        </div>
        <div class="section-actions">
          <el-button round size="small" @click="handleSwap">
            <el-icon><Sort /></el-icon> 交换前后缀
          </el-button>
          <el-button round size="small" @click="handleClear">
            <el-icon><Delete /></el-icon> 清空
          </el-button>
          <el-button type="primary" round size="small" @click="handleCopy">
            <el-icon><CopyDocument /></el-icon> 复制结果
          </el-button>
        </div>
      </div>

      <!-- 三列编辑区 -->
      <div class="editor-row">
        <div class="segment-block" v-for="(block, key) in { prefix, middle, suffix }" :key="key">
          <div class="block-label-bar">
            <span class="block-label" :class="key + '-label'">
              <el-icon><Top v-if="key==='prefix'" /><EditPen v-else-if="key==='middle'" /><Bottom v-else /></el-icon>
              {{ key === 'prefix' ? '前缀 Prefix' : key === 'middle' ? '人工输入' : '后缀 Suffix' }}
            </span>
          </div>
          <div class="title-row">
            <el-autocomplete
              v-model="block.title"
              :fetch-suggestions="searchPrompts"
              :placeholder="key === 'middle' ? '输入或选择标题（可选）' : '输入或选择提示词标题'"
              clearable
              class="title-input"
              value-key="title"
              @select="(item) => onSelectBlock(key, item)"
              @clear="() => onClearBlock(key)"
            />
            <el-button
              v-if="block.title"
              :type="block.selectedId ? 'warning' : 'success'"
              size="small"
              round
              :loading="block.saving"
              @click="handleSaveBlock(key)"
            >
              {{ block.selectedId ? '更新' : '保存' }}
            </el-button>
          </div>
          <el-input
            v-model="block.content"
            type="textarea"
            :rows="key === 'middle' ? 9 : 5"
            :placeholder="key === 'middle' ? '在此输入补充内容（Ctrl+Enter 刷新预览）' : '提示词内容，可直接编辑'"
            class="segment-textarea"
            :class="{ 'main-textarea': key === 'middle' }"
            @keydown="key === 'middle' ? handleKeydown($event) : null"
          />
        </div>
      </div>

      <!-- 实时预览 -->
      <div class="preview-area">
        <div class="preview-bar">
          <span class="preview-label">
            <el-icon><View /></el-icon> 实时预览
          </span>
          <div class="preview-meta">
            <el-tag size="small" effect="plain" round>{{ composedText.length }} 字符</el-tag>
          </div>
        </div>
        <div v-if="composedText" class="preview-content" v-html="highlightedHtml"></div>
        <div v-else class="preview-placeholder">
          <span>🪄</span>
          <p>在上方输入内容，拼接结果将在此实时显示</p>
        </div>
      </div>
    </section>

    <!-- ===== 区域二：提示词润色 ===== -->
    <section class="section-card">
      <div class="section-header">
        <div class="section-title-group">
          <div class="section-icon polish-ic">
            <el-icon :size="16"><MagicStick /></el-icon>
          </div>
          <div>
            <h2 class="section-title">AI 润色</h2>
            <p class="section-desc">利用大模型优化你的提示词，支持多种润色策略</p>
          </div>
        </div>
        <el-button size="small" round @click="showModelSetting = true">
          <el-icon><Setting /></el-icon> 模型设置
        </el-button>
      </div>

      <!-- 润色主体 -->
      <div class="polish-row">
        <div class="text-panel">
          <div class="panel-top">
            <span class="panel-label">📝 原始提示词</span>
          </div>
          <el-input v-model="originalText" type="textarea" :rows="10" placeholder="请输入需要润色的提示词..." class="polish-textarea" />
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
          <el-button v-if="polishedText" size="small" round @click="handleCopyPolished">
            <el-icon><CopyDocument /></el-icon> 复制
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

      <!-- 润色历史 -->
      <div class="history-area">
        <div class="history-bar">
          <span class="history-label"><el-icon><Clock /></el-icon> 润色历史</span>
          <el-button text type="primary" size="small" @click="fetchHistory"><el-icon><Refresh /></el-icon> 刷新</el-button>
        </div>
        <el-table v-loading="historyLoading" :data="historyList" stripe size="small" class="history-table">
          <el-table-column label="策略" width="120">
            <template #default="{ row }"><el-tag size="small" effect="plain" round>{{ row.strategyName }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="originalText" label="原始文本" min-width="180" show-overflow-tooltip />
          <el-table-column prop="polishedText" label="润色结果" min-width="180" show-overflow-tooltip />
          <el-table-column label="时间" width="150">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
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
    </section>

    <ModelSettingDialog v-model="showModelSetting" @saved="onModelSaved" />

    <el-dialog v-model="showSaveDialog" title="保存到提示词词库" width="460px" :close-on-click-modal="false">
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
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listPrompts, createPrompt, updatePrompt } from '../api/prompt'
import { polishText, listPolishingHistory, deletePolishingHistory, saveToLibrary } from '../api/polishing'
import ModelSettingDialog from '../components/ModelSettingDialog.vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const promptList = ref([])
function createBlock() { return reactive({ title: '', content: '', selectedId: null, saving: false }) }
const prefix = createBlock()
const middle = createBlock()
const suffix = createBlock()
const blocks = { prefix, middle, suffix }

onMounted(async () => { await loadPromptList(); fetchHistory() })
async function loadPromptList() { try { const res = await listPrompts({ pageNum:1, pageSize:1000 }); promptList.value = res.data.records || [] } catch{} }
function searchPrompts(query, cb) {
  const results = query ? promptList.value.filter(p => p.title.toLowerCase().includes(query.toLowerCase())) : promptList.value
  cb(results.map(p => ({ ...p, value: p.title })))
}

function onSelectBlock(key, item) { blocks[key].title = item.title; blocks[key].content = item.content; blocks[key].selectedId = item.id }
function onClearBlock(key) { blocks[key].selectedId = null; blocks[key].content = '' }

async function saveBlock(block) {
  if (!block.title.trim()) { ElMessage.warning('请输入提示词标题'); return }
  block.saving = true
  try {
    if (block.selectedId) { await updatePrompt(block.selectedId, { title: block.title, content: block.content }); ElMessage.success('已更新') }
    else {
      const existing = promptList.value.find(p => p.title === block.title.trim())
      if (existing) { await updatePrompt(existing.id, { title: block.title, content: block.content }); block.selectedId = existing.id; ElMessage.success('已更新') }
      else { const res = await createPrompt({ title: block.title, content: block.content }); block.selectedId = res.data.id; ElMessage.success('已保存') }
    }
    await loadPromptList()
  } finally { block.saving = false }
}
function handleSaveBlock(key) { saveBlock(blocks[key]) }

const composedText = computed(() => {
  const parts = []
  if (prefix.content.trim()) parts.push(prefix.content.trim())
  if (middle.content.trim()) parts.push(middle.content.trim())
  if (suffix.content.trim()) parts.push(suffix.content.trim())
  return parts.join('\n\n')
})

const highlightedHtml = computed(() => {
  const text = composedText.value; if (!text) return ''
  const re = /```(\w*)\n([\s\S]*?)```/g; let r='', last=0, m
  while ((m = re.exec(text)) !== null) {
    r += esc(text.substring(last, m.index))
    const lang = m[1], code = m[2]; let h
    try { h = lang && hljs.getLanguage(lang) ? hljs.highlight(code,{language:lang}).value : hljs.highlightAuto(code).value } catch { h = escHtml(code) }
    r += `<pre class="code-block"><code class="hljs">${h}</code></pre>`; last = m.index + m[0].length
  }
  r += esc(text.substring(last)); return r
})
function escHtml(t) { return t.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }
function esc(t) { return t ? escHtml(t).replace(/\n/g,'<br/>') : '' }
function handleSwap() { const t={t:prefix.title,c:prefix.content,i:prefix.selectedId}; prefix.title=suffix.title;prefix.content=suffix.content;prefix.selectedId=suffix.selectedId; suffix.title=t.t;suffix.content=t.c;suffix.selectedId=t.i; ElMessage.success('已交换') }
function handleClear() { [prefix,middle,suffix].forEach(b=>{b.title='';b.content='';b.selectedId=null}); ElMessage.success('已清空') }
function handleCopy() { if(!composedText.value){ElMessage.warning('没有内容');return} navigator.clipboard.writeText(composedText.value).then(()=>ElMessage.success('已复制')).catch(()=>ElMessage.error('失败')) }
function handleKeydown(e) { if((e.ctrlKey||e.metaKey)&&e.key==='Enter'){e.preventDefault();ElMessage.info('预览已刷新')} }

const originalText = ref(''); const polishedText = ref(''); const strategy = ref('professional'); const currentStrategyName = ref(''); const polishing = ref(false)
const historyList = ref([]); const historyTotal = ref(0); const historyPage = ref(1); const historyLoading = ref(false)
const showSaveDialog = ref(false); const saving = ref(false); const saveForm = ref({ title: '', description: '' })
const showModelSetting = ref(false); const currentModelConfig = ref(null)

function loadModelConfig() { try { const r = localStorage.getItem('prompt_manage_ai_config'); if(r) currentModelConfig.value = JSON.parse(r) } catch{} }
onMounted(() => { loadModelConfig() })
function onModelSaved(c) { currentModelConfig.value = {...c} }

async function handlePolish() {
  if(!originalText.value.trim()){ElMessage.warning('请输入');return}
  const cfg = currentModelConfig.value
  if(!cfg||!cfg.apiKey||!cfg.model){ElMessage.warning('请先配置AI模型');showModelSetting.value=true;return}
  polishing.value=true;polishedText.value='';currentStrategyName.value=''
  try { const res = await polishText(originalText.value,strategy.value,cfg);polishedText.value=res.data.polishedText;currentStrategyName.value=res.data.strategyName;ElMessage.success('润色完成');fetchHistory() }
  catch{} finally{polishing.value=false}
}

async function fetchHistory() { historyLoading.value=true; try{const res=await listPolishingHistory({pageNum:historyPage.value,pageSize:10});historyList.value=res.data.records||[];historyTotal.value=res.data.total||0}finally{historyLoading.value=false} }
function loadFromHistory(row) { originalText.value=row.originalText;polishedText.value=row.polishedText;currentStrategyName.value=row.strategyName; const m={professional:'专业术语增强',structured:'逻辑结构化',specific:'语气具体化'}; for(const[k,n] of Object.entries(m)){if(n===row.strategyName){strategy.value=k;break}} window.scrollTo({top:0,behavior:'smooth'}) }
async function handleDeleteHistory(id) { await deletePolishingHistory(id);ElMessage.success('已删除');fetchHistory() }
function handleCopyPolished() { handleCopyText(polishedText.value) }
function handleCopyText(text) { navigator.clipboard.writeText(text).then(()=>ElMessage.success('已复制')).catch(()=>ElMessage.error('失败')) }
async function handleSaveToLibrary() { if(!saveForm.value.title.trim()){ElMessage.warning('请输入标题');return} saving.value=true; try{await saveToLibrary({title:saveForm.value.title,content:polishedText.value,description:saveForm.value.description||null,tagIds:[]});ElMessage.success('已保存');showSaveDialog.value=false;saveForm.value={title:'',description:''};await loadPromptList()}finally{saving.value=false} }
function formatTime(time) { if(!time)return''; const d=new Date(time); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` }
</script>

<style scoped>
.filler-polishing-page { display: flex; flex-direction: column; gap: 20px; min-height: 100%; }

/* Section Card */
.section-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border-light);
}

.section-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 20px; }
.section-title-group { display: flex; align-items: flex-start; gap: 12px; }
.section-icon {
  width: 36px; height: 36px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; margin-top: 2px;
}
.filler-ic { background: var(--brand-gradient); }
.polish-ic { background: linear-gradient(135deg, #10b981, #06b6d4); }
.section-title { font-size: 16px; font-weight: 700; color: var(--color-text-primary); margin: 0; }
.section-desc { font-size: 12px; color: var(--color-text-tertiary); margin: 2px 0 0; }
.section-actions { display: flex; gap: 6px; }

/* Editor */
.editor-row { display: grid; grid-template-columns: 1fr 1.6fr 1fr; gap: 14px; margin-bottom: 16px; }
.segment-block { display: flex; flex-direction: column; gap: 6px; }
.block-label-bar { display: flex; align-items: center; }
.block-label { display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
.prefix-label { color: var(--brand-primary); }
.middle-label { color: #10b981; }
.suffix-label { color: #f59e0b; }
.title-row { display: flex; gap: 6px; align-items: center; }
.title-input { flex: 1; min-width: 0; }
.segment-textarea :deep(.el-textarea__inner) { border-radius: var(--radius-sm); font-family: 'SF Mono','Fira Code','Consolas',monospace; font-size: 13px; line-height: 1.6; resize: vertical; }
.main-textarea :deep(.el-textarea__inner) { min-height: 200px !important; }

/* Preview */
.preview-area { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 16px; }
.preview-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.preview-label { display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 700; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.5px; }
.preview-content { font-size: 13px; line-height: 1.75; color: var(--color-text-primary); max-height: 200px; overflow-y: auto; word-break: break-word; }
.preview-content :deep(.code-block) { margin: 8px 0; border-radius: var(--radius-sm); overflow-x: auto; background: #0f172a; border: none; }
.preview-content :deep(.code-block code) { display: block; padding: 12px; font-size: 12px; line-height: 1.5; color: #e2e8f0; }
.preview-placeholder { text-align: center; padding: 24px; color: var(--color-text-tertiary); }
.preview-placeholder span { font-size: 28px; display: block; margin-bottom: 6px; }
.preview-placeholder p { margin: 0; font-size: 13px; }

/* Polish */
.polish-row { display: grid; grid-template-columns: 1fr auto 1fr; gap: 16px; align-items: start; margin-bottom: 20px; }
.text-panel { display: flex; flex-direction: column; gap: 8px; }
.panel-top { display: flex; justify-content: space-between; align-items: center; }
.panel-label { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); }
.polish-textarea :deep(.el-textarea__inner) { border-radius: var(--radius-sm); font-size: 13px; line-height: 1.7; min-height: 220px !important; }
.action-column { display: flex; flex-direction: column; gap: 8px; align-items: center; justify-content: center; padding-top: 30px; min-width: 130px; }
.strategy-sel { width: 100%; }
.polish-btn { width: 100%; height: 40px; font-size: 13px; }
.loading-area { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 220px; color: var(--brand-primary); gap: 8px; }
.loading-area p { margin: 0; font-size: 13px; color: var(--color-text-tertiary); }
.result-text { background: linear-gradient(135deg, rgba(16,185,129,0.04), rgba(6,182,212,0.04)); border: 1px solid rgba(16,185,129,0.15); border-radius: var(--radius-sm); padding: 14px; font-size: 13px; line-height: 1.75; color: var(--color-text-primary); min-height: 220px; max-height: 360px; overflow-y: auto; white-space: pre-wrap; word-break: break-word; }
.result-placeholder { text-align: center; padding: 60px 20px; color: var(--color-text-tertiary); min-height: 220px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.result-placeholder span { font-size: 32px; display: block; margin-bottom: 8px; }
.result-placeholder p { margin: 0; font-size: 13px; }

/* History */
.history-area { border-top: 1px solid var(--color-border-light); padding-top: 16px; }
.history-bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.history-label { display: flex; align-items: center; gap: 4px; font-size: 13px; font-weight: 600; color: var(--color-text-secondary); }
.history-table { border-radius: var(--radius-sm); }
.pagination-area { display: flex; justify-content: center; margin-top: 12px; }

/* Save Dialog */
.save-preview { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-sm); padding: 10px 12px; font-size: 13px; line-height: 1.6; color: var(--color-text-secondary); max-height: 120px; overflow-y: auto; white-space: pre-wrap; }
</style>
