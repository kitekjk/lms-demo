import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { PayrollPolicy, PayrollPolicyListResponse } from './types'
import type { PolicyCreateInput, PolicyUpdateInput } from './schema'

export const policyKeys = {
  all: ['policies'] as const,
  list: () => [...policyKeys.all, 'list'] as const,
}

function normalize(input: PolicyCreateInput | PolicyUpdateInput) {
  const payload: Record<string, unknown> = { ...input }
  if (payload.effectiveTo === '') delete payload.effectiveTo
  if (payload.description === '') delete payload.description
  return payload
}

export function usePolicies() {
  return useQuery({
    queryKey: policyKeys.list(),
    queryFn: async () => {
      const res = await api.get<PayrollPolicyListResponse>(endpoints.policies.list)
      return res.data
    },
  })
}

export function useCreatePolicy() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: PolicyCreateInput) => {
      const res = await api.post<PayrollPolicy>(endpoints.policies.create, normalize(input))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: policyKeys.all }),
  })
}

export function useUpdatePolicy() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: PolicyUpdateInput }) => {
      const res = await api.put<PayrollPolicy>(endpoints.policies.update(args.id), normalize(args.input))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: policyKeys.all }),
  })
}

export function useDeletePolicy() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => { await api.delete(endpoints.policies.delete(id)) },
    onSuccess: () => qc.invalidateQueries({ queryKey: policyKeys.all }),
  })
}
