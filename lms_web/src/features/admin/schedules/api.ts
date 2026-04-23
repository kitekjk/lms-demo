import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { WorkSchedule, ScheduleListResponse } from '@/features/schedule/types'
import { toBackendTime, type ScheduleCreateInput, type ScheduleUpdateInput } from './schema'

export const adminScheduleKeys = {
  all: ['admin-schedules'] as const,
  list: (filters: object) => [...adminScheduleKeys.all, 'list', filters] as const,
}

export function useAdminSchedules(filters: {
  storeId?: string
  employeeId?: string
  startDate?: string
  endDate?: string
}) {
  return useQuery({
    queryKey: adminScheduleKeys.list(filters),
    queryFn: async () => {
      const res = await api.get<ScheduleListResponse>(endpoints.schedules.list, { params: filters })
      return res.data
    },
    enabled: !!(filters.storeId || filters.employeeId),
  })
}

export function useCreateSchedule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: ScheduleCreateInput) => {
      const payload = {
        ...input,
        startTime: toBackendTime(input.startTime),
        endTime: toBackendTime(input.endTime),
      }
      const res = await api.post<WorkSchedule>(endpoints.schedules.list, payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminScheduleKeys.all }),
  })
}

export function useUpdateSchedule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: ScheduleUpdateInput }) => {
      const payload: Record<string, unknown> = { ...args.input }
      if (payload.startTime) payload.startTime = toBackendTime(payload.startTime as string)
      if (payload.endTime) payload.endTime = toBackendTime(payload.endTime as string)
      const res = await api.put<WorkSchedule>(endpoints.schedules.detail(args.id), payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminScheduleKeys.all }),
  })
}

export function useDeleteSchedule() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(endpoints.schedules.detail(id))
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminScheduleKeys.all }),
  })
}
