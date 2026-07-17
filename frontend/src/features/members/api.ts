import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'

export type MemberStatus = 'ACTIVE' | 'ALUMNI'

export interface Member {
  id: number
  userId: number
  name: string
  email: string
  phone: string | null
  room: string | null
  positions: string[]
  status: MemberStatus
  joinedAt: string
}

/** Catalog position, loaded from the backend (never hardcoded). */
export interface Position {
  id: number
  name: string
  description: string | null
}

export function useMembers() {
  return useQuery({
    queryKey: ['members'],
    queryFn: async () => {
      const { data } = await api.get<Member[]>('/members')
      return data
    },
  })
}

export function useAlumni() {
  return useQuery({
    queryKey: ['members', 'alumni'],
    queryFn: async () => {
      const { data } = await api.get<Member[]>('/members/alumni')
      return data
    },
  })
}

/** The global position catalog (President, Treasurer, …). */
export function usePositions() {
  return useQuery({
    queryKey: ['positions'],
    staleTime: 10 * 60 * 1000,
    queryFn: async () => {
      const { data } = await api.get<Position[]>('/positions')
      return data
    },
  })
}

function useMemberMutation<TVars>(fn: (vars: TVars) => Promise<unknown>) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['members'] }),
  })
}

export function useChangeStatus() {
  return useMemberMutation(({ id, status }: { id: number; status: MemberStatus }) =>
    api.patch(`/members/${id}/status`, { status }),
  )
}

export function useAssignPositions() {
  return useMemberMutation(({ id, positionIds }: { id: number; positionIds: number[] }) =>
    api.post(`/members/${id}/positions`, { positionIds }),
  )
}

export function useRemovePosition() {
  return useMemberMutation(({ id, positionId }: { id: number; positionId: number }) =>
    api.delete(`/members/${id}/positions/${positionId}`),
  )
}
