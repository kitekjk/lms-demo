/// 출퇴근 상태
enum AttendanceStatus {
  /// 정상 출근
  normal('NORMAL', '정상'),

  /// 지각
  late('LATE', '지각'),

  /// 조퇴
  earlyLeave('EARLY_LEAVE', '조퇴'),

  /// 결근
  absent('ABSENT', '결근'),

  /// 출근 중 (퇴근 전)
  checkedIn('CHECKED_IN', '출근중');

  final String value;
  final String displayName;

  const AttendanceStatus(this.value, this.displayName);

  static AttendanceStatus fromString(String value) {
    return AttendanceStatus.values.firstWhere(
      (status) => status.value == value,
      orElse: () => AttendanceStatus.normal,
    );
  }

  @override
  String toString() => displayName;
}
