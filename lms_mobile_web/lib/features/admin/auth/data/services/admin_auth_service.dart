import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/core/exception/error_handler.dart';
import 'package:lms_mobile_web/features/admin/auth/domain/models/admin_user.dart';

class AdminAuthService {
  final Dio _dio;

  AdminAuthService(this._dio);

  Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.login,
        data: {'email': email, 'password': password},
      );

      // ignore: avoid_dynamic_calls
      final data = response.data['data'] as Map<String, dynamic>;
      final user = AdminUser.fromJson(data['user'] as Map<String, dynamic>);

      // Validate admin role
      if (!user.isAdmin) {
        throw Exception('관리자 권한이 없습니다.');
      }

      return {
        'accessToken': data['accessToken'] as String,
        'refreshToken': data['refreshToken'] as String,
        'user': user,
      };
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }

  Future<void> logout() async {
    try {
      await _dio.post(ApiEndpoints.logout);
    } catch (e) {
      // 로그아웃은 실패해도 로컬 토큰 삭제
      // ignore: avoid_print
      print('Logout API error: $e');
    }
  }

  Future<AdminUser> getCurrentUser() async {
    try {
      final response = await _dio.get(ApiEndpoints.me);
      // ignore: avoid_dynamic_calls
      final user = AdminUser.fromJson(
        response.data['data'] as Map<String, dynamic>,
      );

      if (!user.isAdmin) {
        throw Exception('관리자 권한이 없습니다.');
      }

      return user;
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }
}
