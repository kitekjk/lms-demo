# Admin Login Test Guide

## Test Accounts

The following test accounts are available for testing the admin web interface:

### Super Admin Account
- **Email**: `admin@lms.com`
- **Password**: `password123`
- **Role**: SUPER_ADMIN
- **Description**: Full system administrator access

### Manager Accounts

#### 강남점 Manager
- **Email**: `manager.gangnam@lms.com`
- **Password**: `password123`
- **Role**: MANAGER
- **Store**: 강남점 (store-001)

#### 홍대점 Manager
- **Email**: `manager.hongdae@lms.com`
- **Password**: `password123`
- **Role**: MANAGER
- **Store**: 홍대점 (store-002)

## How to Test

### 1. Start the Backend Server

```bash
cd c:\Users\kitek\IdeaProjects\lms-demo
./gradlew :interfaces:bootRun
```

The backend will start on `http://localhost:8080`

### 2. Start the Flutter Web App

```bash
cd c:\Users\kitek\IdeaProjects\lms-demo\lms_mobile_web
flutter run -d chrome --web-port=8081
```

The web app will open in Chrome.

### 3. Navigate to Admin Login

- From the employee login screen, click the "관리자로 로그인" button at the bottom
- Or directly navigate to `http://localhost:8081/#/admin/login`

### 4. Login with Test Account

Use any of the test accounts listed above to login.

### 5. Verify Admin Dashboard

After successful login, you should:
- See the admin dashboard with welcome message
- See user information in the top-right corner (email and role)
- See 6 management menu cards (Store, Employee, Schedule, Attendance, Leave, Payroll)
- Be able to logout using the logout button

## Features Implemented in Task 17.1

✅ Admin authentication system
- Admin login screen with email/password validation
- Role-based access control (SUPER_ADMIN, MANAGER)
- Secure token storage using flutter_secure_storage
- Admin-specific authentication provider with Riverpod
- Login state management (loading, error, authenticated)

✅ Admin dashboard placeholder
- Welcome card with user information
- Menu grid with 6 management categories
- Logout functionality
- Responsive layout

✅ Routing
- `/admin/login` - Admin login page
- `/admin/dashboard` - Admin dashboard
- Link from employee login to admin login

## Next Steps (Tasks 17.2-17.5)

The following features are placeholders and will be implemented in subsequent tasks:
- [ ] Task 17.2: Web admin layout and navigation system
- [ ] Task 17.3: Dashboard with statistics
- [ ] Task 17.4: Store and employee management (CRUD)
- [ ] Task 17.5: Schedule, attendance, leave, payroll management
