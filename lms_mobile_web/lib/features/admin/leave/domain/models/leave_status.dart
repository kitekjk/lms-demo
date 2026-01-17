/// 휴가 승인 상태
enum LeaveStatus {
  /// 대기 중
  pending('PENDING', '대기중'),

  /// 승인됨
  approved('APPROVED', '승인'),

  /// 반려됨
  rejected('REJECTED', '반려'),

  /// 취소됨
  cancelled('CANCELLED', '취소');

  final String value;
  final String displayName;

  const LeaveStatus(this.value, this.displayName);

  static LeaveStatus fromString(String value) {
    return LeaveStatus.values.firstWhere(
      (status) => status.value == value,
      orElse: () => LeaveStatus.pending,
    );
  }

  @override
  String toString() => displayName;
}
