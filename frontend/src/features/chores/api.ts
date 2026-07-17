import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { Chore, ChoreInput } from './types'

const KEY = ['chores']

export function useChores() {
  return useQuery({
    queryKey: KEY,
    queryFn: async () => {
      const { data } = await api.get<Chore[]>('/chores')
      return data
    },
  })
}

export function useCreateChore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: ChoreInput) => {
      const { data } = await api.post<Chore>('/chores', input)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useCompleteChore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await api.post<Chore>(`/chores/${id}/complete`)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteChore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/chores/${id}`)
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}
