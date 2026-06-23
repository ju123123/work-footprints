<template>
  <div class="page">
    <div class="header">
      <div class="title">统计</div>
      <button class="btn" @click="load">刷新</button>
    </div>

    <div class="card">
      <div class="row">
        <input v-model="startDate" class="input" type="date" />
        <input v-model="endDate" class="input" type="date" />
        <button class="btn" @click="load">查询</button>
      </div>
      <div v-if="loading" class="muted">加载中…</div>
      <div v-else class="metrics">
        <div class="metric">
          <div class="k">总记录</div>
          <div class="v">{{ stats?.total || 0 }}</div>
        </div>
        <div class="metric">
          <div class="k">草稿</div>
          <div class="v">{{ stats?.draft || 0 }}</div>
        </div>
        <div class="metric">
          <div class="k">待确认</div>
          <div class="v">{{ stats?.pending || 0 }}</div>
        </div>
        <div class="metric">
          <div class="k">已确认</div>
          <div class="v">{{ stats?.confirmed || 0 }}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">按项目统计</div>
      <div v-if="loading" class="muted">加载中…</div>
      <div v-else-if="!stats || stats.byProject.length === 0" class="muted">暂无数据</div>
      <div v-else class="list">
        <div v-for="p in stats.byProject" :key="p.projectId" class="item">
          <div class="name">{{ p.projectName }}</div>
          <div class="count">{{ p.count }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import type { RecordStats } from '../api/types'

const startDate = ref(dayjs().startOf('week').add(1, 'day').format('YYYY-MM-DD'))
const endDate = ref(dayjs().endOf('week').add(1, 'day').format('YYYY-MM-DD'))

const loading = ref(false)
const stats = ref<RecordStats | null>(null)

const load = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/stats/records', { params: { startDate: startDate.value, endDate: endDate.value } })
    stats.value = res.data
  } finally {
    loading.value = false
  }
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
  align-items: center;
  flex-wrap: wrap;
}
.input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 10px;
}
.metrics {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
@media (max-width: 900px) {
  .metrics {
    grid-template-columns: repeat(2, 1fr);
  }
}
.metric {
  border: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 12px;
  padding: 12px;
}
.k {
  font-size: 12px;
  color: #6b7280;
}
.v {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 600;
  color: #111827;
}
.muted {
  color: #6b7280;
  font-size: 13px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item {
  border: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.name {
  font-size: 13px;
  font-weight: 600;
}
.count {
  font-size: 13px;
  color: #111827;
}
</style>

