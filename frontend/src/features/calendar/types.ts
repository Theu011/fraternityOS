export interface CalendarEvent {
  id: number
  title: string
  description: string | null
  /** ISO date, e.g. "2026-07-15". */
  date: string
  createdBy: number
  createdByName: string | null
}

export interface EventInput {
  title: string
  date: string
  description: string
}
