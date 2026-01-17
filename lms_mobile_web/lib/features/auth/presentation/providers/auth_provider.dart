import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';
import 'package:lms_mobile_web/core/storage/secure_storage.dart';
import 'package:lms_mobile_web/core/storage/storage_keys.dart';
import 'package:lms_mobile_web/features/auth/data/models/login_request.dart';
import 'package:lms_mobile_web/features/auth/data/models/user_info.dart';
import 'package:lms_mobile_web/features/auth/data/services/auth_service.dart';

// AuthState 정의
class AuthState {
  final UserInfo? user;
  final bool isAuthenticated;
  final bool isLoading;
  final String? error;

  AuthState({
    this.user,
    this.isAuthenticated = false,
    this.isLoading = false,
    this.error,
  });

  AuthState copyWith({
    UserInfo? user,
    bool? isAuthenticated,
    bool? isLoading,
    String? error,
    bool clearError = false,
  }) {
    return AuthState(
      user: user ?? this.user,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

// AuthNotifier
class AuthNotifier extends StateNotifier<AuthState> {
  final AuthService _authService;
  final SecureStorage _storage;

  AuthNotifier(this._authService, this._storage) : super(AuthState()) {
    _checkAuthStatus();
  }

  // 인증 상태 확인
  Future<void> _checkAuthStatus() async {
    final accessToken = await _storage.read(StorageKeys.accessToken);

    if (accessToken != null && accessToken.isNotEmpty) {
      // 저장된 사용자 정보 로드
      final userId = await _storage.read(StorageKeys.userId);
      final userEmail = await _storage.read(StorageKeys.userEmail);
      final userRole = await _storage.read(StorageKeys.userRole);

      if (userId != null && userEmail != null && userRole != null) {
        state = state.copyWith(
          isAuthenticated: true,
          user: UserInfo(
            userId: userId,
            email: userEmail,
            role: userRole,
            isActive: true,
          ),
        );
      }
    }
  }

  // 로그인
  Future<void> login(String email, String password) async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final request = LoginRequest(email: email, password: password);
      final response = await _authService.login(request);

      // 토큰 저장
      await _storage.write(StorageKeys.accessToken, response.accessToken);
      await _storage.write(StorageKeys.refreshToken, response.refreshToken);

      // 사용자 정보 저장
      await _storage.write(StorageKeys.userId, response.userInfo.userId);
      await _storage.write(StorageKeys.userEmail, response.userInfo.email);
      await _storage.write(StorageKeys.userRole, response.userInfo.role);

      state = state.copyWith(
        user: response.userInfo,
        isAuthenticated: true,
        isLoading: false,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
      rethrow;
    }
  }

  // 로그아웃
  Future<void> logout() async {
    try {
      await _authService.logout();
    } finally {
      await _storage.deleteAll();
      state = AuthState(); // 초기 상태로 리셋
    }
  }

  // 에러 클리어
  void clearError() {
    state = state.copyWith(clearError: true);
  }
}

// Providers
final secureStorageProvider = Provider<SecureStorage>((ref) {
  return SecureStorage();
});

final dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});

final authServiceProvider = Provider<AuthService>((ref) {
  final dio = ref.watch(dioProvider);
  return AuthService(dio);
});

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final authService = ref.watch(authServiceProvider);
  final storage = ref.watch(secureStorageProvider);
  return AuthNotifier(authService, storage);
});
