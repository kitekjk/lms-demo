class WorkSchedule {
  final String id;
  final String employeeId;
  final String storeId;
  final DateTime workDate;
  final String startTime;
  final String endTime;
  final double workHours;
  final bool isConfirmed;
  final bool isWeekendWork;
  final DateTime createdAt;
  final String? employeeName;
  final String? storeName;

  WorkSchedule({
    required this.id,
    required this.employeeId,
    required this.storeId,
    required this.workDate,
    required this.startTime,
    required this.endTime,
    required this.workHours,
    required this.isConfirmed,
    required this.isWeekendWork,
    required this.createdAt,
    this.employeeName,
    this.storeName,
  });

  factory WorkSchedule.fromJson(Map<String, dynamic> json) {
    return WorkSchedule(
      id: json['id'] as String,
      employeeId: json['employeeId'] as String,
      storeId: json['storeId'] as String,
      workDate: DateTime.parse(json['workDate'] as String),
      startTime: json['startTime'] as String,
      endTime: json['endTime'] as String,
      workHours: (json['workHours'] as num).toDouble(),
      isConfirmed: json['isConfirmed'] as bool? ?? false,
      isWeekendWork: json['isWeekendWork'] as bool? ?? false,
      createdAt: DateTime.parse(json['createdAt'] as String),
      employeeName: json['employeeName'] as String?,
      storeName: json['storeName'] as String?,
    );
  }
}
