import 'package:lms_mobile_web/features/admin/attendance/domain/models/attendance_status.dart';

/// 출퇴근 기록 도메인 모델
class AttendanceRecord {
  final String id;
  final String employeeId;
  final String? workScheduleId;
  final DateTime attendanceDate;
  final DateTime? checkInTime;
  final DateTime? checkOutTime;
  final double? actualWorkHours;
  final AttendanceStatus status;
  final String? note;
  final DateTime createdAt;

  // Extended properties from backend joins
  final String? employeeName;
  final String? storeName;

  const AttendanceRecord({
    required this.id,
    required this.employeeId,
    this.workScheduleId,
    required this.attendanceDate,
    this.checkInTime,
    this.checkOutTime,
    this.actualWorkHours,
    required this.status,
    this.note,
    required this.createdAt,
    this.employeeName,
    this.storeName,
  });

  factory AttendanceRecord.fromJson(Map<String, dynamic> json) {
    return AttendanceRecord(
      id: json['id'] as String,
      employeeId: json['employeeId'] as String,
      workScheduleId: json['workScheduleId'] as String?,
      attendanceDate: DateTime.parse(json['attendanceDate'] as String),
      checkInTime: json['checkInTime'] != null
          ? DateTime.parse(json['checkInTime'] as String)
          : null,
      checkOutTime: json['checkOutTime'] != null
          ? DateTime.parse(json['checkOutTime'] as String)
          : null,
      actualWorkHours: json['actualWorkHours'] != null
          ? (json['actualWorkHours'] as num).toDouble()
          : null,
      status: AttendanceStatus.fromString(json['status'] as String),
      note: json['note'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      employeeName: json['employeeName'] as String?,
      storeName: json['storeName'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'employeeId': employeeId,
      'workScheduleId': workScheduleId,
      'attendanceDate': attendanceDate.toIso8601String().split('T')[0],
      'checkInTime': checkInTime?.toIso8601String(),
      'checkOutTime': checkOutTime?.toIso8601String(),
      'actualWorkHours': actualWorkHours,
      'status': status.value,
      'note': note,
      'createdAt': createdAt.toIso8601String(),
      'employeeName': employeeName,
      'storeName': storeName,
    };
  }

  /// 출근만 한 상태인지 확인
  bool get isCheckedInOnly => checkInTime != null && checkOutTime == null;

  /// 퇴근까지 완료한 상태인지 확인
  bool get isCompleted => checkInTime != null && checkOutTime != null;

  /// 지각 여부 (status가 LATE인 경우)
  bool get isLate => status == AttendanceStatus.late;

  /// 조퇴 여부 (status가 EARLY_LEAVE인 경우)
  bool get isEarlyLeave => status == AttendanceStatus.earlyLeave;

  /// 결근 여부 (status가 ABSENT인 경우)
  bool get isAbsent => status == AttendanceStatus.absent;
}
