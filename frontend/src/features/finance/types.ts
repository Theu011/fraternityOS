export type PaymentStatus = 'PENDING' | 'PAID'

export interface Statement {
  id: number
  month: number
  year: number
  notes: string | null
  uploadedByName: string | null
  createdAt: string
  hasAttachment: boolean
  myStatus: PaymentStatus | null
  myAmount: number | null
  paidCount: number
  totalCount: number
}

export interface Payment {
  memberId: number
  memberName: string | null
  amount: number
  status: PaymentStatus
  paidAt: string | null
}
