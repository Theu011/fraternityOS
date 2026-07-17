import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { Payment, Statement } from './types'

const KEY = ['statements']

export function useStatements() {
  return useQuery({
    queryKey: KEY,
    queryFn: async () => {
      const { data } = await api.get<Statement[]>('/statements')
      return data
    },
  })
}

export function useUploadStatement() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (form: FormData) => {
      const { data } = await api.post<Statement>('/statements', form)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function usePayStatement() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      await api.post(`/statements/${id}/pay`)
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteStatement() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/statements/${id}`)
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useStatementPayments(id: number, enabled: boolean) {
  return useQuery({
    queryKey: ['statements', id, 'payments'],
    enabled,
    queryFn: async () => {
      const { data } = await api.get<Payment[]>(`/statements/${id}/payments`)
      return data
    },
  })
}

/** Fetches the attachment (with auth) and opens it in a new tab. */
export async function openAttachment(id: number) {
  const { data } = await api.get(`/statements/${id}/attachment`, { responseType: 'blob' })
  const url = URL.createObjectURL(data as Blob)
  window.open(url, '_blank', 'noopener,noreferrer')
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}
