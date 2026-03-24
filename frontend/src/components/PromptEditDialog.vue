<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑提示词' : '新建提示词'"
    width="680px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入提示词标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="所属分组" prop="groupId">
        <el-select v-model="form.groupId" placeholder="请选择分组" clearable style="width: 100%">
          <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="标签" prop="tagIds">
        <el-select
          v-model="form.tagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入标签"
          style="width: 100%"
        >
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          placeholder="简要描述该提示词的用途（选填）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="提示词内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="10"
          placeholder="请输入提示词内容"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createPrompt, updatePrompt } from '../api/prompt'

const props = defineProps({
  visible: Boolean,
  prompt: Object,
  groups: Array,
  tags: Array
})

const emit = defineEmits(['update:visible', 'saved'])

const formRef = ref(null)
const submitting = ref(false)

const isEdit = computed(() => !!props.prompt?.id)

const form = ref({
  title: '',
  content: '',
  description: '',
  groupId: null,
  tagIds: []
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入提示词内容', trigger: 'blur' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.prompt) {
      form.value = {
        title: props.prompt.title || '',
        content: props.prompt.content || '',
        description: props.prompt.description || '',
        groupId: props.prompt.groupId || null,
        tagIds: props.prompt.tagIds || []
      }
    } else {
      form.value = { title: '', content: '', description: '', groupId: null, tagIds: [] }
    }
    formRef.value?.clearValidate()
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePrompt(props.prompt.id, form.value)
      ElMessage.success('提示词已更新')
    } else {
      await createPrompt(form.value)
      ElMessage.success('提示词已创建')
    }
    emit('saved')
  } finally {
    submitting.value = false
  }
}
</script>
