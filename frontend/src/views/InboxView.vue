<template>
  <div class="page">
    <div class="header">
      <div class="title">AI 整理箱</div>
      <button class="btn" @click="load">刷新</button>
    </div>

    <div v-if="loading" class="muted">加载中…</div>
    <div v-else-if="pending.length === 0" class="muted">暂无待确认记录</div>

    <div v-else class="list">
      <div v-for="r in pending" :key="r.id" class="item">
        <div class="main">
          <div class="raw">{{ r.rawContent }}</div>
          <div class="fields">
            <div class="f"><span class="k">项目</span><span class="v">{{ r.projectName || '-' }}</span></div>
            <div class="f"><span class="k">模块</span><span class="v">{{ r.moduleName || '-' }}</span></div>
            <div class="f"><span class="k">任务</span><span class="v">{{ r.taskSummary || '-' }}</span></div>
          </div>
        </div>
        <div class="ops">
          <button class="btn" @click="openEdit(r)">编辑</button>
          <button class="primary" @click="confirm(r.id)">确认</button>
        </div>
      </div>
    </div>

    <div v-if="editing" class="drawer">
      <div class="drawer-card">
        <div class="drawer-title">编辑并确认</div>
        <div class="row">
          <div class="field">
            <label>关联项目</label>
            <select v-model="form.projectId" class="input">
              <option value="">不关联</option>
              <option v-for="p in projects" :key="p.id" :value="String(p.id)">{{ p.name }}</option>
            </select>
          </div>
          <div class="field">
            <label>关联模块</label>
            <select v-model="form.moduleId" class="input" :disabled="!form.projectId">
              <option value="">不关联</option>
              <option v-for="m in modules" :key="m.id" :value="String(m.id)">{{ m.name }}</option>
            </select>
          </div>
          <div class="field">
            <label>关联任务</label>
            <select v-model="form.taskId" class="input" :disabled="!form.projectId">
              <option value="">不关联</option>
              <option v-for="t in tasks" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
            </select>
          </div>
        </div>
        <div class="field">
          <label>项目</label>
          <input v-model="form.projectName" class="input" />
        </div>
        <div class="field">
          <label>模块</label>
          <input v-model="form.moduleName" class="input" />
        </div>
        <div class="field">
          <label>任务</label>
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
        <div class="drawer-actions">
          <button class="btn" @click="closeEdit">取消</button>
          <button class="primary" @click="saveAndConfirm">保存并确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { api } from '../api/client'
import type { Project, ProjectModule, ProjectTask, WorkRecord } from '../api/types'

const pending = ref<WorkRecord[]>([])
const loading = ref(false)
const projects = ref<Project[]>([])
const modules = ref<ProjectModule[]>([])
const tasks = ref<ProjectTask[]>([])

const editing = ref<WorkRecord | null>(null)
const form = reactive({
  projectId: '',
  moduleId: '',
  taskId: '',
  projectName: '',
  moduleName: '',
  taskSummary: '',
  resultSummary: '',
  issueSummary: '',
  nextPlan: '',
})

const load = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/records/pending')
    pending.value = res.data
  } finally {
    loading.value = false
  }
}

const confirm = async (id: number) => {
  await api.patch(`/api/records/${id}`, { status: 'CONFIRMED' })
  await load()
}

const openEdit = (r: WorkRecord) => {
  editing.value = r
  form.projectId = r.projectId ? String(r.projectId) : ''
  form.moduleId = r.moduleId ? String(r.moduleId) : ''
  form.taskId = r.taskId ? String(r.taskId) : ''
  form.projectName = r.projectName || ''
  form.moduleName = r.moduleName || ''
  form.taskSummary = r.taskSummary || ''
  form.resultSummary = r.resultSummary || ''
  form.issueSummary = r.issueSummary || ''
  form.nextPlan = r.nextPlan || ''
}

const closeEdit = () => {
  editing.value = null
}

const saveAndConfirm = async () => {
  if (!editing.value) return
  const payload: any = { ...form, status: 'CONFIRMED' }
  payload.projectId = form.projectId ? Number(form.projectId) : null
  payload.moduleId = form.moduleId ? Number(form.moduleId) : null
  payload.taskId = form.taskId ? Number(form.taskId) : null
  await api.patch(`/api/records/${editing.value.id}`, payload)
  editing.value = null
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
}
.title {
  font-weight: 600;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.item {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}
.main {
  flex: 1;
  min-width: 0;
}
.raw {
  font-size: 14px;
  white-space: pre-wrap;
  color: #111827;
}
.fields {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
}
.f {
  display: flex;
  gap: 10px;
  font-size: 13px;
  color: #374151;
}
.k {
  width: 36px;
  color: #6b7280;
}
.v {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ops {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.primary {
  border: 0;
  background: #111827;
  color: #ffffff;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.muted {
  color: #6b7280;
  font-size: 13px;
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
  width: 720px;
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
.row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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
.input,
.textarea {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
}
.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}
</style>
