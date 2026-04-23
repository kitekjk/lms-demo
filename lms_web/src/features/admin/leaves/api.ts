import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { LeaveListResponse, LeaveRequest } from '@/features/leave/types'

export const adminLeaveKeys = {
  all: ['admin-leaves'] as const,
  pending: () => [...adminLeaveKeys.all, 'pending'] as const,
}

export function usePendingLeaves() {
  return useQuery({
    queryKey: adminLeaveKeys.pending(),
    queryFn: async () => {
      const res = await api.get<LeaveListResponse>(endpoints.leaves.pending)
      return res.data
    },
  })
}

export function useApproveLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await api.patch<LeaveRequest>(endpoints.leaves.approve(id))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminLeaveKeys.all }),
  })
}

export function useRejectLeave() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; rejectionReason: string }) => {
      const res = await api.patch<LeaveRequest>(endpoints.leaves.reject(args.id), {
        rejectionReason: args.rejectionReason,
      })
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminLeaveKeys.all }),
  })
}
