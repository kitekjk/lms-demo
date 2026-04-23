import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { Payroll } from '@/features/payroll/types'
import type { BatchExecuteInput } from './schema'

interface BatchHistoryResponse {
  id: string
  period: string
  storeId: string | null
  status: 'RUNNING' | 'COMPLETED' | 'PARTIAL_SUCCESS' | 'FAILED'
  totalCount: number
  successCount: number
  failureCount: number
  startedAt: string
  completedAt: string | null
  errorMessage: string | null
  createdAt: string
}

export const adminPayrollKeys = {
  all: ['admin-payroll'] as const,
  period: (period: string) => [...adminPayrollKeys.all, 'period', period] as const,
  batchHistory: () => [...adminPayrollKeys.all, 'batch-history'] as const,
}

export function usePayrollsByPeriod(period: string) {
  return useQuery({
    queryKey: adminPayrollKeys.period(period),
    queryFn: async () => {
      const res = await api.get<Payroll[]>(endpoints.payroll.list, { params: { period } })
      return res.data
    },
    enabled: /^\d{4}-\d{2}$/.test(period),
  })
}

export function useBatchHistory() {
  return useQuery({
    queryKey: adminPayrollKeys.batchHistory(),
    queryFn: async () => {
      const res = await api.get<BatchHistoryResponse[]>(endpoints.payroll.batchHistory)
      return res.data
    },
  })
}

export function useExecuteBatch() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: BatchExecuteInput) => {
      const payload = { period: input.period, ...(input.storeId ? { storeId: input.storeId } : {}) }
      const res = await api.post<BatchHistoryResponse>(endpoints.payroll.batch, payload)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: adminPayrollKeys.all }),
  })
}
