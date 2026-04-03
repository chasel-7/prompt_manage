<template>
  <el-container class="main-layout">
    <!-- 沉浸式侧边栏 -->
    <el-aside width="240px" class="layout-aside">
      <!-- Logo -->
      <div class="logo-area">
        <div class="logo-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <rect width="24" height="24" rx="6" fill="url(#logo-grad)"/>
            <path d="M7 8h10M7 12h7M7 16h10" stroke="white" stroke-width="2" stroke-linecap="round"/>
            <defs>
              <linearGradient id="logo-grad" x1="0" y1="0" x2="24" y2="24">
                <stop stop-color="#6366f1"/>
                <stop offset="1" stop-color="#06b6d4"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="logo-info">
          <span class="logo-text">PromptVault</span>
          <span class="logo-version">v1.0</span>
        </div>
      </div>

      <!-- 导航分组 -->
      <div class="nav-section">
        <div class="nav-group-label">工作台</div>
        <nav class="side-nav">
          <router-link
            to="/filler"
            class="nav-item"
            :class="{ active: activeMenu === '/filler' }"
          >
            <el-icon><Connection /></el-icon>
            <span>提示词组装</span>
            <span class="nav-badge hot">HOT</span>
          </router-link>
          <router-link
            to="/prompts"
            class="nav-item"
            :class="{ active: activeMenu === '/prompts' }"
          >
            <el-icon><Document /></el-icon>
            <span>提示词库</span>
          </router-link>
        </nav>

        <div class="nav-group-label">系统</div>
        <nav class="side-nav">
          <router-link
            to="/trash"
            class="nav-item"
            :class="{ active: activeMenu === '/trash' }"
          >
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </router-link>
        </nav>
      </div>

      <!-- 底部装饰 -->
      <div class="sidebar-footer">
        <div class="sidebar-glow"></div>
      </div>
    </el-aside>

    <!-- 右侧主内容区 -->
    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentTitle }}</h1>
          <span class="page-desc" v-if="currentDesc">{{ currentDesc }}</span>
        </div>
        <div class="header-right">
          <div class="header-stat">
            <span class="stat-dot"></span>
            <span class="stat-text">系统运行中</span>
          </div>
        </div>
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

const descMap = {
  '/filler': '组装前后缀 + AI润色，一站式提示词工作台',
  '/prompts': '管理你的提示词资产，支持分组、标签和导入导出',
  '/trash': '已删除的提示词，支持恢复或永久清除'
}
const currentDesc = computed(() => descMap[route.path] || '')
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.layout-aside {
  background: var(--sidebar-bg);
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  border-right: 1px solid rgba(255,255,255,0.06);
}

/* Logo */
.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}

.logo-icon {
  flex-shrink: 0;
}

.logo-info {
  display: flex;
  flex-direction: column;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.3px;
}

.logo-version {
  font-size: 10px;
  color: rgba(255,255,255,0.35);
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 导航 */
.nav-section {
  flex: 1;
  padding: 8px 12px;
  overflow-y: auto;
}

.nav-group-label {
  font-size: 10px;
  font-weight: 700;
  color: rgba(255,255,255,0.25);
  text-transform: uppercase;
  letter-spacing: 1.2px;
  padding: 16px 8px 6px;
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 12px;
  border-radius: var(--radius-sm);
  color: var(--sidebar-text);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  position: relative;
}

.nav-item:hover {
  background: var(--sidebar-surface);
  color: rgba(255,255,255,0.9);
}

.nav-item.active {
  background: var(--brand-gradient);
  color: #fff;
  box-shadow: 0 2px 12px rgba(99,102,241,0.3);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #fff;
  border-radius: 0 3px 3px 0;
}

.nav-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  margin-left: auto;
  letter-spacing: 0.5px;
}

.nav-badge.hot {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: #fff;
}

/* 底部光晕 */
.sidebar-footer {
  flex-shrink: 0;
  padding: 20px;
  position: relative;
}

.sidebar-glow {
  height: 80px;
  background: radial-gradient(ellipse at center bottom, rgba(99,102,241,0.15) 0%, transparent 70%);
  pointer-events: none;
}

/* ===== 主内容区 ===== */
.main-container {
  background: var(--color-bg);
}

.layout-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
  padding: 0 28px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
  letter-spacing: -0.3px;
}

.page-desc {
  font-size: 12px;
  color: var(--color-text-tertiary);
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #ecfdf5;
  border-radius: 20px;
}

.stat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  animation: pulse 2s infinite;
}

.stat-text {
  font-size: 11px;
  color: #059669;
  font-weight: 600;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.layout-main {
  background: var(--color-bg);
  padding: 24px 28px;
  overflow-y: auto;
}
</style>
