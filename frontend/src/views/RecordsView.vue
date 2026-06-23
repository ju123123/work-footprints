<template>
  <div class="page">
    <div class="header">
      <div class="title">工作记录</div>
      <div class="filters">
        <input v-model="date" class="input" type="date" />
        <button class="btn" @click="load">查询</button>
      </div>
    </div>

    <div v-if="loading" class="muted">加载中…</div>
    <div v-else-if="records.length === 0" class="muted">暂无记录</div>
    <div v-else class="list">
      <div v-for="r in records" :key="r.id" class="item">
        <div class="main">
          <div class="line">{{ r.taskSummary || r.rawContent }}</div>
          <div class="sub">
            <span class="tag">{{ r.status }}</span>
            <span class="muted">{{ r.recordDate }}</span>
            <span class="muted">{{ r.source }}</span>
          </div>
        </div>
        <button class="danger" @click="remove(r.id)">删除</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import type { WorkRecord } from '../api/types'

const date = ref(dayjs().format('YYYY-MM-DD'))
const records = ref<WorkRecord[]>([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/records', { params: { date: date.value } })
    records.value = res.data
  } finally {
    loading.value = false
  }
}

const remove = async (id: number) => {
  await api.delete(`/api/records/${id}`)
  await load()
}

onMounted(load)
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.title {
  font-weight: 600;
}
.filters {
  display: flex;
  gap: 10px;
  align-items: center;
}
.input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 10px;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.main {
  flex: 1;
}
.line {
  font-size: 14px;
  color: #111827;
  white-space: pre-wrap;
}
.sub {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.tag {
  font-size: 12px;
  background: #f3f4f6;
  border-radius: 999px;
  padding: 2px 8px;
}
.danger {
  border: 1px solid #fecaca;
  background: #fff1f2;
  color: #b91c1c;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.muted {
  color: #6b7280;
  font-size: 12px;
}
</style>

