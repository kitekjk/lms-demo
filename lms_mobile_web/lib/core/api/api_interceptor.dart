import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/storage/secure_storage.dart';
import 'package:lms_mobile_web/core/storage/storage_keys.dart';

class ApiInterceptor extends Interceptor {
  final SecureStorage _storage = SecureStorage();

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final accessToken = await _storage.read(StorageKeys.accessToken);
    if (accessToken != null) {
      options.headers['Authorization'] = 'Bearer $accessToken';
    }

    super.onRequest(options, handler);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (err.response?.statusCode == 401) {
      try {
        final refreshToken = await _storage.read(StorageKeys.refreshToken);
        if (refreshToken != null) {
          final response = await Dio().post(
            '${err.requestOptions.baseUrl}/auth/refresh',
            data: {'refreshToken': refreshToken},
          );

          // ignore: avoid_dynamic_calls
          final newAccessToken = response.data['accessToken'] as String;
          await _storage.write(StorageKeys.accessToken, newAccessToken);

          final retryOptions = err.requestOptions;
          retryOptions.headers['Authorization'] = 'Bearer $newAccessToken';

          final retryResponse = await Dio().fetch(retryOptions);
          return handler.resolve(retryResponse);
        }
      } catch (e) {
        await _storage.deleteAll();
      }
    }

    super.onError(err, handler);
  }
}
