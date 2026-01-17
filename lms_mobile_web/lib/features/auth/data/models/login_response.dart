import 'package:lms_mobile_web/features/auth/data/models/user_info.dart';

class LoginResponse {
  final String accessToken;
  final String refreshToken;
  final UserInfo userInfo;

  LoginResponse({
    required this.accessToken,
    required this.refreshToken,
    required this.userInfo,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      userInfo: UserInfo.fromJson(json['userInfo'] as Map<String, dynamic>),
    );
  }

  Map<String, dynamic> toJson() => {
    'accessToken': accessToken,
    'refreshToken': refreshToken,
    'userInfo': userInfo.toJson(),
  };
}
