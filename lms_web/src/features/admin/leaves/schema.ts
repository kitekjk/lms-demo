import { z } from 'zod'

export const LeaveRejectionSchema = z.object({
  rejectionReason: z.string().min(1, '반려 사유를 입력해주세요.').max(500, '500자 이하'),
})

export type LeaveRejectionInput = z.infer<typeof LeaveRejectionSchema>
