import 'package:lms_mobile_web/features/attendance/domain/models/attendance_record.dart';

class AttendanceState {
  final bool isLoading;
  final AttendanceRecord? todayRecord;
  final List<AttendanceRecord> records;
  final String? error;

  AttendanceState({
    this.isLoading = false,
    this.todayRecord,
    this.records = const [],
    this.error,
  });

  AttendanceState copyWith({
    bool? isLoading,
    AttendanceRecord? todayRecord,
    List<AttendanceRecord>? records,
    String? error,
    bool clearError = false,
    bool clearTodayRecord = false,
  }) {
    return AttendanceState(
      isLoading: isLoading ?? this.isLoading,
      todayRecord: clearTodayRecord ? null : (todayRecord ?? this.todayRecord),
      records: records ?? this.records,
      error: clearError ? null : (error ?? this.error),
    );
  }
}
