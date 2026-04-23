import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { AdminEmployee, AdminEmployeeListResponse } from './types'
import type { EmployeeCreateInput, EmployeeUpdateInput } from './schema'

export const employeeKeys = {
  all: ['employees'] as const,
  list: (filters: { storeId?: string; activeOnly?: boolean }) =>
    [...employeeKeys.all, 'list', filters] as const,
}

export function useEmployees(filters: { storeId?: string; activeOnly?: boolean } = {}) {
  return useQuery({
    queryKey: employeeKeys.list(filters),
    queryFn: async () => {
      const res = await api.get<AdminEmployeeListResponse>(endpoints.employees.list, { params: filters })
      return res.data
    },
  })
}

export function useCreateEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: EmployeeCreateInput) => {
      const res = await api.post<AdminEmployee>(endpoints.employees.create, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}

export function useUpdateEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: EmployeeUpdateInput }) => {
      const res = await api.put<AdminEmployee>(endpoints.employees.update(args.id), args.input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}

export function useDeactivateEmployee() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await api.patch<AdminEmployee>(endpoints.employees.deactivate(id))
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}
