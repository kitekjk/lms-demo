import 'package:lms_mobile_web/features/leave/domain/models/leave_request.dart';

class LeaveState {
  final List<LeaveRequest> requests;
  final bool isLoading;
  final String? error;
  final bool isSubmitting;

  LeaveState({
    this.requests = const [],
    this.isLoading = false,
    this.error,
    this.isSubmitting = false,
  });

  LeaveState copyWith({
    List<LeaveRequest>? requests,
    bool? isLoading,
    String? error,
    bool clearError = false,
    bool? isSubmitting,
  }) {
    return LeaveState(
      requests: requests ?? this.requests,
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : (error ?? this.error),
      isSubmitting: isSubmitting ?? this.isSubmitting,
    );
  }
}
