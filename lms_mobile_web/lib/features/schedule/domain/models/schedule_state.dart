import 'package:lms_mobile_web/features/schedule/domain/models/work_schedule.dart';

class ScheduleState {
  final List<WorkSchedule> schedules;
  final bool isLoading;
  final String? error;

  ScheduleState({
    this.schedules = const [],
    this.isLoading = false,
    this.error,
  });

  ScheduleState copyWith({
    List<WorkSchedule>? schedules,
    bool? isLoading,
    String? error,
    bool clearError = false,
  }) {
    return ScheduleState(
      schedules: schedules ?? this.schedules,
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : (error ?? this.error),
    );
  }

  List<WorkSchedule> getSchedulesForDate(DateTime date) {
    return schedules.where((schedule) {
      return schedule.workDate.year == date.year &&
          schedule.workDate.month == date.month &&
          schedule.workDate.day == date.day;
    }).toList();
  }
}
