import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/providers/dio_provider.dart';
import 'package:lms_mobile_web/features/admin/leave/data/services/leave_api_service.dart';
import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_request.dart';

final leaveApiServiceProvider = Provider<LeaveApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return LeaveApiService(dio);
});

// Provider for leave requests by store
final leaveRequestsByStoreProvider =
    FutureProvider.family<List<LeaveRequest>, String>((ref, storeId) async {
  final apiService = ref.watch(leaveApiServiceProvider);
  return await apiService.getLeaveRequestsByStore(storeId);
});

// Provider for pending leave requests
final pendingLeaveRequestsProvider = FutureProvider<List<LeaveRequest>>((ref) async {
  final apiService = ref.watch(leaveApiServiceProvider);
  return await apiService.getPendingLeaveRequests();
});

class LeaveNotifier extends StateNotifier<AsyncValue<void>> {
  final LeaveApiService _apiService;
  final Ref _ref;

  LeaveNotifier(this._apiService, this._ref) : super(const AsyncValue.data(null));

  Future<void> approveLeave(String leaveId) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.approveLeaveRequest(leaveId);
      state = const AsyncValue.data(null);
      _ref.invalidate(leaveRequestsByStoreProvider);
      _ref.invalidate(pendingLeaveRequestsProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> rejectLeave({
    required String leaveId,
    required String rejectionReason,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.rejectLeaveRequest(
        leaveId: leaveId,
        rejectionReason: rejectionReason,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(leaveRequestsByStoreProvider);
      _ref.invalidate(pendingLeaveRequestsProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}

final leaveNotifierProvider = StateNotifierProvider<LeaveNotifier, AsyncValue<void>>((ref) {
  final apiService = ref.watch(leaveApiServiceProvider);
  return LeaveNotifier(apiService, ref);
});
