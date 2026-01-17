import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/auth/presentation/screens/admin_login_screen.dart';
import 'package:lms_mobile_web/features/admin/dashboard/presentation/screens/admin_dashboard_screen.dart';
import 'package:lms_mobile_web/features/admin/employee/presentation/screens/employee_form_screen.dart';
import 'package:lms_mobile_web/features/admin/employee/presentation/screens/employee_list_screen.dart';
import 'package:lms_mobile_web/features/admin/attendance/presentation/screens/attendance_management_screen.dart';
import 'package:lms_mobile_web/features/admin/leave/presentation/screens/leave_management_screen.dart';
import 'package:lms_mobile_web/features/admin/payroll/presentation/screens/payroll_management_screen.dart';
import 'package:lms_mobile_web/features/admin/schedule/presentation/screens/schedule_calendar_screen.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/screens/store_form_screen.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/screens/store_list_screen.dart';
import 'package:lms_mobile_web/features/attendance/presentation/screens/attendance_records_screen.dart';
import 'package:lms_mobile_web/features/attendance/presentation/screens/check_in_out_screen.dart';
import 'package:lms_mobile_web/features/auth/presentation/screens/login_screen.dart';
import 'package:lms_mobile_web/features/home/presentation/screens/home_screen.dart';

final appRouter = GoRouter(
  initialLocation: RouteNames.login,
  routes: [
    GoRoute(
      path: RouteNames.login,
      name: 'login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: RouteNames.home,
      name: 'home',
      builder: (context, state) => const HomeScreen(),
    ),
    GoRoute(
      path: RouteNames.attendance,
      name: 'attendance',
      builder: (context, state) => const CheckInOutScreen(),
    ),
    GoRoute(
      path: RouteNames.attendanceRecords,
      name: 'attendanceRecords',
      builder: (context, state) => const AttendanceRecordsScreen(),
    ),
    // Admin routes
    GoRoute(
      path: RouteNames.adminLogin,
      name: 'adminLogin',
      builder: (context, state) => const AdminLoginScreen(),
    ),
    GoRoute(
      path: RouteNames.adminDashboard,
      name: 'adminDashboard',
      builder: (context, state) => const AdminDashboardScreen(),
    ),
    // Admin - Stores
    GoRoute(
      path: RouteNames.adminStores,
      name: 'adminStores',
      builder: (context, state) => const StoreListScreen(),
      routes: [
        GoRoute(
          path: 'new',
          name: 'adminStoreCreate',
          builder: (context, state) => const StoreFormScreen(),
        ),
        GoRoute(
          path: ':storeId/edit',
          name: 'adminStoreEdit',
          builder: (context, state) {
            final storeId = state.pathParameters['storeId']!;
            return StoreFormScreen(storeId: storeId);
          },
        ),
      ],
    ),
    // Admin - Employees
    GoRoute(
      path: RouteNames.adminEmployees,
      name: 'adminEmployees',
      builder: (context, state) => const EmployeeListScreen(),
      routes: [
        GoRoute(
          path: 'new',
          name: 'adminEmployeeCreate',
          builder: (context, state) => const EmployeeFormScreen(),
        ),
        GoRoute(
          path: ':employeeId/edit',
          name: 'adminEmployeeEdit',
          builder: (context, state) {
            final employeeId = state.pathParameters['employeeId']!;
            return EmployeeFormScreen(employeeId: employeeId);
          },
        ),
      ],
    ),
    // Admin - Schedules
    GoRoute(
      path: RouteNames.adminSchedules,
      name: 'adminSchedules',
      builder: (context, state) => const ScheduleCalendarScreen(),
    ),
    // Admin - Attendance
    GoRoute(
      path: RouteNames.adminAttendance,
      name: 'adminAttendance',
      builder: (context, state) => const AttendanceManagementScreen(),
    ),
    // Admin - Leaves
    GoRoute(
      path: RouteNames.adminLeaves,
      name: 'adminLeaves',
      builder: (context, state) => const LeaveManagementScreen(),
    ),
    // Admin - Payroll
    GoRoute(
      path: RouteNames.adminPayroll,
      name: 'adminPayroll',
      builder: (context, state) => const PayrollManagementScreen(),
    ),
  ],
  errorBuilder: (context, state) =>
      Scaffold(body: Center(child: Text('페이지를 찾을 수 없습니다: ${state.uri}'))),
);
