import 'package:dio/dio.dart';
import 'package:lms_mobile_web/core/api/endpoints.dart';
import 'package:lms_mobile_web/features/payroll/domain/models/payroll.dart';

class PayrollService {
  final Dio dio;

  PayrollService(this.dio);

  Future<List<Payroll>> getMyPayrolls() async {
    try {
      final response = await dio.get(ApiEndpoints.myPayroll);
      final data = response.data as Map<String, dynamic>;
      final payrolls = (data['payrolls'] as List)
          .map((e) => Payroll.fromJson(e as Map<String, dynamic>))
          .toList();
      return payrolls;
    } catch (e) {
      rethrow;
    }
  }

  Future<Payroll> getPayrollDetail(String id) async {
    try {
      final response = await dio.get('${ApiEndpoints.payrolls}/$id');
      return Payroll.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      rethrow;
    }
  }
}
