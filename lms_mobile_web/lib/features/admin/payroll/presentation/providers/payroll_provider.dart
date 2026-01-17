import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/providers/dio_provider.dart';
import 'package:lms_mobile_web/features/admin/payroll/data/services/payroll_api_service.dart';
import 'package:lms_mobile_web/features/admin/payroll/domain/models/payroll.dart';

final payrollApiServiceProvider = Provider<PayrollApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return PayrollApiService(dio);
});

// Provider for payrolls by period
final payrollsByPeriodProvider =
    FutureProvider.family<List<Payroll>, String>((ref, period) async {
  final apiService = ref.watch(payrollApiServiceProvider);
  return await apiService.getPayrollsByPeriod(period);
});

// Provider for single payroll
final payrollProvider = FutureProvider.family<Payroll, String>((ref, id) async {
  final apiService = ref.watch(payrollApiServiceProvider);
  return await apiService.getPayroll(id);
});

class PayrollNotifier extends StateNotifier<AsyncValue<void>> {
  final PayrollApiService _apiService;
  final Ref _ref;

  PayrollNotifier(this._apiService, this._ref) : super(const AsyncValue.data(null));

  Future<void> calculatePayroll({
    required String employeeId,
    required String period,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.calculatePayroll(
        employeeId: employeeId,
        period: period,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(payrollsByPeriodProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> executeBatch(String period) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.executeBatch(period);
      state = const AsyncValue.data(null);
      _ref.invalidate(payrollsByPeriodProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}

final payrollNotifierProvider = StateNotifierProvider<PayrollNotifier, AsyncValue<void>>((ref) {
  final apiService = ref.watch(payrollApiServiceProvider);
  return PayrollNotifier(apiService, ref);
});
