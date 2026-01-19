import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/api/api_client.dart';
import 'package:lms_mobile_web/features/payroll/data/services/payroll_service.dart';
import 'package:lms_mobile_web/features/payroll/domain/models/payroll_state.dart';

class PayrollNotifier extends StateNotifier<PayrollState> {
  final PayrollService _payrollService;

  PayrollNotifier(this._payrollService) : super(PayrollState());

  Future<void> loadMyPayrolls() async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final payrolls = await _payrollService.getMyPayrolls();
      state = state.copyWith(
        isLoading: false,
        payrolls: payrolls,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadPayrollDetail(String id) async {
    state = state.copyWith(isLoading: true, clearError: true);

    try {
      final payroll = await _payrollService.getPayrollDetail(id);
      state = state.copyWith(
        isLoading: false,
        selectedPayroll: payroll,
        clearError: true,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  void clearSelectedPayroll() {
    state = state.copyWith(clearSelectedPayroll: true);
  }

  void clearError() {
    state = state.copyWith(clearError: true);
  }
}

final _dioProvider = Provider<Dio>((ref) {
  return ApiClient().dio;
});

final payrollServiceProvider = Provider<PayrollService>((ref) {
  final dio = ref.watch(_dioProvider);
  return PayrollService(dio);
});

final payrollProvider =
    StateNotifierProvider<PayrollNotifier, PayrollState>((ref) {
  final payrollService = ref.watch(payrollServiceProvider);
  return PayrollNotifier(payrollService);
});
