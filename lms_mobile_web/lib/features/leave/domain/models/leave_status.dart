enum LeaveStatus {
  pending('PENDING', '대기중'),
  approved('APPROVED', '승인'),
  rejected('REJECTED', '반려'),
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
