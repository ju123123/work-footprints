<template>
  <div class="app">
    <header v-if="showTopbar" class="topbar">
      <div class="brand">工作足迹</div>
      <nav class="nav">
        <RouterLink to="/">记录</RouterLink>
        <RouterLink to="/inbox">整理箱</RouterLink>
        <RouterLink to="/records">列表</RouterLink>
        <RouterLink to="/projects">项目</RouterLink>
        <RouterLink to="/reports">总结</RouterLink>
        <RouterLink to="/stats">统计</RouterLink>
      </nav>
      <div class="user">
        <span class="name">{{ auth.username }}</span>
        <button class="btn" @click="onLogout">退出</button>
      </div>
    </header>
    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const showTopbar = computed(() => route.path !== '/login')

const onLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #fafafa;
  color: #1f2937;
}
.topbar {
  height: 56px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 16px;
}
.brand {
  font-weight: 600;
  letter-spacing: 0.5px;
}
.nav {
  display: flex;
  gap: 12px;
  flex: 1;
}
.nav a {
  text-decoration: none;
  color: #374151;
  padding: 6px 10px;
  border-radius: 8px;
}
.nav a.router-link-active {
  background: #f3f4f6;
  color: #111827;
}
.user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.name {
  color: #6b7280;
  font-size: 14px;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}
.content {
  max-width: 980px;
  margin: 0 auto;
  padding: 18px 18px 40px;
}
</style>
