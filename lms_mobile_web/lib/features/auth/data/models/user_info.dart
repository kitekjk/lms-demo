class UserInfo {
  final String userId;
  final String email;
  final String role;
  final bool isActive;

  UserInfo({
    required this.userId,
    required this.email,
    required this.role,
    required this.isActive,
  });

  factory UserInfo.fromJson(Map<String, dynamic> json) {
    return UserInfo(
      userId: json['userId'] as String,
      email: json['email'] as String,
      role: json['role'] as String,
      isActive: json['isActive'] as bool,
    );
  }

  Map<String, dynamic> toJson() => {
    'userId': userId,
    'email': email,
    'role': role,
    'isActive': isActive,
  };

  bool get isSuperAdmin => role == 'SUPER_ADMIN';
  bool get isManager => role == 'MANAGER';
  bool get isEmployee => role == 'EMPLOYEE';
}
