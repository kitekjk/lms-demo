class DashboardStats {
  final int totalStores;
  final int totalEmployees;
  final int todayAttendance;
  final int onLeave;
  final AttendanceSummary attendanceSummary;
  final List<RecentActivity> recentActivities;

  DashboardStats({
    required this.totalStores,
    required this.totalEmployees,
    required this.todayAttendance,
    required this.onLeave,
    required this.attendanceSummary,
    required this.recentActivities,
  });

  factory DashboardStats.fromJson(Map<String, dynamic> json) {
    return DashboardStats(
      totalStores: json['totalStores'] as int,
      totalEmployees: json['totalEmployees'] as int,
      todayAttendance: json['todayAttendance'] as int,
      onLeave: json['onLeave'] as int,
      attendanceSummary: AttendanceSummary.fromJson(
        json['attendanceSummary'] as Map<String, dynamic>,
      ),
      recentActivities: (json['recentActivities'] as List<dynamic>)
          .map((e) => RecentActivity.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }

  // Mock data for development
  factory DashboardStats.mock() {
    return DashboardStats(
      totalStores: 3,
      totalEmployees: 6,
      todayAttendance: 5,
      onLeave: 0,
      attendanceSummary: AttendanceSummary.mock(),
      recentActivities: RecentActivity.mockList(),
    );
  }
}

class AttendanceSummary {
  final int normal;
  final int late;
  final int earlyLeave;
  final int absent;

  AttendanceSummary({
    required this.normal,
    required this.late,
    required this.earlyLeave,
    required this.absent,
  });

  factory AttendanceSummary.fromJson(Map<String, dynamic> json) {
    return AttendanceSummary(
      normal: json['normal'] as int,
      late: json['late'] as int,
      earlyLeave: json['earlyLeave'] as int,
      absent: json['absent'] as int,
    );
  }

  factory AttendanceSummary.mock() {
    return AttendanceSummary(normal: 4, late: 1, earlyLeave: 0, absent: 0);
  }

  int get total => normal + late + earlyLeave + absent;
}

class RecentActivity {
  final String type;
  final String employeeName;
  final String action;
  final DateTime timestamp;

  RecentActivity({
    required this.type,
    required this.employeeName,
    required this.action,
    required this.timestamp,
  });

  factory RecentActivity.fromJson(Map<String, dynamic> json) {
    return RecentActivity(
      type: json['type'] as String,
      employeeName: json['employeeName'] as String,
      action: json['action'] as String,
      timestamp: DateTime.parse(json['timestamp'] as String),
    );
  }

  static List<RecentActivity> mockList() {
    final now = DateTime.now();
    return [
      RecentActivity(
        type: 'attendance',
        employeeName: '김민수',
        action: '출근 체크',
        timestamp: now.subtract(const Duration(hours: 2)),
      ),
      RecentActivity(
        type: 'attendance',
        employeeName: '이지영',
        action: '출근 체크',
        timestamp: now.subtract(const Duration(hours: 3)),
      ),
      RecentActivity(
        type: 'leave_request',
        employeeName: '정서연',
        action: '휴가 신청',
        timestamp: now.subtract(const Duration(days: 1)),
      ),
    ];
  }
}
