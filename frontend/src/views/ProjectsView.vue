<template>
  <div class="page">
    <div class="header">
      <div class="title">项目管理</div>
      <button class="btn" @click="load">刷新</button>
    </div>

    <div class="grid">
      <div class="card">
        <div class="card-title">项目列表</div>
        <div class="create">
          <input v-model="newProjectName" class="input" placeholder="项目名称" />
          <input v-model="newProjectDesc" class="input" placeholder="描述（可选）" />
          <button class="primary" :disabled="!newProjectName.trim() || creating" @click="createProject">新增</button>
        </div>
        <div v-if="projects.length === 0" class="muted">暂无项目</div>
        <div v-else class="list">
          <button
            v-for="p in projects"
            :key="p.id"
            class="row-btn"
            :class="{ active: selectedProject?.id === p.id }"
            @click="selectProject(p)"
          >
            <div class="p-name">{{ p.name }}</div>
            <div class="p-sub muted">{{ p.status }}</div>
          </button>
        </div>
      </div>

      <div class="card">
        <div class="card-title">项目详情</div>
        <div v-if="!selectedProject" class="muted">请选择一个项目</div>
        <div v-else class="detail">
          <div class="field">
            <label>名称</label>
            <input v-model="editProject.name" class="input" />
          </div>
          <div class="field">
            <label>描述</label>
            <textarea v-model="editProject.description" class="textarea" rows="3" />
          </div>
          <div class="row">
            <div class="field">
              <label>状态</label>
              <select v-model="editProject.status" class="input">
                <option value="ACTIVE">ACTIVE</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </select>
            </div>
          </div>
          <div class="actions">
            <button class="primary" :disabled="saving" @click="saveProject">保存</button>
            <button class="danger" :disabled="saving" @click="deleteProject">删除</button>
          </div>

          <div class="split">
            <div class="box">
              <div class="box-title">模块</div>
              <div class="create">
                <input v-model="newModuleName" class="input" placeholder="模块名称" />
                <button class="btn" :disabled="!newModuleName.trim()" @click="createModule">新增模块</button>
              </div>
              <div v-if="modules.length === 0" class="muted">暂无模块</div>
              <div v-else class="pill-list">
                <div v-for="m in modules" :key="m.id" class="pill">
                  <span>{{ m.name }}</span>
                  <button class="mini" @click="removeModule(m.id)">删除</button>
                </div>
              </div>
            </div>

            <div class="box">
              <div class="box-title">任务</div>
              <div class="task-form">
                <input v-model="newTaskName" class="input" placeholder="任务名称" />
                <select v-model="newTaskModuleId" class="input">
                  <option value="">不关联模块</option>
                  <option v-for="m in modules" :key="m.id" :value="String(m.id)">{{ m.name }}</option>
                </select>
                <select v-model="newTaskStatus" class="input">
                  <option value="TODO">TODO</option>
                  <option value="DOING">DOING</option>
                  <option value="DONE">DONE</option>
                  <option value="BLOCKED">BLOCKED</option>
                </select>
                <button class="btn" :disabled="!newTaskName.trim()" @click="createTask">新增任务</button>
              </div>
              <div v-if="tasks.length === 0" class="muted">暂无任务</div>
              <div v-else class="task-list">
                <div v-for="t in tasks" :key="t.id" class="task">
                  <div class="t-main">
                    <div class="t-name">{{ t.name }}</div>
                    <div class="t-sub">
                      <span class="tag">{{ t.status }}</span>
                      <span v-if="moduleNameById(t.moduleId)" class="muted">{{ moduleNameById(t.moduleId) }}</span>
                    </div>
                  </div>
                  <button class="mini" @click="removeTask(t.id)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'
import type { Project, ProjectModule, ProjectTask } from '../api/types'

const projects = ref<Project[]>([])
const modules = ref<ProjectModule[]>([])
const tasks = ref<ProjectTask[]>([])

const selectedProject = ref<Project | null>(null)
const creating = ref(false)
const saving = ref(false)

const newProjectName = ref('')
const newProjectDesc = ref('')
const newModuleName = ref('')

const newTaskName = ref('')
const newTaskModuleId = ref('')
const newTaskStatus = ref('TODO')

const editProject = reactive({
  name: '',
  description: '',
  status: 'ACTIVE',
})

