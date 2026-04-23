import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import type { Store, StoreListResponse } from './types'
import type { StoreInput } from './schema'

export const storeKeys = {
  all: ['stores'] as const,
  list: () => [...storeKeys.all, 'list'] as const,
}

export function useStores() {
  return useQuery({
    queryKey: storeKeys.list(),
    queryFn: async () => {
      const res = await api.get<StoreListResponse>(endpoints.stores.list)
      return res.data
    },
  })
}

export function useCreateStore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: StoreInput) => {
      const res = await api.post<Store>(endpoints.stores.create, input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: storeKeys.all }),
  })
}

export function useUpdateStore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: string; input: StoreInput }) => {
      const res = await api.put<Store>(endpoints.stores.update(args.id), args.input)
      return res.data
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: storeKeys.all }),
  })
}

export function useDeleteStore() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(endpoints.stores.delete(id))
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: storeKeys.all }),
  })
}
