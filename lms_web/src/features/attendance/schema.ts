import { z } from 'zod'

export const CheckInSchema = z.object({
  workScheduleId: z.string().optional(),
})
export type CheckInInput = z.infer<typeof CheckInSchema>

export const CheckOutSchema = z.object({
  note: z.string().max(500, '메모는 500자 이하로 입력해주세요.').optional(),
})
export type CheckOutInput = z.infer<typeof CheckOutSchema>

export const DateRangeSchema = z.object({
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
})
export type DateRangeInput = z.infer<typeof DateRangeSchema>
