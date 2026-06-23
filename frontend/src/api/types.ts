export type WorkRecord = {
  id: number
  source: string
  rawContent: string
  recordDate: string
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

