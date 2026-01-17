import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/exception/api_exception.dart';

class ErrorHandler {
  static ApiException handleError(dynamic error) {
    if (error is DioException) {
      switch (error.type) {
        case DioExceptionType.connectionTimeout:
        case DioExceptionType.sendTimeout:
        case DioExceptionType.receiveTimeout:
          return NetworkException('연결 시간이 초과되었습니다.');

        case DioExceptionType.badResponse:
          return _handleHttpError(error.response);

        case DioExceptionType.cancel:
          return NetworkException('요청이 취소되었습니다.');

        case DioExceptionType.connectionError:
          return NetworkException('서버에 연결할 수 없습니다.');

        default:
          return NetworkException('네트워크 오류가 발생했습니다.');
      }
    }

    if (error is ApiException) {
      return error;
    }

    return ServerException('알 수 없는 오류가 발생했습니다.');
  }

  static ApiException _handleHttpError(Response? response) {
    final statusCode = response?.statusCode;
    final data = response?.data;

    String message = '오류가 발생했습니다.';

    if (data is Map<String, dynamic> && data.containsKey('message')) {
      message = data['message'] as String;
    }

    switch (statusCode) {
      case 400:
        return ValidationException(message);
      case 401:
        return UnauthorizedException(message);
      case 403:
        return ForbiddenException(message);
      case 404:
        return NotFoundException(message);
      case 409:
        return ConflictException(message);
      case 500:
      case 502:
      case 503:
        return ServerException(message);
      default:
        return ServerException(message);
    }
  }
}