const load = async () => {
  const res = await api.get('/api/projects')
  projects.value = res.data
  if (selectedProject.value) {
    const latest = projects.value.find((p) => p.id === selectedProject.value?.id)
    if (latest) {
      await selectProject(latest)
    } else {
      selectedProject.value = null
      modules.value = []
      tasks.value = []
    }
  }
}

const selectProject = async (p: Project) => {
  selectedProject.value = p
  editProject.name = p.name
  editProject.description = p.description || ''
  editProject.status = p.status
  await Promise.all([loadModules(), loadTasks()])
}

const createProject = async () => {
  creating.value = true
  try {
    await api.post('/api/projects', { name: newProjectName.value, description: newProjectDesc.value })
    newProjectName.value = ''
    newProjectDesc.value = ''
    await load()
  } finally {
    creating.value = false
  }
}

const saveProject = async () => {
  if (!selectedProject.value) return
  saving.value = true
  try {
    await api.patch(`/api/projects/${selectedProject.value.id}`, {
      name: editProject.name,
      description: editProject.description,
      status: editProject.status,
    })
    await load()
  } finally {
    saving.value = false
  }
}

const deleteProject = async () => {
  if (!selectedProject.value) return
  saving.value = true
  try {
    await api.delete(`/api/projects/${selectedProject.value.id}`)
    selectedProject.value = null
    modules.value = []
    tasks.value = []
    await load()
  } finally {
    saving.value = false
  }
}

const loadModules = async () => {
  if (!selectedProject.value) return
  const res = await api.get(`/api/projects/${selectedProject.value.id}/modules`)
  modules.value = res.data
}

const createModule = async () => {
  if (!selectedProject.value) return
  await api.post(`/api/projects/${selectedProject.value.id}/modules`, { name: newModuleName.value })
  newModuleName.value = ''
  await loadModules()
}

const removeModule = async (id: number) => {
  if (!selectedProject.value) return
  await api.delete(`/api/projects/${selectedProject.value.id}/modules/${id}`)
  await Promise.all([loadModules(), loadTasks()])
}

const loadTasks = async () => {
  if (!selectedProject.value) return
  const res = await api.get(`/api/projects/${selectedProject.value.id}/tasks`)
  tasks.value = res.data
}

const createTask = async () => {
  if (!selectedProject.value) return
  const payload: any = { name: newTaskName.value, status: newTaskStatus.value }
  if (newTaskModuleId.value) payload.moduleId = Number(newTaskModuleId.value)
  await api.post(`/api/projects/${selectedProject.value.id}/tasks`, payload)
  newTaskName.value = ''
  newTaskModuleId.value = ''
  newTaskStatus.value = 'TODO'
  await loadTasks()
}

const removeTask = async (id: number) => {
  if (!selectedProject.value) return
  await api.delete(`/api/projects/${selectedProject.value.id}/tasks/${id}`)
  await loadTasks()
}

const moduleNameById = (id?: number) => {
  if (!id) return ''
  const m = modules.value.find((x) => x.id === id)
  return m?.name || ''
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
}
.title {
  font-weight: 600;
}
.grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
}
@media (max-width: 980px) {
  .grid {
    grid-template-columns: 1fr;
  }
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
.create {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px 10px;
  outline: none;
}
.textarea {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  outline: none;
  resize: vertical;
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
.danger {
  border: 1px solid #fecaca;
  background: #fff1f2;
  color: #b91c1c;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.row-btn {
  text-align: left;
  border: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 12px;
  padding: 10px 12px;
  cursor: pointer;
}
.row-btn.active {
  border-color: #e5e7eb;
  background: #f9fafb;
}
.p-name {
  font-weight: 600;
  font-size: 13px;
}
.p-sub {
  margin-top: 6px;
  font-size: 12px;
}
.muted {
  color: #6b7280;
  font-size: 12px;
}
.detail {
  display: flex;
  flex-direction: column;
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
.row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 12px;
}
.split {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 980px) {
  .split {
    grid-template-columns: 1fr;
  }
}
.box-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.pill-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.pill {
  display: flex;
  gap: 10px;
  align-items: center;
  border: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 13px;
}
.mini {
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 999px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
}
.task-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.task {
  border: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  justify-content: space-between;
}
.t-name {
  font-weight: 600;
  font-size: 13px;
}
.t-sub {
  margin-top: 6px;
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
</style>

