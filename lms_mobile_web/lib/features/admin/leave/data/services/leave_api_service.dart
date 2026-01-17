import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_request.dart';

class LeaveApiService {
  final Dio dio;

  LeaveApiService(this.dio);

  /// 매장별 휴가 신청 목록 조회 (관리자용)
  Future<List<LeaveRequest>> getLeaveRequestsByStore(String storeId) async {
    try {
      final response = await dio.get(
        ApiEndpoints.leaves,
        queryParameters: {'storeId': storeId},
      );

      final data = response.data as Map<String, dynamic>;
      final requests = (data['requests'] as List)
          .map((e) => LeaveRequest.fromJson(e as Map<String, dynamic>))
          .toList();
      return requests;
    } catch (e) {
      rethrow;
    }
  }

  /// 대기 중인 휴가 신청 목록 조회 (관리자용)
  Future<List<LeaveRequest>> getPendingLeaveRequests() async {
    try {
      final response = await dio.get(ApiEndpoints.pendingLeaves);

      final data = response.data as Map<String, dynamic>;
      final requests = (data['requests'] as List)
          .map((e) => LeaveRequest.fromJson(e as Map<String, dynamic>))
          .toList();
      return requests;
    } catch (e) {
      rethrow;
    }
  }

  /// 휴가 승인 (관리자용)
  Future<LeaveRequest> approveLeaveRequest(String leaveId) async {
    try {
      final response = await dio.patch(ApiEndpoints.approveLeave(leaveId));
      return LeaveRequest.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  /// 휴가 반려 (관리자용)
  Future<LeaveRequest> rejectLeaveRequest({
    required String leaveId,
    required String rejectionReason,
  }) async {
    try {
      final response = await dio.patch(
        ApiEndpoints.rejectLeave(leaveId),
        data: {'rejectionReason': rejectionReason},
      );
      return LeaveRequest.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }
}
