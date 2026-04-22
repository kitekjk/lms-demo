import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { AttendanceListResponse, AttendanceRecord } from './types'
import type { CheckInInput, CheckOutInput, DateRangeInput } from './schema'

export const attendanceKeys = {
  all: ['attendance'] as const,
  myRecords: (range: DateRangeInput) =>
    [...attendanceKeys.all, 'my-records', range] as const,
}

export function useMyAttendance(range: DateRangeInput) {
  return useQuery({
    queryKey: attendanceKeys.myRecords(range),
    queryFn: async () => {
      const res = await api.get<AttendanceListResponse>(endpoints.attendance.myRecords, {
        params: range,
      })
      return res.data
    },
  })
}

export function useCheckIn() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: CheckInInput) => {
      const res = await api.post<AttendanceRecord>(endpoints.attendance.checkIn, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: attendanceKeys.all }),
  })
}

export function useCheckOut() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: CheckOutInput) => {
      const res = await api.post<AttendanceRecord>(endpoints.attendance.checkOut, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: attendanceKeys.all }),
  })
}
