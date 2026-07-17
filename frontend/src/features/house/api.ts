import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api'
import type { AuthResponse } from '@/features/auth/types'

export interface House {
  id: number
  name: string
  createdAt: string
}

/** Directory entry shown on the onboarding page. */
export interface HouseSummary {
  id: number
  name: string
  activeMemberCount: number
  createdAt: string
}

export type JoinRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface JoinRequest {
  id: number
  houseId: number
  status: JoinRequestStatus
  createdAt: string
}

/** A request the current user submitted, with the target house name. */
export interface MyJoinRequest {
  id: number
  houseId: number
  houseName: string
  status: JoinRequestStatus
  createdAt: string
}

/** A pending request to the President's house, awaiting a decision. */
export interface PendingJoinRequest {
  id: number
  userId: number
  name: string
  email: string
  createdAt: string
}

export function useHouse() {
  return useQuery({
    queryKey: ['house', 'current'],
    queryFn: async () => {
      const { data } = await api.get<House>('/houses/current')
      return data
    },
    staleTime: 5 * 60 * 1000,
  })
}

/** Lists houses for onboarding, filtered by an optional name fragment. */
export function useHouses(name: string) {
  const query = name.trim()
  return useQuery({
    queryKey: ['houses', query],
    queryFn: async () => {
      const { data } = await api.get<HouseSummary[]>('/houses', {
        params: query ? { name: query } : undefined,
      })
      return data
    },
  })
}

export function useCreateHouse() {
  return useMutation({
    mutationFn: async (name: string) => {
      const { data } = await api.post<AuthResponse>('/houses', { name })
      return data
    },
  })
}

export function useRequestJoin() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (houseId: number) => {
      const { data } = await api.post<JoinRequest>(`/houses/${houseId}/join-request`)
      return data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['houses'] })
      qc.invalidateQueries({ queryKey: ['join-requests', 'mine'] })
    },
  })
}

/** The current user's own join requests (any status). */
export function useMyJoinRequests() {
  return useQuery({
    queryKey: ['join-requests', 'mine'],
    queryFn: async () => {
      const { data } = await api.get<MyJoinRequest[]>('/houses/join-requests/mine')
      return data
    },
  })
}

/** Pending requests to the President's house (President only). */
export function usePendingJoinRequests(enabled: boolean) {
  return useQuery({
    queryKey: ['join-requests', 'pending'],
    enabled,
    queryFn: async () => {
      const { data } = await api.get<PendingJoinRequest[]>('/houses/join-requests')
      return data
    },
  })
}

export function useDecideJoinRequest() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, decision }: { id: number; decision: 'approve' | 'reject' }) => {
      await api.post(`/houses/join-requests/${id}/${decision}`)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['join-requests', 'pending'] })
      qc.invalidateQueries({ queryKey: ['members'] })
    },
  })
}
