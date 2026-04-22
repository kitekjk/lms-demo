import { z } from 'zod'

const LEAVE_TYPES = ['ANNUAL', 'SICK', 'PERSONAL', 'MATERNITY', 'PATERNITY', 'BEREAVEMENT', 'UNPAID'] as const

export const LeaveRequestSchema = z.object({
  leaveType: z.enum(LEAVE_TYPES),
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '시작일을 선택해주세요.'),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '종료일을 선택해주세요.'),
  reason: z.string().max(500, '사유는 500자 이하').optional(),
}).refine((v) => v.endDate >= v.startDate, {
  message: '종료일은 시작일 이후여야 합니다.',
  path: ['endDate'],
})

export type LeaveRequestInput = z.infer<typeof LeaveRequestSchema>
