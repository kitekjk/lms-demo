import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import { tokenStorage } from '@/lib/auth/token-storage'

export type Role = 'EMPLOYEE' | 'MANAGER' | 'SUPER_ADMIN'

export interface AuthUser {
  userId: string
  email: string
  role: Role
  isActive: boolean
}

interface AuthState {
  currentUser: AuthUser | null
  setCurrentUser: (user: AuthUser | null) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      currentUser: null,
      setCurrentUser: (user) => set({ currentUser: user }),
      logout: () => {
        tokenStorage.clear()
        set({ currentUser: null })
      },
    }),
    {
      name: 'lms.auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ currentUser: state.currentUser }),
    },
  ),
)
