import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { LeaveListResponse, LeaveRequest } from './types'
import type { LeaveRequestInput } from './schema'

export const leaveKeys = {
  all: ['leave'] as const,
  mine: () => [...leaveKeys.all, 'mine'] as const,
}

export function useMyLeaves() {
  return useQuery({
    queryKey: leaveKeys.mine(),
    queryFn: async () => {
      const res = await api.get<LeaveListResponse>(endpoints.leaves.mine)
      return res.data
    },
  })
}

export function useCreateLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: LeaveRequestInput) => {
      const res = await api.post<LeaveRequest>(endpoints.leaves.create, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveKeys.all }),
  })
}

export function useCancelLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(endpoints.leaves.cancel(id))
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: leaveKeys.all }),
  })
}
