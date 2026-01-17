abstract class ApiException implements Exception {
  final String message;
  final String? code;

  ApiException(this.message, {this.code});

  @override
  String toString() => message;
}

// 401 Unauthorized
class UnauthorizedException extends ApiException {
  UnauthorizedException(super.message) : super(code: 'UNAUTHORIZED');
}

// 403 Forbidden
class ForbiddenException extends ApiException {
  ForbiddenException(super.message) : super(code: 'FORBIDDEN');
}

// 404 Not Found
class NotFoundException extends ApiException {
  NotFoundException(super.message) : super(code: 'NOT_FOUND');
}

// 409 Conflict
class ConflictException extends ApiException {
  ConflictException(super.message) : super(code: 'CONFLICT');
}

// 422 Validation Error
class ValidationException extends ApiException {
  ValidationException(super.message) : super(code: 'VALIDATION_ERROR');
}

// 500 Server Error
class ServerException extends ApiException {
  ServerException(super.message) : super(code: 'SERVER_ERROR');
}

// Network Error
class NetworkException extends ApiException {
  NetworkException(super.message) : super(code: 'NETWORK_ERROR');
}
