class Payroll {
  final String id;
  final String employeeId;
  final String period;
  final double baseSalary;
  final double overtimePay;
  final double nightWorkPay;
  final double holidayWorkPay;
  final double totalPay;
  final double taxAmount;
  final double insuranceAmount;
  final double netPay;
  final DateTime calculatedAt;
  final bool isConfirmed;
  final DateTime? confirmedAt;
  final String? employeeName;
  final String? storeName;
  final int? workDays;
  final double? workHours;

  const Payroll({
    required this.id,
    required this.employeeId,
    required this.period,
    required this.baseSalary,
    required this.overtimePay,
    required this.nightWorkPay,
    required this.holidayWorkPay,
    required this.totalPay,
    required this.taxAmount,
    required this.insuranceAmount,
    required this.netPay,
    required this.calculatedAt,
    required this.isConfirmed,
    this.confirmedAt,
    this.employeeName,
    this.storeName,
    this.workDays,
    this.workHours,
  });

  factory Payroll.fromJson(Map<String, dynamic> json) {
    return Payroll(
      id: json['id'] as String,
      employeeId: json['employeeId'] as String,
      period: json['period'] as String,
      baseSalary: (json['baseSalary'] as num).toDouble(),
      overtimePay: (json['overtimePay'] as num).toDouble(),
      nightWorkPay: (json['nightWorkPay'] as num).toDouble(),
      holidayWorkPay: (json['holidayWorkPay'] as num).toDouble(),
      totalPay: (json['totalPay'] as num).toDouble(),
      taxAmount: (json['taxAmount'] as num).toDouble(),
      insuranceAmount: (json['insuranceAmount'] as num).toDouble(),
      netPay: (json['netPay'] as num).toDouble(),
      calculatedAt: DateTime.parse(json['calculatedAt'] as String),
      isConfirmed: json['isConfirmed'] as bool,
      confirmedAt: json['confirmedAt'] != null
          ? DateTime.parse(json['confirmedAt'] as String)
          : null,
      employeeName: json['employeeName'] as String?,
      storeName: json['storeName'] as String?,
      workDays: json['workDays'] as int?,
      workHours: json['workHours'] != null
          ? (json['workHours'] as num).toDouble()
          : null,
    );
  }

  double get totalAllowance => overtimePay + nightWorkPay + holidayWorkPay;
  double get totalDeduction => taxAmount + insuranceAmount;

  String get periodString {
    final parts = period.split('-');
    if (parts.length == 2) {
      return '${parts[0]}년 ${parts[1]}월';
    }
    return period;
  }
}
