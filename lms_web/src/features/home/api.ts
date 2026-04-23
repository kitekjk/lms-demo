import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/client'
import { endpoints } from '@/lib/api/endpoints'
import { useAuthStore } from '@/features/auth/store'

interface EmployeeResponse {
  id: string
  userId: string
  name: string
  employeeType: string
  storeId: string | null
  remainingLeave: number
  isActive: boolean
  createdAt: string
}

interface EmployeeListResponse {
  employees: EmployeeResponse[]
}

export const homeKeys = {
  all: ['home'] as const,
  currentEmployee: (userId: string) => [...homeKeys.all, 'current-employee', userId] as const,
}

export function useCurrentEmployee() {
  const userId = useAuthStore((s) => s.currentUser?.userId) ?? ''
  return useQuery({
    queryKey: homeKeys.currentEmployee(userId),
    queryFn: async () => {
      const res = await api.get<EmployeeListResponse | EmployeeResponse[]>(endpoints.employees.list, {
        params: { activeOnly: true },
      })
      // Handle both possible shapes (wrapped list or plain array)
      const list = Array.isArray(res.data) ? res.data : res.data.employees
      const match = list.find((e) => e.userId === userId)
      if (!match) throw new Error('내 직원 정보를 찾을 수 없습니다.')
      return match
    },
    enabled: !!userId,
  })
}
