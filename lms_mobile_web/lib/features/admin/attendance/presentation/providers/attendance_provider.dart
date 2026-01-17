import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/providers/dio_provider.dart';
import 'package:lms_mobile_web/features/admin/attendance/data/services/attendance_api_service.dart';
import 'package:lms_mobile_web/features/admin/attendance/domain/models/attendance_record.dart';

final attendanceApiServiceProvider = Provider<AttendanceApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return AttendanceApiService(dio);
});

// Provider for attendance records filtered by parameters
final attendanceRecordsProvider =
    FutureProvider.family<List<AttendanceRecord>, AttendanceFilter>((ref, filter) async {
  final apiService = ref.watch(attendanceApiServiceProvider);
  return await apiService.getRecordsByStore(
    storeId: filter.storeId,
    startDate: filter.startDate,
    endDate: filter.endDate,
  );
});

// Provider for single attendance record
final attendanceRecordProvider = FutureProvider.family<AttendanceRecord, String>((ref, id) async {
  final apiService = ref.watch(attendanceApiServiceProvider);
  return await apiService.getRecord(id);
});

class AttendanceNotifier extends StateNotifier<AsyncValue<void>> {
  final AttendanceApiService _apiService;
  final Ref _ref;

  AttendanceNotifier(this._apiService, this._ref) : super(const AsyncValue.data(null));

  Future<void> adjustRecord({
    required String recordId,
    DateTime? adjustedCheckInTime,
    DateTime? adjustedCheckOutTime,
    required String reason,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.adjustRecord(
        recordId: recordId,
        adjustedCheckInTime: adjustedCheckInTime,
        adjustedCheckOutTime: adjustedCheckOutTime,
        reason: reason,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(attendanceRecordsProvider);
      _ref.invalidate(attendanceRecordProvider(recordId));
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}

final attendanceNotifierProvider = StateNotifierProvider<AttendanceNotifier, AsyncValue<void>>((ref) {
  final apiService = ref.watch(attendanceApiServiceProvider);
  return AttendanceNotifier(apiService, ref);
});

// Filter class for attendance queries
class AttendanceFilter {
  final String storeId;
  final DateTime? startDate;
  final DateTime? endDate;

  const AttendanceFilter({
    required this.storeId,
    this.startDate,
    this.endDate,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AttendanceFilter &&
          runtimeType == other.runtimeType &&
          storeId == other.storeId &&
          startDate == other.startDate &&
          endDate == other.endDate;

  @override
  int get hashCode => storeId.hashCode ^ startDate.hashCode ^ endDate.hashCode;
}
