import { z } from 'zod'

const POLICY_TYPES = ['OVERTIME_WEEKDAY','OVERTIME_WEEKEND','OVERTIME_HOLIDAY','NIGHT_SHIFT','HOLIDAY_WORK','BONUS','ALLOWANCE'] as const

export const PolicyCreateSchema = z.object({
  policyType: z.enum(POLICY_TYPES),
  multiplier: z.number().min(0).max(10),
  effectiveFrom: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, '시작일을 선택하세요.'),
  effectiveTo: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional().or(z.literal('')),
  description: z.string().max(500).optional(),
})

export const PolicyUpdateSchema = z.object({
  multiplier: z.number().min(0).max(10).optional(),
  effectiveTo: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional().or(z.literal('')),
  description: z.string().max(500).optional(),
})

export type PolicyCreateInput = z.infer<typeof PolicyCreateSchema>
export type PolicyUpdateInput = z.infer<typeof PolicyUpdateSchema>
