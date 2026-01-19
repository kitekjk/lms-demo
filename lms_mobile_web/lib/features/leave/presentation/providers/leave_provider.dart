import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';
import 'package:lms_mobile_web/features/leave/data/services/leave_service.dart';
import 'package:lms_mobile_web/features/leave/domain/models/leave_state.dart';
import 'package:lms_mobile_web/features/leave/domain/models/leave_type.dart';

class LeaveNotifier extends StateNotifier<LeaveState> {
  final LeaveService _leaveService;

  LeaveNotifier(this._leaveService) : super(LeaveState());

  Future<void> loadMyLeaveRequests() async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final requests = await _leaveService.getMyLeaveRequests();
      state = state.copyWith(
        isLoading: false,
        requests: requests,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> createLeaveRequest({
    required LeaveType leaveType,
    required DateTime startDate,
    required DateTime endDate,
    required String reason,
  }) async {
    state = state.copyWith(isSubmitting: true, clearError: true);

    try {
      await _leaveService.createLeaveRequest(
        leaveType: leaveType,
        startDate: startDate,
        endDate: endDate,
        reason: reason,
      );
      state = state.copyWith(isSubmitting: false, clearError: true);
      await loadMyLeaveRequests();
    } catch (e) {
      state = state.copyWith(isSubmitting: false, error: e.toString());
      rethrow;
    }
  }

  Future<void> cancelLeaveRequest(String id) async {
    state = state.copyWith(isSubmitting: true, clearError: true);

    try {
      await _leaveService.cancelLeaveRequest(id);
      state = state.copyWith(isSubmitting: false, clearError: true);
      await loadMyLeaveRequests();
    } catch (e) {
      state = state.copyWith(isSubmitting: false, error: e.toString());
      rethrow;
    }
  }

  void clearError() {
    state = state.copyWith(clearError: true);
  }
}

final _dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});

final leaveServiceProvider = Provider<LeaveService>((ref) {
  final dio = ref.watch(_dioProvider);
  return LeaveService(dio);
});

final leaveProvider = StateNotifierProvider<LeaveNotifier, LeaveState>((ref) {
  final leaveService = ref.watch(leaveServiceProvider);
  return LeaveNotifier(leaveService);
});
