import { z } from 'zod'

const EMPLOYEE_TYPES = ['REGULAR', 'IRREGULAR', 'PART_TIME'] as const

export const EmployeeCreateSchema = z.object({
  userId: z.string().min(1, 'userId를 입력해주세요.').max(100),
  name: z.string().min(1, '이름을 입력해주세요.').max(100),
  employeeType: z.enum(EMPLOYEE_TYPES),
  storeId: z.string().optional(),
})

export const EmployeeUpdateSchema = EmployeeCreateSchema.omit({ userId: true })

export type EmployeeCreateInput = z.infer<typeof EmployeeCreateSchema>
export type EmployeeUpdateInput = z.infer<typeof EmployeeUpdateSchema>
