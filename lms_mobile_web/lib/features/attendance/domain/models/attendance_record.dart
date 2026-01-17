class AttendanceRecord {
  final String id;
  final String employeeId;
  final String workScheduleId;
  final DateTime attendanceDate;
  final DateTime? checkInTime;
  final DateTime? checkOutTime;
  final double? actualWorkHours;
  final String status;
  final String? note;
  final DateTime createdAt;

  AttendanceRecord({
    required this.id,
    required this.employeeId,
    required this.workScheduleId,
    required this.attendanceDate,
    this.checkInTime,
    this.checkOutTime,
    this.actualWorkHours,
    required this.status,
    this.note,
    required this.createdAt,
  });

  factory AttendanceRecord.fromJson(Map<String, dynamic> json) {
    return AttendanceRecord(
      id: json['id'] as String,
      employeeId: json['employeeId'] as String,
      workScheduleId: json['workScheduleId'] as String,
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
      status: json['status'] as String,
      note: json['note'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'employeeId': employeeId,
        'workScheduleId': workScheduleId,
        'attendanceDate': attendanceDate.toIso8601String(),
        'checkInTime': checkInTime?.toIso8601String(),
        'checkOutTime': checkOutTime?.toIso8601String(),
        'actualWorkHours': actualWorkHours,
        'status': status,
        'note': note,
        'createdAt': createdAt.toIso8601String(),
      };

  bool get hasCheckedIn => checkInTime != null;
  bool get hasCheckedOut => checkOutTime != null;
  bool get isComplete => hasCheckedIn && hasCheckedOut;
}
