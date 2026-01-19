import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/leave/domain/models/leave_request.dart';
import 'package:lms_mobile_web/features/leave/domain/models/leave_type.dart';

class LeaveService {
  final Dio dio;

  LeaveService(this.dio);

  Future<List<LeaveRequest>> getMyLeaveRequests() async {
    try {
      final response = await dio.get(ApiEndpoints.myLeaveRequests);
      final data = response.data as Map<String, dynamic>;
      final requests = (data['leaveRequests'] as List)
          .map((e) => LeaveRequest.fromJson(e as Map<String, dynamic>))
          .toList();
      return requests;
    } catch (e) {
      rethrow;
    }
  }

  Future<LeaveRequest> createLeaveRequest({
    required LeaveType leaveType,
    required DateTime startDate,
    required DateTime endDate,
    required String reason,
  }) async {
    try {
      final response = await dio.post(
        ApiEndpoints.leaveRequests,
        data: {
          'leaveType': leaveType.value,
          'startDate': startDate.toIso8601String().split('T')[0],
          'endDate': endDate.toIso8601String().split('T')[0],
          'reason': reason,
        },
      );
      return LeaveRequest.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<void> cancelLeaveRequest(String id) async {
    try {
      await dio.delete('${ApiEndpoints.leaveRequests}/$id');
    } catch (e) {
      rethrow;
    }
  }
}
