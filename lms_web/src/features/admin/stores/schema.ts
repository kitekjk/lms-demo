import { z } from 'zod'

export const StoreSchema = z.object({
  name: z.string().min(1, '매장 이름을 입력해주세요.').max(100),
  location: z.string().min(1, '위치를 입력해주세요.').max(200),
})

export type StoreInput = z.infer<typeof StoreSchema>
