export const endpoints = {
  auth: {
    login: '/auth/login',
    refresh: '/auth/refresh',
    logout: '/auth/logout',
  },
  attendance: {
    checkIn: '/attendance/check-in',
    checkOut: '/attendance/check-out',
    myRecords: '/attendance/my-records',
  },
  schedules: {
    mine: '/schedules/my-schedule',
    detail: (id: string) => `/schedules/${id}`,
  },
  leaves: {
    create: '/leaves',
    mine: '/leaves/my-leaves',
    cancel: (id: string) => `/leaves/${id}`,
  },
  payroll: {
    mine: '/payroll/my-payroll',
    detail: (id: string) => `/payroll/${id}`,
  },
  employees: {
    list: '/employees',
    detail: (id: string) => `/employees/${id}`,
  },
} as const
