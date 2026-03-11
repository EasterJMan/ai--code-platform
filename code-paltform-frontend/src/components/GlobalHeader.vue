<template>
  <a-layout-header class="header">
    <a-row :wrap="false" align="middle" class="header-row">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">智能应用平台</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <a-button type="primary" @click="goLogin">登录</a-button>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { globalHeaderMenus } from '@/config/menu'

const router = useRouter()
const route = useRoute()

// 当前选中菜单
const selectedKeys = ref<string[]>([route.path])

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
  { immediate: true },
)

// 菜单项（支持通过配置集中维护）
const menuItems = computed<MenuProps['items']>(() => globalHeaderMenus)

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const goLogin = async () => {
  await router.push('/user/login')
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
  height: 64px;
  line-height: 64px;
  display: flex;
  align-items: center;
}

.header-row {
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
}

.user-login-status {
  display: flex;
  align-items: center;
  height: 64px;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}
</style>
