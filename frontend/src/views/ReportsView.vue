<template>
  <div class="page">
    <div class="header">
      <div class="title">工作总结</div>
      <button class="btn" @click="load">刷新</button>
    </div>

    <div class="card">
      <div class="row">
        <select v-model="type" class="input">
          <option value="日报">日报</option>
          <option value="周报">周报</option>
          <option value="月报">月报</option>
        </select>
        <input v-model="startDate" class="input" type="date" />
        <input v-model="endDate" class="input" type="date" />
        <button class="primary" :disabled="loadingGenerate" @click="generate">生成</button>
      </div>
      <textarea v-model="content" class="textarea" rows="14" placeholder="生成结果会出现在这里…" />
      <div class="hint">如果未配置 AI，会生成一个基础拼接版。</div>
    </div>

    <div class="card">
      <div class="card-title">历史总结</div>
      <div v-if="loading" class="muted">加载中…</div>
      <div v-else-if="reports.length === 0" class="muted">暂无历史记录</div>
      <div v-else class="list">
        <button v-for="r in reports" :key="r.id" class="report" @click="select(r)">
          <div class="r-title">{{ r.type }}</div>
          <div class="r-sub">{{ r.startDate }} ~ {{ r.endDate }}</div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import type { Report } from '../api/types'

const type = ref('周报')
const startDate = ref(dayjs().startOf('week').add(1, 'day').format('YYYY-MM-DD'))
const endDate = ref(dayjs().endOf('week').add(1, 'day').format('YYYY-MM-DD'))
const content = ref('')

const reports = ref<Report[]>([])
const loading = ref(false)
const loadingGenerate = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/reports')
    reports.value = res.data
  } finally {
    loading.value = false
  }
}

const generate = async () => {
  loadingGenerate.value = true
  try {
    const res = await api.post('/api/reports/generate', {
      type: type.value,
      startDate: startDate.value,
      endDate: endDate.value,
    })
    content.value = res.data.content
    await load()
  } finally {
    loadingGenerate.value = false
  }
}

const select = (r: Report) => {
  content.value = r.content
}

onMounted(load)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-weight: 600;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
}
.card-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 10px;
}
.input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 10px;
}
.primary {
  border: 0;
  border-radius: 10px;
  padding: 8px 12px;
  background: #111827;
  color: #ffffff;
  cursor: pointer;
}
.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  resize: vertical;
  outline: none;
}
.hint {
  margin-top: 10px;
  color: #6b7280;
  font-size: 12px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.report {
  text-align: left;
  border: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 12px;
  padding: 10px 12px;
  cursor: pointer;
}
.r-title {
  font-weight: 600;
  font-size: 13px;
  color: #111827;
}
.r-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}
.muted {
  color: #6b7280;
  font-size: 13px;
}
</style>

