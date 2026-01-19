import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/schedule/domain/models/work_schedule.dart';

class ScheduleService {
  final Dio dio;

  ScheduleService(this.dio);

  Future<List<WorkSchedule>> getMySchedules({
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

      final response = await dio.get(
        ApiEndpoints.mySchedule,
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
}
