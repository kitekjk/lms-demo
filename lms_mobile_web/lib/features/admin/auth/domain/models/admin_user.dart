class AdminUser {
  final String userId;
  final String email;
  final String role;
  final String? storeId;
  final String? storeName;

  AdminUser({
    required this.userId,
    required this.email,
    required this.role,
    this.storeId,
    this.storeName,
  });

  factory AdminUser.fromJson(Map<String, dynamic> json) {
    return AdminUser(
      userId: json['userId'] as String,
      email: json['email'] as String,
      role: json['role'] as String,
      storeId: json['storeId'] as String?,
      storeName: json['storeName'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'userId': userId,
      'email': email,
      'role': role,
      'storeId': storeId,
      'storeName': storeName,
    };
  }

  bool get isSuperAdmin => role == 'SUPER_ADMIN';
  bool get isManager => role == 'MANAGER';
  bool get isAdmin => isSuperAdmin || isManager;
}
