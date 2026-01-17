import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/admin/employee/domain/models/employee.dart';

class EmployeeApiService {
  final Dio dio;

  EmployeeApiService(this.dio);

  Future<List<Employee>> getAllEmployees({String? storeId}) async {
    try {
      final response = await dio.get(
        ApiEndpoints.employees,
        queryParameters: storeId != null ? {'storeId': storeId} : null,
      );
      final data = response.data as Map<String, dynamic>;
      final employees = (data['employees'] as List)
          .map((e) => Employee.fromJson(e as Map<String, dynamic>))
          .toList();
      return employees;
    } catch (e) {
      rethrow;
    }
  }

  Future<Employee> getEmployee(String id) async {
    try {
      final response = await dio.get(ApiEndpoints.employeeById(id));
      return Employee.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<Employee> createEmployee({
    required String userId,
    required String name,
    required String employeeType,
    required String storeId,
  }) async {
    try {
      final response = await dio.post(
        ApiEndpoints.employees,
        data: {
          'userId': userId,
          'name': name,
          'employeeType': employeeType,
          'storeId': storeId,
        },
      );
      return Employee.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<Employee> updateEmployee({
    required String id,
    required String name,
    required String employeeType,
    required String storeId,
  }) async {
    try {
      final response = await dio.put(
        ApiEndpoints.employeeById(id),
        data: {
          'name': name,
          'employeeType': employeeType,
          'storeId': storeId,
        },
      );
      return Employee.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  Future<Employee> deactivateEmployee(String id) async {
    try {
      final response = await dio.patch(ApiEndpoints.employeeDeactivate(id));
      return Employee.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }
}
