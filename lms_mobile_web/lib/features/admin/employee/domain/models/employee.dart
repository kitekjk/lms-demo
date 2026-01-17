enum EmployeeType {
  regular('REGULAR', '정규직'),
  irregular('IRREGULAR', '계약직'),
  partTime('PART_TIME', '아르바이트');

  const EmployeeType(this.value, this.displayName);

  final String value;
  final String displayName;

  static EmployeeType fromString(String value) {
    return EmployeeType.values.firstWhere((e) => e.value == value);
  }
}

class Employee {
  final String id;
  final String userId;
  final String name;
  final EmployeeType employeeType;
  final String? storeId;
  final double remainingLeave;
  final bool isActive;
  final DateTime createdAt;

  Employee({
    required this.id,
    required this.userId,
    required this.name,
    required this.employeeType,
    this.storeId,
    required this.remainingLeave,
    required this.isActive,
    required this.createdAt,
  });

  factory Employee.fromJson(Map<String, dynamic> json) {
    return Employee(
      id: json['id'] as String,
      userId: json['userId'] as String,
      name: json['name'] as String,
      employeeType: EmployeeType.fromString(json['employeeType'] as String),
      storeId: json['storeId'] as String?,
      remainingLeave: (json['remainingLeave'] as num).toDouble(),
      isActive: json['isActive'] as bool,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'userId': userId,
      'name': name,
      'employeeType': employeeType.value,
      'storeId': storeId,
      'remainingLeave': remainingLeave,
      'isActive': isActive,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}
