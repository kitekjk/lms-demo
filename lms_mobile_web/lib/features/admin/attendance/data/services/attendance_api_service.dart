import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/admin/attendance/domain/models/attendance_record.dart';

class AttendanceApiService {
  final Dio dio;

  AttendanceApiService(this.dio);

  /// 매장별 출퇴근 기록 조회 (관리자용)
  Future<List<AttendanceRecord>> getRecordsByStore({
    required String storeId,
    DateTime? startDate,
    DateTime? endDate,
  }) async {
    try {
      final queryParams = <String, dynamic>{'storeId': storeId};

      if (startDate != null) {
        queryParams['startDate'] = startDate.toIso8601String().split('T')[0];
      }
      if (endDate != null) {
        queryParams['endDate'] = endDate.toIso8601String().split('T')[0];
      }

      final response = await dio.get(
        ApiEndpoints.attendanceRecords,
        queryParameters: queryParams,
      );

      final data = response.data as Map<String, dynamic>;
      final records = (data['records'] as List)
          .map((e) => AttendanceRecord.fromJson(e as Map<String, dynamic>))
          .toList();
      return records;
    } catch (e) {
      rethrow;
    }
  }

  /// 출퇴근 기록 단일 조회
  Future<AttendanceRecord> getRecord(String recordId) async {
    try {
      final response = await dio.get(ApiEndpoints.attendanceRecordById(recordId));
      return AttendanceRecord.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  /// 출퇴근 기록 수정 (관리자용)
  Future<AttendanceRecord> adjustRecord({
    required String recordId,
    DateTime? adjustedCheckInTime,
    DateTime? adjustedCheckOutTime,
    required String reason,
  }) async {
    try {
      final response = await dio.put(
        ApiEndpoints.attendanceRecordById(recordId),
        data: {
          if (adjustedCheckInTime != null)
            'adjustedCheckInTime': adjustedCheckInTime.toIso8601String(),
          if (adjustedCheckOutTime != null)
            'adjustedCheckOutTime': adjustedCheckOutTime.toIso8601String(),
          'reason': reason,
        },
      );
      return AttendanceRecord.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }
}
