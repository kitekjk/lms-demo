import 'package:lms_mobile_web/features/payroll/domain/models/payroll.dart';

class PayrollState {
  final List<Payroll> payrolls;
  final Payroll? selectedPayroll;
  final bool isLoading;
  final String? error;

  PayrollState({
    this.payrolls = const [],
    this.selectedPayroll,
    this.isLoading = false,
    this.error,
  });

  PayrollState copyWith({
    List<Payroll>? payrolls,
    Payroll? selectedPayroll,
    bool? isLoading,
    String? error,
    bool clearError = false,
    bool clearSelectedPayroll = false,
  }) {
    return PayrollState(
      payrolls: payrolls ?? this.payrolls,
      selectedPayroll:
          clearSelectedPayroll ? null : (selectedPayroll ?? this.selectedPayroll),
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : (error ?? this.error),
    );
  }
}
