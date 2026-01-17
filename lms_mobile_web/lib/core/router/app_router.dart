import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
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
  ],
  errorBuilder: (context, state) => Scaffold(
    body: Center(
      child: Text('페이지를 찾을 수 없습니다: ${state.uri}'),
    ),
  ),
);
