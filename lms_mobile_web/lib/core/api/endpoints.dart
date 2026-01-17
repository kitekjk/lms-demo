class ApiEndpoints {
  // Auth
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String refresh = '/auth/refresh';
  static const String logout = '/auth/logout';
  static const String me = '/auth/me';

  // Attendance
  static const String checkIn = '/attendance/check-in';
  static const String checkOut = '/attendance/check-out';
  static const String myAttendance = '/attendance/my-records';

  // Schedule
  static const String mySchedule = '/work-schedules/my-schedules';

  // Leave
  static const String leaveRequests = '/leave-requests';
  static const String myLeaveRequests = '/leave-requests/my-requests';

  // Payroll
  static const String myPayroll = '/payroll/my-payroll';

  // Admin - Stores
  static const String stores = '/stores';
  static String storeById(String id) => '/stores/$id';

  // Admin - Employees
  static const String employees = '/employees';
  static String employeeById(String id) => '/employees/$id';
  static String employeeDeactivate(String id) => '/employees/$id/deactivate';

  // Admin - Schedules
  static const String schedules = '/schedules';
  static String scheduleById(String id) => '/schedules/$id';

  // Admin - Attendance
  static const String attendanceRecords = '/attendance/records';
  static String attendanceRecordById(String id) => '/attendance/records/$id';

  // Admin - Leaves
  static const String leaves = '/leaves';
  static const String pendingLeaves = '/leaves/pending';
  static String approveLeave(String id) => '/leaves/$id/approve';
  static String rejectLeave(String id) => '/leaves/$id/reject';

  // Admin - Payroll
  static const String payrolls = '/payroll';
  static String payrollById(String id) => '/payroll/$id';
  static const String calculatePayroll = '/payroll/calculate';
  static const String payrollBatch = '/payroll/batch';
  static const String payrollBatchHistory = '/payroll/batch-history';
}
