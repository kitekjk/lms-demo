import { z } from 'zod'

export const BatchExecuteSchema = z.object({
  period: z.string().regex(/^\d{4}-\d{2}$/, 'YYYY-MM 형식'),
  storeId: z.string().optional(),
})

export type BatchExecuteInput = z.infer<typeof BatchExecuteSchema>
