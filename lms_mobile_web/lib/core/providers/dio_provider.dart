import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';

final dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});
