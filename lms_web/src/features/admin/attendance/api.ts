import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { AttendanceListResponse, AttendanceRecord } from '@/features/attendance/types'
import type { AttendanceAdjustInput } from './schema'

export const adminAttendanceKeys = {
  all: ['admin-attendance'] as const,
  records: (filters: object) => [...adminAttendanceKeys.all, 'records', filters] as const,
}

export function useStoreAttendance(filters: {
  storeId?: string
  startDate?: string
  endDate?: string
}) {
  return useQuery({
    queryKey: adminAttendanceKeys.records(filters),
    queryFn: async () => {
      const res = await api.get<AttendanceListResponse>(endpoints.attendance.records, {
        params: filters,
      })
      return res.data
    },
    enabled: !!filters.storeId,
  })
}

// Convert "YYYY-MM-DD" + "HH:mm" to ISO. Uses user local tz.
function combineIso(date: string, time: string): string {
  const [y, m, d] = date.split('-').map(Number)
  const [hh, mm] = time.split(':').map(Number)
  return new Date(y, m - 1, d, hh, mm, 0).toISOString()
}

export function useAdjustAttendance() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: {
      recordId: string
      attendanceDate: string
      input: AttendanceAdjustInput
    }) => {
      const payload: Record<string, unknown> = {
        adjustedCheckInTime: combineIso(args.attendanceDate, args.input.adjustedCheckInTime),
        reason: args.input.reason,
      }
      if (args.input.adjustedCheckOutTime) {
        payload.adjustedCheckOutTime = combineIso(
          args.attendanceDate,
          args.input.adjustedCheckOutTime,
        )
      }
      const res = await api.put<AttendanceRecord>(
        endpoints.attendance.adjust(args.recordId),
        payload,
      )
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminAttendanceKeys.all }),
  })
}
