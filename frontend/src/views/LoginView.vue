<template>
  <div class="wrap">
    <div class="card">
      <h1 class="title">登录</h1>
      <div class="field">
        <label>账号</label>
        <input v-model="username" class="input" autocomplete="username" />
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="password" class="input" type="password" autocomplete="current-password" />
      </div>
      <button class="primary" :disabled="loading" @click="onLogin">登录</button>
      <div v-if="error" class="error">{{ error }}</div>
      <div class="hint">默认账号：admin / admin123（可在后端配置里改）</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const error = ref('')

const onLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const res = await api.post('/api/auth/login', { username: username.value, password: password.value })
    auth.setAuth(res.data.token, res.data.username)
    router.push('/')
  } catch (e: any) {
    error.value = e?.response?.status === 401 ? '账号或密码错误' : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.card {
  width: 420px;
  max-width: 100%;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 24px;
}
.title {
  margin: 0 0 18px;
  font-size: 20px;
  font-weight: 600;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}
label {
  font-size: 13px;
  color: #6b7280;
}
.input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
}
.input:focus {
  border-color: #cbd5e1;
}
.primary {
  width: 100%;
  border: 0;
  border-radius: 10px;
  padding: 10px 12px;
  background: #111827;
  color: #ffffff;
  cursor: pointer;
}
.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error {
  margin-top: 10px;
  color: #b91c1c;
  font-size: 13px;
}
.hint {
  margin-top: 12px;
  color: #6b7280;
  font-size: 12px;
}
</style>

