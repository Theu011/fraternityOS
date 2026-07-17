export type ChoreStatus = 'PENDING' | 'COMPLETED' | 'OVERDUE'

export interface Chore {
  id: number
  title: string
  description: string | null
  assigneeId: number
  assigneeName: string | null
  /** ISO date, e.g. "2026-08-01". */
  dueDate: string
  status: ChoreStatus
  completedAt: string | null
}

export interface ChoreInput {
  title: string
  description: string
  assigneeId: number
  dueDate: string
}
