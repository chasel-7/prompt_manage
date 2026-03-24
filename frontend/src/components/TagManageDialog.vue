<template>
  <el-dialog
    :model-value="visible"
    title="管理标签"
    width="450px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <!-- 新建标签 -->
    <div class="add-tag-row">
      <el-input v-model="newTagName" placeholder="输入新标签名称" class="add-input" @keyup.enter="handleAdd" />
      <el-button type="primary" @click="handleAdd">添加</el-button>
    </div>

    <!-- 标签列表 -->
    <div class="tag-list">
      <el-tag
        v-for="tag in tags"
        :key="tag.id"
        closable
        size="large"
        class="tag-item"
        @close="handleDelete(tag.id)"
      >
        {{ tag.name }}
      </el-tag>
      <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="60" />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTag, deleteTag } from '../api/tag'

const props = defineProps({
  visible: Boolean,
  tags: Array
})

const emit = defineEmits(['update:visible', 'refresh'])

const newTagName = ref('')

async function handleAdd() {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  await createTag(newTagName.value.trim())
  ElMessage.success('标签创建成功')
  newTagName.value = ''
  emit('refresh')
}

async function handleDelete(id) {
  await deleteTag(id)
  ElMessage.success('标签已删除')
  emit('refresh')
}
</script>

<style scoped>
.add-tag-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.add-input {
  flex: 1;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 60px;
}

.tag-item {
  border-radius: 6px;
}
</style>
