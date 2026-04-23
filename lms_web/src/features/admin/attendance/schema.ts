import { z } from 'zod'

// Backend accepts ISO-datetime (Instant). We combine attendanceDate + time (HH:mm) on submit.
export const AttendanceAdjustSchema = z.object({
  adjustedCheckInTime: z.string().regex(/^\d{2}:\d{2}$/, '출근 시간을 입력하세요.'),
  adjustedCheckOutTime: z
    .string()
    .regex(/^\d{2}:\d{2}$/)
    .optional()
    .or(z.literal('')),
  reason: z.string().min(1, '조정 사유를 입력해주세요.').max(500),
})

export type AttendanceAdjustInput = z.infer<typeof AttendanceAdjustSchema>
