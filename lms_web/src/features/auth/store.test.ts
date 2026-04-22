import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore, type Role } from './store'

const sampleUser = { userId: 'u1', email: 'e@x', role: 'EMPLOYEE' as Role, isActive: true }

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.setState({ currentUser: null })
    localStorage.clear()
  })

  it('starts empty', () => {
    expect(useAuthStore.getState().currentUser).toBeNull()
  })

  it('setCurrentUser persists to localStorage', () => {
    useAuthStore.getState().setCurrentUser(sampleUser)
    expect(useAuthStore.getState().currentUser).toEqual(sampleUser)
    expect(localStorage.getItem('lms.auth')).toContain('u1')
  })

  it('logout clears state', () => {
    useAuthStore.getState().setCurrentUser(sampleUser)
    useAuthStore.getState().logout()
    expect(useAuthStore.getState().currentUser).toBeNull()
  })
})
