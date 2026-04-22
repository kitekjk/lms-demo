import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { ScheduleListResponse, WorkSchedule } from './types'

export const scheduleKeys = {
  all: ['schedule'] as const,
  mine: (range: { startDate: string; endDate: string }) =>
    [...scheduleKeys.all, 'mine', range] as const,
  detail: (id: string) => [...scheduleKeys.all, 'detail', id] as const,
}

export function useMySchedule(range: { startDate: string; endDate: string }) {
  return useQuery({
    queryKey: scheduleKeys.mine(range),
    queryFn: async () => {
      const res = await api.get<ScheduleListResponse>(endpoints.schedules.mine, { params: range })
      return res.data
    },
  })
}

export function useScheduleById(id: string | undefined) {
  return useQuery({
    queryKey: scheduleKeys.detail(id ?? ''),
    queryFn: async () => {
      const res = await api.get<WorkSchedule>(endpoints.schedules.detail(id!))
      return res.data
    },
    enabled: !!id,
  })
}
