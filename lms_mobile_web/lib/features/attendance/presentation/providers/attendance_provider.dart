import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';
import 'package:lms_mobile_web/features/attendance/data/services/attendance_service.dart';
import 'package:lms_mobile_web/features/attendance/domain/models/attendance_state.dart';

class AttendanceNotifier extends StateNotifier<AttendanceState> {
  final AttendanceService _attendanceService;

  AttendanceNotifier(this._attendanceService) : super(AttendanceState()) {
    _loadTodayRecord();
  }

  Future<void> _loadTodayRecord() async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final todayRecord = await _attendanceService.getTodayRecord();
      state = state.copyWith(
        isLoading: false,
        todayRecord: todayRecord,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> checkIn(String workScheduleId) async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final record = await _attendanceService.checkIn(workScheduleId);
      state = state.copyWith(
        isLoading: false,
        todayRecord: record,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
      rethrow;
    }
  }

  Future<void> checkOut() async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final record = await _attendanceService.checkOut();
      state = state.copyWith(
        isLoading: false,
        todayRecord: record,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
      rethrow;
    }
  }

  Future<void> loadRecords({DateTime? startDate, DateTime? endDate}) async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final records = await _attendanceService.getMyRecords(
        startDate: startDate,
        endDate: endDate,
      );
      state = state.copyWith(
        isLoading: false,
        records: records,
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

// Providers
final attendanceServiceProvider = Provider<AttendanceService>((ref) {
  final dio = ref.watch(dioProvider);
  return AttendanceService(dio);
});

final dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});

final attendanceProvider =
    StateNotifierProvider<AttendanceNotifier, AttendanceState>((ref) {
      final attendanceService = ref.watch(attendanceServiceProvider);
      return AttendanceNotifier(attendanceService);
    });
