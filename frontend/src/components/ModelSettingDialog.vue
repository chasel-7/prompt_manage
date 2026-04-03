<template>
  <el-dialog
    v-model="visible"
    title="AI 模型设置"
    width="520px"
    :close-on-click-modal="false"
    class="model-setting-dialog"
  >
    <div class="setting-body">
      <div class="setting-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>配置将保存在本机浏览器中，不会上传到服务器。</span>
      </div>

      <!-- 模型分类选择 -->
      <el-form :model="form" label-position="top" size="default">
        <el-form-item label="选择模型">
          <el-select
            v-model="form.model"
            placeholder="选择或输入模型名称"
            filterable
            allow-create
            class="full-width"
            @change="onModelChange"
          >
            <el-option-group label="OpenAI">
              <el-option v-for="m in modelGroups.openai" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
            <el-option-group label="Anthropic (Claude)">
              <el-option v-for="m in modelGroups.anthropic" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
            <el-option-group label="阿里 通义千问">
              <el-option v-for="m in modelGroups.qwen" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
            <el-option-group label="百度 文心">
              <el-option v-for="m in modelGroups.ernie" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
            <el-option-group label="智谱 GLM">
              <el-option v-for="m in modelGroups.glm" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
            <el-option-group label="月之暗面 Moonshot">
              <el-option v-for="m in modelGroups.moonshot" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
            <el-option-group label="DeepSeek">
              <el-option v-for="m in modelGroups.deepseek" :key="m.value" :label="m.label" :value="m.value" />
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="API Key">
          <el-input
            v-model="form.apiKey"
            type="password"
            show-password
            placeholder="请输入 API Key"
            class="full-width"
          />
        </el-form-item>

        <el-form-item label="Base URL">
          <el-input
            v-model="form.baseUrl"
            placeholder="API 地址，如 https://api.openai.com"
            class="full-width"
          />
          <div class="url-hint" v-if="recommendedUrl">
            推荐：<el-link type="primary" @click="form.baseUrl = recommendedUrl">{{ recommendedUrl }}</el-link>
          </div>
        </el-form-item>
      </el-form>

      <!-- 快速预设 -->
      <div class="presets">
        <span class="presets-label">快速填写 Base URL：</span>
        <div class="preset-chips">
          <el-tag
            v-for="p in presets"
            :key="p.name"
            class="preset-chip"
            type="info"
            effect="plain"
            @click="applyPreset(p)"
          >
            {{ p.name }}
          </el-tag>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleSave">
        <el-icon><Check /></el-icon>
        保存配置
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})
const emit = defineEmits(['update:modelValue', 'saved'])

const STORAGE_KEY = 'prompt_manage_ai_config'

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const form = ref({
  model: '',
  apiKey: '',
  baseUrl: ''
})

// 各厂商推荐 base URL
const MODEL_BASE_URLS = {
  'gpt-4o': 'https://api.openai.com',
  'gpt-4-turbo': 'https://api.openai.com',
  'gpt-3.5-turbo': 'https://api.openai.com',
  'claude-3-5-sonnet-20241022': 'https://api.anthropic.com',
  'claude-3-haiku-20240307': 'https://api.anthropic.com',
  'qwen-plus': 'https://dashscope.aliyuncs.com/compatible-mode',
  'qwen-max': 'https://dashscope.aliyuncs.com/compatible-mode',
  'qwen-turbo': 'https://dashscope.aliyuncs.com/compatible-mode',
  'ERNIE-4.0-8K': 'https://qianfan.baidubce.com/v2',
  'ERNIE-3.5-8K': 'https://qianfan.baidubce.com/v2',
  'glm-4': 'https://open.bigmodel.cn/api/paas',
  'glm-3-turbo': 'https://open.bigmodel.cn/api/paas',
  'moonshot-v1-8k': 'https://api.moonshot.cn',
  'moonshot-v1-32k': 'https://api.moonshot.cn',
  'deepseek-chat': 'https://api.deepseek.com',
  'deepseek-coder': 'https://api.deepseek.com',
}

