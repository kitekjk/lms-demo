import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';
import 'package:lms_mobile_web/core/storage/secure_storage.dart';
import 'package:lms_mobile_web/core/storage/storage_keys.dart';
import 'package:lms_mobile_web/features/admin/auth/data/services/admin_auth_service.dart';
import 'package:lms_mobile_web/features/admin/auth/domain/models/admin_auth_state.dart';
import 'package:lms_mobile_web/features/admin/auth/domain/models/admin_user.dart';

class AdminAuthNotifier extends StateNotifier<AdminAuthState> {
  final AdminAuthService _authService;
  final SecureStorage _storage;

  AdminAuthNotifier(this._authService, this._storage)
    : super(AdminAuthState.initial()) {
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    final accessToken = await _storage.read(StorageKeys.accessToken);

    if (accessToken != null && accessToken.isNotEmpty) {
      final userId = await _storage.read(StorageKeys.userId);
      final userEmail = await _storage.read(StorageKeys.userEmail);
      final userRole = await _storage.read(StorageKeys.userRole);
      final storeId = await _storage.read(StorageKeys.storeId);
      final storeName = await _storage.read(StorageKeys.storeName);

      if (userId != null &&
          userEmail != null &&
          userRole != null &&
          (userRole == 'SUPER_ADMIN' || userRole == 'MANAGER')) {
        state = AdminAuthState.authenticated(
          AdminUser(
            userId: userId,
            email: userEmail,
            role: userRole,
            storeId: storeId,
            storeName: storeName,
          ),
        );
      }
    }
  }

  Future<void> login(String email, String password) async {
    state = AdminAuthState.loading();

    try {
      final result = await _authService.login(email, password);

      // 토큰 저장
      await _storage.write(
        StorageKeys.accessToken,
        result['accessToken'] as String,
      );
      await _storage.write(
        StorageKeys.refreshToken,
        result['refreshToken'] as String,
      );

      // 사용자 정보 저장
      final user = result['user'] as AdminUser;
      await _storage.write(StorageKeys.userId, user.userId);
      await _storage.write(StorageKeys.userEmail, user.email);
      await _storage.write(StorageKeys.userRole, user.role);
      if (user.storeId != null) {
        await _storage.write(StorageKeys.storeId, user.storeId!);
      }
      if (user.storeName != null) {
        await _storage.write(StorageKeys.storeName, user.storeName!);
      }

      state = AdminAuthState.authenticated(user);
    } catch (e) {
      state = AdminAuthState.error(e.toString());
      rethrow;
    }
  }

  Future<void> logout() async {
    try {
      await _authService.logout();
    } finally {
      await _storage.deleteAll();
      state = AdminAuthState.unauthenticated();
    }
  }
}

// Providers
final adminAuthServiceProvider = Provider<AdminAuthService>((ref) {
  final dio = ref.watch(dioProvider);
  return AdminAuthService(dio);
});

final dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});

final adminAuthProvider =
    StateNotifierProvider<AdminAuthNotifier, AdminAuthState>((ref) {
      final authService = ref.watch(adminAuthServiceProvider);
      final storage = SecureStorage();
      return AdminAuthNotifier(authService, storage);
    });
