export type PolicyTypeValue =
  | 'OVERTIME_WEEKDAY' | 'OVERTIME_WEEKEND' | 'OVERTIME_HOLIDAY'
  | 'NIGHT_SHIFT' | 'HOLIDAY_WORK' | 'BONUS' | 'ALLOWANCE'

export interface PayrollPolicy {
  id: string
  policyType: PolicyTypeValue
  policyTypeDescription: string
  multiplier: number
  effectiveFrom: string
  effectiveTo: string | null
  description: string | null
  isCurrentlyEffective: boolean
  createdAt: string
}

export interface PayrollPolicyListResponse {
  policies: PayrollPolicy[]
  totalCount: number
}
