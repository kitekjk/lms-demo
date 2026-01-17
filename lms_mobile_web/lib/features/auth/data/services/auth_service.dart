import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/core/exception/error_handler.dart';
import 'package:lms_mobile_web/features/auth/data/models/login_request.dart';
import 'package:lms_mobile_web/features/auth/data/models/login_response.dart';

class AuthService {
  final Dio _dio;

  AuthService(this._dio);

  Future<LoginResponse> login(LoginRequest request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.login,
        data: request.toJson(),
      );

      return LoginResponse.fromJson(response.data as Map<String, dynamic>);
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

  Future<String> refreshToken(String refreshToken) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.refresh,
        data: {'refreshToken': refreshToken},
      );

      // ignore: avoid_dynamic_calls
      return response.data['accessToken'] as String;
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }
}
