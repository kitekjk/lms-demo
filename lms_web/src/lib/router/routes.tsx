import { Navigate, createBrowserRouter } from 'react-router-dom'
import { ProtectedRoute } from './protected-route'
import EmployeeShell from '@/components/layout/EmployeeShell'
import AdminShell from '@/components/layout/AdminShell'

import LoginPage from '@/features/auth/pages/LoginPage'
import ForbiddenPage from '@/features/common/pages/ForbiddenPage'
import NotFoundPage from '@/features/common/pages/NotFoundPage'

import HomePage from '@/features/home/pages/HomePage'
import AttendancePage from '@/features/attendance/pages/AttendancePage'
import AttendanceHistoryPage from '@/features/attendance/pages/AttendanceHistoryPage'
import SchedulePage from '@/features/schedule/pages/SchedulePage'
import LeaveListPage from '@/features/leave/pages/LeaveListPage'
import LeaveRequestPage from '@/features/leave/pages/LeaveRequestPage'
import PayrollListPage from '@/features/payroll/pages/PayrollListPage'
import PayrollDetailPage from '@/features/payroll/pages/PayrollDetailPage'

import AdminDashboardPage from '@/features/admin/pages/AdminDashboardPage'
import EmployeeManagementPage from '@/features/admin/pages/EmployeeManagementPage'
import StoreManagementPage from '@/features/admin/pages/StoreManagementPage'
import ScheduleManagementPage from '@/features/admin/pages/ScheduleManagementPage'
import AttendanceManagementPage from '@/features/admin/pages/AttendanceManagementPage'
import LeaveManagementPage from '@/features/admin/pages/LeaveManagementPage'
import PayrollManagementPage from '@/features/admin/pages/PayrollManagementPage'
import PayrollPolicyPage from '@/features/admin/pages/PayrollPolicyPage'

import { tokenStorage } from '@/lib/auth/token-storage'
import { useAuthStore } from '@/features/auth/store'

function RootRedirect() {
  const user = useAuthStore((s) => s.currentUser)
  const hasToken = tokenStorage.getAccessToken() !== null
  if (!hasToken || !user) return <Navigate to="/login" replace />
  return <Navigate to={user.role === 'EMPLOYEE' ? '/home' : '/admin'} replace />
}

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/403', element: <ForbiddenPage /> },

  {
    element: (
      <ProtectedRoute>
        <EmployeeShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/home', element: <HomePage /> },
      { path: '/attendance', element: <AttendancePage /> },
      { path: '/attendance/history', element: <AttendanceHistoryPage /> },
      { path: '/schedule', element: <SchedulePage /> },
      { path: '/leave', element: <LeaveListPage /> },
      { path: '/leave/request', element: <LeaveRequestPage /> },
      { path: '/payroll', element: <PayrollListPage /> },
      { path: '/payroll/:id', element: <PayrollDetailPage /> },
    ],
  },

  {
    element: (
      <ProtectedRoute roles={['MANAGER', 'SUPER_ADMIN']}>
        <AdminShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/admin', element: <AdminDashboardPage /> },
      { path: '/admin/employees', element: <EmployeeManagementPage /> },
      {
        path: '/admin/stores',
        element: (
          <ProtectedRoute roles={['SUPER_ADMIN']}>
            <StoreManagementPage />
          </ProtectedRoute>
        ),
      },
      { path: '/admin/schedules', element: <ScheduleManagementPage /> },
      { path: '/admin/attendance', element: <AttendanceManagementPage /> },
      { path: '/admin/leaves', element: <LeaveManagementPage /> },
      { path: '/admin/payroll', element: <PayrollManagementPage /> },
      {
        path: '/admin/payroll/policies',
        element: (
          <ProtectedRoute roles={['SUPER_ADMIN']}>
            <PayrollPolicyPage />
          </ProtectedRoute>
        ),
      },
    ],
  },

  { path: '*', element: <NotFoundPage /> },
])
