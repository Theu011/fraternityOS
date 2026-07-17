import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { CalendarEvent, EventInput } from './types'

const KEY = ['events']

export function useEvents() {
  return useQuery({
    queryKey: KEY,
    queryFn: async () => {
      const { data } = await api.get<CalendarEvent[]>('/events')
      return data
    },
  })
}

export function useCreateEvent() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: EventInput) => {
      const { data } = await api.post<CalendarEvent>('/events', input)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useUpdateEvent() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, input }: { id: number; input: EventInput }) => {
      const { data } = await api.put<CalendarEvent>(`/events/${id}`, input)
      return data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}

export function useDeleteEvent() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/events/${id}`)
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}
