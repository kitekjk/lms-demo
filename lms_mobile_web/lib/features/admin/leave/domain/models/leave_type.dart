/// 휴가 종류
enum LeaveType {
  /// 연차
  annual('ANNUAL', '연차'),

  /// 병가
  sick('SICK', '병가'),

  /// 개인 사유
  personal('PERSONAL', '개인 사유'),

  /// 경조사
  family('FAMILY', '경조사'),

  /// 기타
  other('OTHER', '기타');

  final String value;
  final String displayName;

  const LeaveType(this.value, this.displayName);

  static LeaveType fromString(String value) {
    return LeaveType.values.firstWhere(
      (type) => type.value == value,
      orElse: () => LeaveType.other,
    );
  }

  @override
  String toString() => displayName;
}
