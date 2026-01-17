import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_status.dart';
import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_type.dart';

/// 휴가 신청 도메인 모델
class LeaveRequest {
  final String id;
  final String employeeId;
  final LeaveType leaveType;
  final DateTime startDate;
  final DateTime endDate;
  final int totalDays;
  final String reason;
  final LeaveStatus status;
  final String? rejectionReason;
  final String? approvedBy;
  final DateTime? approvedAt;
  final DateTime createdAt;

  // Extended properties from backend joins
  final String? employeeName;
  final String? storeName;
  final String? approverName;

  const LeaveRequest({
    required this.id,
    required this.employeeId,
    required this.leaveType,
    required this.startDate,
    required this.endDate,
    required this.totalDays,
    required this.reason,
    required this.status,
    this.rejectionReason,
    this.approvedBy,
    this.approvedAt,
    required this.createdAt,
    this.employeeName,
    this.storeName,
    this.approverName,
  });

  factory LeaveRequest.fromJson(Map<String, dynamic> json) {
    return LeaveRequest(
      id: json['id'] as String,
      employeeId: json['employeeId'] as String,
      leaveType: LeaveType.fromString(json['leaveType'] as String),
      startDate: DateTime.parse(json['startDate'] as String),
      endDate: DateTime.parse(json['endDate'] as String),
      totalDays: json['totalDays'] as int,
      reason: json['reason'] as String,
      status: LeaveStatus.fromString(json['status'] as String),
      rejectionReason: json['rejectionReason'] as String?,
      approvedBy: json['approvedBy'] as String?,
      approvedAt: json['approvedAt'] != null
          ? DateTime.parse(json['approvedAt'] as String)
          : null,
      createdAt: DateTime.parse(json['createdAt'] as String),
      employeeName: json['employeeName'] as String?,
      storeName: json['storeName'] as String?,
      approverName: json['approverName'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'employeeId': employeeId,
      'leaveType': leaveType.value,
      'startDate': startDate.toIso8601String().split('T')[0],
      'endDate': endDate.toIso8601String().split('T')[0],
      'totalDays': totalDays,
      'reason': reason,
      'status': status.value,
      'rejectionReason': rejectionReason,
      'approvedBy': approvedBy,
      'approvedAt': approvedAt?.toIso8601String(),
      'createdAt': createdAt.toIso8601String(),
      'employeeName': employeeName,
      'storeName': storeName,
      'approverName': approverName,
    };
  }

  /// 승인 대기 중인지 확인
  bool get isPending => status == LeaveStatus.pending;

  /// 승인되었는지 확인
  bool get isApproved => status == LeaveStatus.approved;

  /// 반려되었는지 확인
  bool get isRejected => status == LeaveStatus.rejected;

  /// 취소되었는지 확인
  bool get isCancelled => status == LeaveStatus.cancelled;

  /// 휴가 기간 문자열 (yyyy-MM-dd ~ yyyy-MM-dd)
  String get periodString {
    final start = startDate.toIso8601String().split('T')[0];
    final end = endDate.toIso8601String().split('T')[0];
    return '$start ~ $end';
  }
}