const recommendedUrl = computed(() => MODEL_BASE_URLS[form.value.model] || '')

const modelGroups = {
  openai: [
    { label: 'GPT-4o', value: 'gpt-4o' },
    { label: 'GPT-4 Turbo', value: 'gpt-4-turbo' },
    { label: 'GPT-3.5 Turbo', value: 'gpt-3.5-turbo' },
  ],
  anthropic: [
    { label: 'Claude 3.5 Sonnet', value: 'claude-3-5-sonnet-20241022' },
    { label: 'Claude 3 Haiku', value: 'claude-3-haiku-20240307' },
  ],
  qwen: [
    { label: 'Qwen Plus（通义千问）', value: 'qwen-plus' },
    { label: 'Qwen Max', value: 'qwen-max' },
    { label: 'Qwen Turbo', value: 'qwen-turbo' },
  ],
  ernie: [
    { label: 'ERNIE 4.0', value: 'ERNIE-4.0-8K' },
    { label: 'ERNIE 3.5', value: 'ERNIE-3.5-8K' },
  ],
  glm: [
    { label: 'GLM-4（智谱）', value: 'glm-4' },
    { label: 'GLM-3 Turbo', value: 'glm-3-turbo' },
  ],
  moonshot: [
    { label: 'Moonshot v1 8k', value: 'moonshot-v1-8k' },
    { label: 'Moonshot v1 32k', value: 'moonshot-v1-32k' },
  ],
  deepseek: [
    { label: 'DeepSeek Chat', value: 'deepseek-chat' },
    { label: 'DeepSeek Coder', value: 'deepseek-coder' },
  ],
}

const presets = [
  { name: 'OpenAI 官方', url: 'https://api.openai.com' },
  { name: '阿里云百炼', url: 'https://dashscope.aliyuncs.com/compatible-mode' },
  { name: '智谱 GLM', url: 'https://open.bigmodel.cn/api/paas' },
  { name: '月之暗面', url: 'https://api.moonshot.cn' },
  { name: 'DeepSeek', url: 'https://api.deepseek.com' },
  { name: '百度千帆', url: 'https://qianfan.baidubce.com/v2' },
]

function applyPreset(p) {
  form.value.baseUrl = p.url
}

function onModelChange(model) {
  const url = MODEL_BASE_URLS[model]
  if (url && !form.value.baseUrl) {
    form.value.baseUrl = url
  }
}

// 打开时从 localStorage 加载
watch(() => props.modelValue, (open) => {
  if (open) {
    loadFromStorage()
  }
})

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const saved = JSON.parse(raw)
      form.value = { ...form.value, ...saved }
    }
  } catch {}
}

function handleSave() {
  if (!form.value.model) {
    ElMessage.warning('请选择或输入模型名称')
    return
  }
  if (!form.value.apiKey) {
    ElMessage.warning('请输入 API Key')
    return
  }
  if (!form.value.baseUrl) {
    ElMessage.warning('请输入 Base URL')
    return
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(form.value))
  ElMessage.success('配置已保存到本机')
  emit('saved', { ...form.value })
  emit('update:modelValue', false)
}

/** 供外部读取当前配置 */
function getConfig() {
  loadFromStorage()
  return { ...form.value }
}

defineExpose({ getConfig })
</script>

<style scoped>
.model-setting-dialog :deep(.el-dialog__header) {
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f5;
}

.setting-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  background: var(--brand-gradient-soft);
  border: 1px solid rgba(99,102,241,0.15);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
}

.full-width {
  width: 100%;
}

.url-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #888;
}

.presets {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 4px;
}

.presets-label {
  font-size: 13px;
  color: #666;
}

.preset-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preset-chip {
  cursor: pointer;
  border-radius: 20px;
  transition: all 0.25s ease;
}

.preset-chip:hover {
  background: var(--brand-primary);
  color: #fff;
  border-color: var(--brand-primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(99,102,241,0.3);
}
</style>
