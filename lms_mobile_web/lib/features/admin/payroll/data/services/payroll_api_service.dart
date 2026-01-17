import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/admin/payroll/domain/models/payroll.dart';

class PayrollApiService {
  final Dio dio;

  PayrollApiService(this.dio);

  /// 기간별 급여 내역 조회 (관리자용)
  /// period format: YYYY-MM
  Future<List<Payroll>> getPayrollsByPeriod(String period) async {
    try {
      final response = await dio.get(
        ApiEndpoints.payrolls,
        queryParameters: {'period': period},
      );

      final payrolls = (response.data as List)
          .map((e) => Payroll.fromJson(e as Map<String, dynamic>))
          .toList();
      return payrolls;
    } catch (e) {
      rethrow;
    }
  }

  /// 급여 상세 조회
  Future<Payroll> getPayroll(String payrollId) async {
    try {
      final response = await dio.get(ApiEndpoints.payrollById(payrollId));
      return Payroll.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  /// 급여 계산 실행 (관리자용)
  Future<Payroll> calculatePayroll({
    required String employeeId,
    required String period,
  }) async {
    try {
      final response = await dio.post(
        ApiEndpoints.calculatePayroll,
        data: {
          'employeeId': employeeId,
          'period': period,
        },
      );
      return Payroll.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }

  /// 급여 배치 실행 (슈퍼 관리자용)
  Future<void> executeBatch(String period) async {
    try {
      await dio.post(
        ApiEndpoints.payrollBatch,
        data: {'period': period},
      );
    } catch (e) {
      rethrow;
    }
  }
}
