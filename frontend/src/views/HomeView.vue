<template>
  <div class="page">
    <div class="card">
      <div class="card-title">快速记录</div>
      <textarea v-model="rawContent" class="textarea" rows="5" placeholder="用自然语言输入今天做了什么…" />
      <div class="actions">
        <button class="btn" :disabled="loadingCreate || !rawContent.trim()" @click="onCreateDraft">保存草稿</button>
        <button class="primary" :disabled="loadingCreate || !rawContent.trim()" @click="onCreatePublish">发布并AI整理</button>
        <div class="meta">
          <span>今日记录：{{ records.length }}</span>
          <span>待确认：{{ pending.length }}</span>
        </div>
      </div>
    </div>

    <div class="grid">
      <div class="card">
        <div class="card-title">今日记录</div>
        <div v-if="loading" class="muted">加载中…</div>
        <div v-else-if="records.length === 0" class="muted">暂无记录</div>
        <div v-else class="list">
          <div v-for="r in records" :key="r.id" class="item">
            <div class="line">{{ r.taskSummary || r.rawContent }}</div>
            <div class="sub">
              <span class="tag">{{ r.status }}</span>
              <span class="muted">{{ r.source }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">待确认（AI整理箱）</div>
        <div v-if="loading" class="muted">加载中…</div>
        <div v-else-if="pending.length === 0" class="muted">暂无待确认</div>
        <div v-else class="list">
          <div v-for="r in pending.slice(0, 6)" :key="r.id" class="item">
            <div class="line">{{ r.taskSummary || r.rawContent }}</div>
            <div class="sub">
              <span class="tag">PENDING</span>
              <span class="muted">{{ r.source }}</span>
            </div>
          </div>
          <RouterLink class="link" to="/inbox">进入整理箱 →</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { api } from '../api/client'
import type { WorkRecord } from '../api/types'

const rawContent = ref('')
const records = ref<WorkRecord[]>([])
const pending = ref<WorkRecord[]>([])
const loading = ref(false)
const loadingCreate = ref(false)

const load = async () => {
  loading.value = true
  try {
    const date = dayjs().format('YYYY-MM-DD')
    const [r1, r2] = await Promise.all([api.get('/api/records', { params: { date } }), api.get('/api/records/pending')])
    records.value = r1.data
    pending.value = r2.data
  } finally {
    loading.value = false
  }
}

const onCreateDraft = async () => {
  loadingCreate.value = true
  try {
    await api.post('/api/records', { rawContent: rawContent.value, status: 'DRAFT' })
    rawContent.value = ''
    await load()
  } finally {
    loadingCreate.value = false
  }
}

const onCreatePublish = async () => {
  loadingCreate.value = true
  try {
    await api.post('/api/records', { rawContent: rawContent.value })
    rawContent.value = ''
    await load()
  } finally {
    loadingCreate.value = false
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
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
.card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
}
.card-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  resize: vertical;
  outline: none;
}
.textarea:focus {
  border-color: #cbd5e1;
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}
.primary {
  border: 0;
  border-radius: 10px;
  padding: 10px 12px;
  background: #111827;
  color: #ffffff;
  cursor: pointer;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
}
.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.meta {
  display: flex;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
  margin-left: auto;
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
  color: #374151;
  background: #f3f4f6;
  border-radius: 999px;
  padding: 2px 8px;
}
.muted {
  color: #6b7280;
  font-size: 12px;
}
.link {
  margin-top: 6px;
  color: #111827;
  text-decoration: none;
  font-size: 13px;
}
</style>
