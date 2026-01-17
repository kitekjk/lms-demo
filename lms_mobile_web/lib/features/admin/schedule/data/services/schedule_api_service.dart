import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/admin/schedule/domain/models/work_schedule.dart';

class ScheduleApiService {
  final Dio dio;

  ScheduleApiService(this.dio);

  Future<List<WorkSchedule>> getSchedules({
    String? employeeId,
    String? storeId,
    DateTime? startDate,
    DateTime? endDate,
  }) async {
    try {
      final queryParams = <String, dynamic>{};
      if (employeeId != null) queryParams['employeeId'] = employeeId;
      if (storeId != null) queryParams['storeId'] = storeId;
      if (startDate != null) {
        queryParams['startDate'] = startDate.toIso8601String().split('T')[0];
      }
      if (endDate != null) {
        queryParams['endDate'] = endDate.toIso8601String().split('T')[0];
      }

      final response = await dio.get(
        ApiEndpoints.schedules,
        queryParameters: queryParams.isNotEmpty ? queryParams : null,
      );

      final data = response.data as Map<String, dynamic>;
      final schedules = (data['schedules'] as List)
          .map((e) => WorkSchedule.fromJson(e as Map<String, dynamic>))
          .toList();
      return schedules;
    } catch (e) {
      rethrow;
    }
  }

  Future<WorkSchedule> getSchedule(String id) async {
    try {
      final response = await dio.get(ApiEndpoints.scheduleById(id));
      return WorkSchedule.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<WorkSchedule> createSchedule({
    required String employeeId,
    required String storeId,
    required DateTime workDate,
    required String startTime,
    required String endTime,
  }) async {
    try {
      final response = await dio.post(
        ApiEndpoints.schedules,
        data: {
          'employeeId': employeeId,
          'storeId': storeId,
          'workDate': workDate.toIso8601String().split('T')[0],
          'startTime': startTime,
          'endTime': endTime,
        },
      );
      return WorkSchedule.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<WorkSchedule> updateSchedule({
    required String id,
    required String employeeId,
    required String storeId,
    required DateTime workDate,
    required String startTime,
    required String endTime,
  }) async {
    try {
      final response = await dio.put(
        ApiEndpoints.scheduleById(id),
        data: {
          'employeeId': employeeId,
          'storeId': storeId,
          'workDate': workDate.toIso8601String().split('T')[0],
          'startTime': startTime,
          'endTime': endTime,
        },
      );
      return WorkSchedule.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<void> deleteSchedule(String id) async {
    try {
      await dio.delete(ApiEndpoints.scheduleById(id));
    } catch (e) {
      rethrow;
    }
  }
}
