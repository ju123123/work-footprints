<template>
  <div class="page">
    <div class="header">
      <div class="title">工作记录</div>
      <div class="filters">
        <input v-model="date" class="input" type="date" />
        <select v-model="projectId" class="input">
          <option value="">全部项目</option>
          <option v-for="p in projects" :key="p.id" :value="String(p.id)">{{ p.name }}</option>
        </select>
        <select v-model="status" class="input">
          <option value="">全部状态</option>
          <option value="DRAFT">草稿</option>
          <option value="PENDING">待确认</option>
          <option value="CONFIRMED">已确认</option>
        </select>
        <input v-model="keyword" class="input" placeholder="关键词" />
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
            <span v-if="projectNameById(r.projectId)" class="tag">{{ projectNameById(r.projectId) }}</span>
            <span class="muted">{{ r.recordDate }}</span>
            <span class="muted">{{ r.source }}</span>
          </div>
        </div>
        <div class="ops">
          <button v-if="r.status === 'DRAFT'" class="btn" @click="publish(r.id)">发布</button>
          <button class="btn" @click="openEdit(r)">编辑</button>
          <button class="danger" @click="remove(r.id)">删除</button>
        </div>
      </div>
    </div>

    <div v-if="editing" class="drawer">
      <div class="drawer-card">
        <div class="drawer-title">编辑记录</div>
        <div class="field">
          <label>原始内容</label>
          <textarea v-model="form.rawContent" class="textarea" rows="4" />
        </div>
        <div class="row">
          <div class="field">
            <label>项目</label>
            <select v-model="form.projectId" class="input">
              <option value="">不关联</option>
              <option v-for="p in projects" :key="p.id" :value="String(p.id)">{{ p.name }}</option>
            </select>
          </div>
          <div class="field">
            <label>模块</label>
            <select v-model="form.moduleId" class="input" :disabled="!form.projectId">
              <option value="">不关联</option>
              <option v-for="m in modules" :key="m.id" :value="String(m.id)">{{ m.name }}</option>
            </select>
          </div>
          <div class="field">
            <label>任务</label>
            <select v-model="form.taskId" class="input" :disabled="!form.projectId">
              <option value="">不关联</option>
              <option v-for="t in tasks" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
            </select>
          </div>
        </div>
        <div class="field">
          <label>任务描述</label>
          <textarea v-model="form.taskSummary" class="textarea" rows="3" />
        </div>
        <div class="field">
          <label>成果</label>
          <textarea v-model="form.resultSummary" class="textarea" rows="3" />
        </div>
        <div class="field">
          <label>问题/风险</label>
          <textarea v-model="form.issueSummary" class="textarea" rows="3" />
        </div>
        <div class="field">
          <label>下阶段计划</label>
          <textarea v-model="form.nextPlan" class="textarea" rows="3" />
        </div>
        <div class="row">
          <div class="field">
            <label>状态</label>
            <select v-model="form.status" class="input">
              <option value="DRAFT">草稿</option>
              <option value="PENDING">待确认</option>
              <option value="CONFIRMED">已确认</option>
            </select>
          </div>
        </div>
        <div class="drawer-actions">
          <button class="btn" @click="closeEdit">取消</button>
          <button class="primary" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { onMounted, reactive, ref, watch } from 'vue'
import { api } from '../api/client'
import type { Project, ProjectModule, ProjectTask, WorkRecord } from '../api/types'

const date = ref(dayjs().format('YYYY-MM-DD'))
const keyword = ref('')
const status = ref('')
const projectId = ref('')
const records = ref<WorkRecord[]>([])
const loading = ref(false)
const projects = ref<Project[]>([])
const modules = ref<ProjectModule[]>([])
const tasks = ref<ProjectTask[]>([])

const editing = ref<WorkRecord | null>(null)
const form = reactive({
  rawContent: '',
  projectId: '',
  moduleId: '',
  taskId: '',
  taskSummary: '',
  resultSummary: '',
  issueSummary: '',
  nextPlan: '',
  status: 'CONFIRMED',
})

const load = async () => {
  loading.value = true
  try {
    const params: any = { startDate: date.value, endDate: date.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (status.value) params.status = status.value
    if (projectId.value) params.projectId = projectId.value
    const res = await api.get('/api/records/search', { params })
    records.value = res.data
  } finally {
    loading.value = false
  }
}

const remove = async (id: number) => {
  await api.delete(`/api/records/${id}`)
  await load()
}

const publish = async (id: number) => {
  await api.post(`/api/records/${id}/publish`)
  await load()
}

const loadProjects = async () => {
  const res = await api.get('/api/projects')
  projects.value = res.data
}

const loadModules = async (pid: string) => {
  if (!pid) {
    modules.value = []
    return
  }
  const res = await api.get(`/api/projects/${pid}/modules`)
  modules.value = res.data
}

const loadTasks = async (pid: string, mid: string) => {
  if (!pid) {
    tasks.value = []
    return
  }
  const params: any = {}
  if (mid) params.moduleId = mid
  const res = await api.get(`/api/projects/${pid}/tasks`, { params })
  tasks.value = res.data
}

const openEdit = async (r: WorkRecord) => {
  editing.value = r
  form.rawContent = r.rawContent
  form.projectId = r.projectId ? String(r.projectId) : ''
  form.moduleId = r.moduleId ? String(r.moduleId) : ''
  form.taskId = r.taskId ? String(r.taskId) : ''
  form.taskSummary = r.taskSummary || ''
  form.resultSummary = r.resultSummary || ''
  form.issueSummary = r.issueSummary || ''
  form.nextPlan = r.nextPlan || ''
  form.status = r.status
  await loadModules(form.projectId)
  await loadTasks(form.projectId, form.moduleId)
}

const closeEdit = () => {
  editing.value = null
}

const saveEdit = async () => {
  if (!editing.value) return
  const payload: any = {
    rawContent: form.rawContent,
    taskSummary: form.taskSummary,
    resultSummary: form.resultSummary,
    issueSummary: form.issueSummary,
    nextPlan: form.nextPlan,
    status: form.status,
  }
  payload.projectId = form.projectId ? Number(form.projectId) : null
  payload.moduleId = form.moduleId ? Number(form.moduleId) : null
  payload.taskId = form.taskId ? Number(form.taskId) : null
  await api.patch(`/api/records/${editing.value.id}`, payload)
  editing.value = null
  await load()
}

watch(
  () => form.projectId,
  async (pid) => {
    form.moduleId = ''
    form.taskId = ''
    await loadModules(pid)
    await loadTasks(pid, '')
  }
)

watch(
  () => form.moduleId,
  async (mid) => {
    form.taskId = ''
    await loadTasks(form.projectId, mid)
  }
)

const projectNameById = (id?: number) => {
  if (!id) return ''
  const p = projects.value.find((x) => x.id === id)
  return p?.name || ''
}

onMounted(load)
onMounted(loadProjects)
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
  flex-wrap: wrap;
  justify-content: flex-end;
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
.ops {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
.drawer {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
}
.drawer-card {
  width: 820px;
  max-width: 100%;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  padding: 16px;
}
.drawer-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 10px;
}
label {
  font-size: 13px;
  color: #6b7280;
}
.textarea {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
  resize: vertical;
}
.row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.primary {
  border: 0;
  background: #111827;
  color: #ffffff;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}
</style>
