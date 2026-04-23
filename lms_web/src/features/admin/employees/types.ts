export type EmployeeTypeValue = 'REGULAR' | 'IRREGULAR' | 'PART_TIME'

export interface AdminEmployee {
  id: string
  userId: string
  name: string
  employeeType: EmployeeTypeValue
  storeId: string | null
  remainingLeave: number
  isActive: boolean
  createdAt: string
}

export interface AdminEmployeeListResponse {
  employees: AdminEmployee[]
  totalCount: number
}
