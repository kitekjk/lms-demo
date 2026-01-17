import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/core/providers/dio_provider.dart';
import 'package:lms_mobile_web/features/admin/employee/data/services/employee_api_service.dart';
import 'package:lms_mobile_web/features/admin/employee/domain/models/employee.dart';

final employeeApiServiceProvider = Provider<EmployeeApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return EmployeeApiService(dio);
});

final employeesProvider = FutureProvider.family<List<Employee>, String?>((ref, storeId) async {
  final apiService = ref.watch(employeeApiServiceProvider);
  return await apiService.getAllEmployees(storeId: storeId);
});

final employeeProvider = FutureProvider.family<Employee, String>((ref, id) async {
  final apiService = ref.watch(employeeApiServiceProvider);
  return await apiService.getEmployee(id);
});

class EmployeeNotifier extends StateNotifier<AsyncValue<void>> {
  final EmployeeApiService _apiService;
  final Ref _ref;

  EmployeeNotifier(this._apiService, this._ref) : super(const AsyncValue.data(null));

  Future<void> createEmployee({
    required String userId,
    required String name,
    required String employeeType,
    required String storeId,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.createEmployee(
        userId: userId,
        name: name,
        employeeType: employeeType,
        storeId: storeId,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(employeesProvider);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> updateEmployee({
    required String id,
    required String name,
    required String employeeType,
    required String storeId,
  }) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.updateEmployee(
        id: id,
        name: name,
        employeeType: employeeType,
        storeId: storeId,
      );
      state = const AsyncValue.data(null);
      _ref.invalidate(employeesProvider);
      _ref.invalidate(employeeProvider(id));
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> deactivateEmployee(String id) async {
    state = const AsyncValue.loading();
    try {
      await _apiService.deactivateEmployee(id);
      state = const AsyncValue.data(null);
      _ref.invalidate(employeesProvider);
      _ref.invalidate(employeeProvider(id));
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}

final employeeNotifierProvider = StateNotifierProvider<EmployeeNotifier, AsyncValue<void>>((ref) {
  final apiService = ref.watch(employeeApiServiceProvider);
  return EmployeeNotifier(apiService, ref);
});
