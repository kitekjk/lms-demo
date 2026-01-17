import 'package:lms_mobile_web/features/admin/auth/domain/models/admin_user.dart';

class AdminAuthState {
  final AdminUser? user;
  final bool isLoading;
  final String? error;
  final bool isAuthenticated;

  AdminAuthState({
    this.user,
    this.isLoading = false,
    this.error,
    this.isAuthenticated = false,
  });

  AdminAuthState copyWith({
    AdminUser? user,
    bool? isLoading,
    String? error,
    bool? isAuthenticated,
  }) {
    return AdminAuthState(
      user: user ?? this.user,
      isLoading: isLoading ?? this.isLoading,
      error: error,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
    );
  }

  factory AdminAuthState.initial() {
    return AdminAuthState();
  }

  factory AdminAuthState.loading() {
    return AdminAuthState(isLoading: true);
  }

  factory AdminAuthState.authenticated(AdminUser user) {
    return AdminAuthState(user: user, isAuthenticated: true);
  }

  factory AdminAuthState.error(String error) {
    return AdminAuthState(error: error);
  }

  factory AdminAuthState.unauthenticated() {
    return AdminAuthState(isAuthenticated: false);
  }
}
