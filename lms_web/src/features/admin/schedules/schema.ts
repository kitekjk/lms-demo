import { z } from 'zod'

export const ScheduleCreateSchema = z.object({
  employeeId: z.string().min(1, '직원을 선택하세요.'),
  storeId: z.string().min(1, '매장을 선택하세요.'),
  workDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '날짜를 선택하세요.'),
  startTime: z.string().regex(/^\d{2}:\d{2}$/, 'HH:mm 형식'),
  endTime: z.string().regex(/^\d{2}:\d{2}$/, 'HH:mm 형식'),
})

export const ScheduleUpdateSchema = z.object({
  workDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
  startTime: z.string().regex(/^\d{2}:\d{2}$/).optional(),
  endTime: z.string().regex(/^\d{2}:\d{2}$/).optional(),
})

export type ScheduleCreateInput = z.infer<typeof ScheduleCreateSchema>
export type ScheduleUpdateInput = z.infer<typeof ScheduleUpdateSchema>

// Convert 'HH:mm' to 'HH:mm:ss' for backend
export function toBackendTime(v: string): string {
  return v.length === 5 ? v + ':00' : v
}
