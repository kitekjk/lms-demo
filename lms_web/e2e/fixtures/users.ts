// Seeded accounts from interfaces/src/main/resources/data.sql.
// All users share password 'password123' (seed BCrypt hash).

export const USERS = {
  superAdmin: {
    email: 'admin@lms.com',
    password: 'password123',
    role: 'SUPER_ADMIN' as const,
    name: '관리자',
  },
  managerGangnam: {
    email: 'manager.gangnam@lms.com',
    password: 'password123',
    role: 'MANAGER' as const,
    name: '박수진',
    storeId: 'store-001',
  },
  employeeGangnam: {
    email: 'employee1.gangnam@lms.com',
    password: 'password123',
    role: 'EMPLOYEE' as const,
    name: '김민수',
    employeeId: 'emp-001',
    storeId: 'store-001',
  },
}
