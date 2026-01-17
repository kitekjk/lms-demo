import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/providers/dio_provider.dart';
import 'package:lms_mobile_web/features/admin/schedule/data/services/schedule_api_service.dart';
import 'package:lms_mobile_web/features/admin/schedule/domain/models/work_schedule.dart';

final scheduleApiServiceProvider = Provider<ScheduleApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return ScheduleApiService(dio);
});

// Provider for schedules filtered by parameters
final schedulesProvider = FutureProvider.family<List<WorkSchedule>, ScheduleFilter>((ref, filter) async {
  final apiService = ref.watch(scheduleApiServiceProvider);
  return await apiService.getSchedules(
    employeeId: filter.employeeId,
    storeId: filter.storeId,
    startDate: filter.startDate,
    endDate: filter.endDate,
  );
});

// Provider for single schedule
final scheduleProvider = FutureProvider.family<WorkSchedule, String>((ref, id) async {
  final apiService = ref.watch(scheduleApiServiceProvider);
  return await apiService.getSchedule(id);
});

class ScheduleNotifier extends StateNotifier<AsyncValue<void>> {
  final ScheduleApiService _apiService;
  final Ref _ref;

  ScheduleNotifier(this._apiService, this._ref) : super(const AsyncValue.data(null));

  Future<void> createSchedule({
    required String employeeId,
    required String storeId,
    required DateTime workDate,
    required String startTime,
    required String endTime,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.createSchedule(
        employeeId: employeeId,
        storeId: storeId,
        workDate: workDate,
        startTime: startTime,
        endTime: endTime,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(schedulesProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> updateSchedule({
    required String id,
    required String employeeId,
    required String storeId,
    required DateTime workDate,
    required String startTime,
    required String endTime,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.updateSchedule(
        id: id,
        employeeId: employeeId,
        storeId: storeId,
        workDate: workDate,
        startTime: startTime,
        endTime: endTime,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(schedulesProvider);
      _ref.invalidate(scheduleProvider(id));
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> deleteSchedule(String id) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.deleteSchedule(id);
      state = const AsyncValue.data(null);
      _ref.invalidate(schedulesProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}

final scheduleNotifierProvider = StateNotifierProvider<ScheduleNotifier, AsyncValue<void>>((ref) {
  final apiService = ref.watch(scheduleApiServiceProvider);
  return ScheduleNotifier(apiService, ref);
});

// Filter class for schedule queries
class ScheduleFilter {
  final String? employeeId;
  final String? storeId;
  final DateTime? startDate;
  final DateTime? endDate;

  const ScheduleFilter({
    this.employeeId,
    this.storeId,
    this.startDate,
    this.endDate,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ScheduleFilter &&
          runtimeType == other.runtimeType &&
          employeeId == other.employeeId &&
          storeId == other.storeId &&
          startDate == other.startDate &&
          endDate == other.endDate;

  @override
  int get hashCode =>
      employeeId.hashCode ^ storeId.hashCode ^ startDate.hashCode ^ endDate.hashCode;
}
