import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/core/exception/error_handler.dart';
import 'package:lms_mobile_web/features/attendance/domain/models/attendance_record.dart';

class AttendanceService {
  final Dio _dio;

  AttendanceService(this._dio);

  Future<AttendanceRecord> checkIn(String workScheduleId) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.checkIn,
        data: {'workScheduleId': workScheduleId},
      );

      return AttendanceRecord.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }

  Future<AttendanceRecord> checkOut() async {
    try {
      final response = await _dio.post(ApiEndpoints.checkOut);

      return AttendanceRecord.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }

  Future<List<AttendanceRecord>> getMyRecords({
    DateTime? startDate,
    DateTime? endDate,
  }) async {
    try {
      final queryParams = <String, dynamic>{};
      if (startDate != null) {
        queryParams['startDate'] = startDate.toIso8601String().split('T')[0];
      }
      if (endDate != null) {
        queryParams['endDate'] = endDate.toIso8601String().split('T')[0];
      }

      final response = await _dio.get(
        ApiEndpoints.myAttendance,
        queryParameters: queryParams,
      );

      // ignore: avoid_dynamic_calls
      final records = response.data['records'] as List;
      return records
          .map((record) =>
              AttendanceRecord.fromJson(record as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }

  Future<AttendanceRecord?> getTodayRecord() async {
    try {
      final today = DateTime.now();
      final records = await getMyRecords(
        startDate: today,
        endDate: today,
      );

      return records.isNotEmpty ? records.first : null;
    } catch (e) {
      throw ErrorHandler.handleError(e);
    }
  }
}
