import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';
import 'package:lms_mobile_web/features/schedule/data/services/schedule_service.dart';
import 'package:lms_mobile_web/features/schedule/domain/models/schedule_state.dart';

class ScheduleNotifier extends StateNotifier<ScheduleState> {
  final ScheduleService _scheduleService;

  ScheduleNotifier(this._scheduleService) : super(ScheduleState());

  Future<void> loadSchedules({DateTime? startDate, DateTime? endDate}) async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final schedules = await _scheduleService.getMySchedules(
        startDate: startDate,
        endDate: endDate,
      );
      state = state.copyWith(
        isLoading: false,
        schedules: schedules,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  void clearError() {
    state = state.copyWith(clearError: true);
  }
}

final _dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});

final scheduleServiceProvider = Provider<ScheduleService>((ref) {
  final dio = ref.watch(_dioProvider);
  return ScheduleService(dio);
});

final scheduleProvider =
    StateNotifierProvider<ScheduleNotifier, ScheduleState>((ref) {
  final scheduleService = ref.watch(scheduleServiceProvider);
  return ScheduleNotifier(scheduleService);
});
