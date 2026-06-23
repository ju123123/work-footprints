export type WorkRecord = {
  id: number
  source: string
  rawContent: string
  recordDate: string
  projectId?: number
  moduleId?: number
  taskId?: number
  projectName?: string
  moduleName?: string
  taskSummary?: string
  resultSummary?: string
  issueSummary?: string
  nextPlan?: string
  tagsJson?: string
  status: string
  createdAt?: string
  updatedAt?: string
}

export type Report = {
  id: number
  type: string
  startDate: string
  endDate: string
  content: string
  createdAt?: string
  updatedAt?: string
}

export type Project = {
  id: number
  name: string
  description?: string
  status: string
  createdAt?: string
  updatedAt?: string
}

export type ProjectModule = {
  id: number
  projectId: number
  name: string
  createdAt?: string
  updatedAt?: string
}

export type ProjectTask = {
  id: number
  projectId: number
  moduleId?: number
  name: string
  status: string
  progressSummary?: string
  riskSummary?: string
  createdAt?: string
  updatedAt?: string
}

export type ProjectCount = {
  projectId: number
  projectName: string
  count: number
}

export type RecordStats = {
  total: number
  draft: number
  pending: number
  confirmed: number
  byProject: ProjectCount[]
}
