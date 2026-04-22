import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useAuthStore } from '@/features/auth/store'
import { tokenStorage } from '@/lib/auth/token-storage'
import { ProtectedRoute } from './protected-route'

function Guarded({ roles }: { roles?: Array<'EMPLOYEE' | 'MANAGER' | 'SUPER_ADMIN'> }) {
  return (
    <MemoryRouter initialEntries={['/secret']}>
      <Routes>
        <Route path="/login" element={<div>LOGIN_PAGE</div>} />
        <Route path="/403" element={<div>FORBIDDEN_PAGE</div>} />
        <Route path="/secret" element={<ProtectedRoute roles={roles}><div>SECRET</div></ProtectedRoute>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear()
    useAuthStore.setState({ currentUser: null })
  })

  it('redirects unauthenticated users to /login', () => {
    render(<Guarded />)
    expect(screen.getByText('LOGIN_PAGE')).toBeInTheDocument()
  })

  it('renders children when authenticated without role check', () => {
    tokenStorage.setTokens('a', 'r')
    useAuthStore.setState({ currentUser: { userId: 'u', email: 'e', role: 'EMPLOYEE', isActive: true } })
    render(<Guarded />)
    expect(screen.getByText('SECRET')).toBeInTheDocument()
  })

  it('redirects to /403 when role insufficient', () => {
    tokenStorage.setTokens('a', 'r')
    useAuthStore.setState({ currentUser: { userId: 'u', email: 'e', role: 'EMPLOYEE', isActive: true } })
    render(<Guarded roles={['MANAGER', 'SUPER_ADMIN']} />)
    expect(screen.getByText('FORBIDDEN_PAGE')).toBeInTheDocument()
  })
})
