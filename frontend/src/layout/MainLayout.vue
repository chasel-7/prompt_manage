<template>
  <el-container class="main-layout">
    <!-- 左侧边栏 -->
    <el-aside width="240px" class="layout-aside">
      <div class="logo-area">
        <el-icon :size="24"><Collection /></el-icon>
        <span class="logo-text">提示词词库</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="side-menu"
      >
        <el-menu-item index="/prompts">
          <el-icon><Document /></el-icon>
          <span>提示词管理</span>
        </el-menu-item>
        <el-menu-item index="/filler">
          <el-icon><Connection /></el-icon>
          <span>前后缀填充</span>
        </el-menu-item>
        <el-menu-item index="/polishing">
          <el-icon><MagicStick /></el-icon>
          <span>提示词润色</span>
        </el-menu-item>
        <el-menu-item index="/assistants">
          <el-icon><UserFilled /></el-icon>
          <span>分析助手</span>
        </el-menu-item>
        <el-menu-item index="/trash">
          <el-icon><Delete /></el-icon>
          <span>回收站</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主内容区 -->
    <el-container>
      <el-header class="layout-header">
        <h2 class="page-title">{{ currentTitle }}</h2>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '提示词词库管理')
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.layout-aside {
  background: #1d1e2c;
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 16px;
  font-weight: 600;
  color: #e0e0ff;
}

.side-menu {
  border-right: none;
  background: transparent;
  flex: 1;
  padding-top: 8px;
}

.side-menu .el-menu-item {
  color: #b0b0cc;
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 8px;
}

.side-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.side-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
}

.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
  background: #fff;
  padding: 0 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.layout-main {
  background: #f5f5fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
