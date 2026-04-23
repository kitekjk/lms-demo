export interface Payroll {
  id: string
  employeeId: string
  period: string              // 'YYYY-MM'
  baseAmount: number
  overtimeAmount: number
  totalAmount: number
  isPaid: boolean
  paidAt: string | null
  calculatedAt: string
  createdAt: string
}

export interface PayrollDetail {
  id: string
  payrollId: string
  workDate: string
  workType: string
  hours: number
  hourlyRate: number
  multiplier: number
  amount: number
}

export interface PayrollWithDetails {
  payroll: Payroll
  details: PayrollDetail[]
}
