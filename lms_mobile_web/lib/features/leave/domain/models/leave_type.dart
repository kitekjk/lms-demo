enum LeaveType {
  annual('ANNUAL', '연차'),
  sick('SICK', '병가'),
  personal('PERSONAL', '개인 사유'),
  family('FAMILY', '경조사'),
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
