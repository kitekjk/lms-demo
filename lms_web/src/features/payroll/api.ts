import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { Payroll, PayrollWithDetails } from './types'

export const payrollKeys = {
  all: ['payroll'] as const,
  mine: () => [...payrollKeys.all, 'mine'] as const,
  detail: (id: string) => [...payrollKeys.all, 'detail', id] as const,
}

export function useMyPayrolls() {
  return useQuery({
    queryKey: payrollKeys.mine(),
    queryFn: async () => {
      const res = await api.get<Payroll[]>(endpoints.payroll.mine)
      return res.data
    },
  })
}

export function usePayrollDetail(id: string | undefined) {
  return useQuery({
    queryKey: payrollKeys.detail(id ?? ''),
    queryFn: async () => {
      const res = await api.get<PayrollWithDetails>(endpoints.payroll.detail(id!))
      return res.data
    },
    enabled: !!id,
  })
}
