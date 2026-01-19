import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/payroll/domain/models/payroll.dart';
import 'package:lms_mobile_web/features/payroll/presentation/providers/payroll_provider.dart';

class PayrollDetailScreen extends ConsumerStatefulWidget {
  final String payrollId;

  const PayrollDetailScreen({super.key, required this.payrollId});

  @override
  ConsumerState<PayrollDetailScreen> createState() =>
      _PayrollDetailScreenState();
}

class _PayrollDetailScreenState extends ConsumerState<PayrollDetailScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      ref.read(payrollProvider.notifier).loadPayrollDetail(widget.payrollId);
    });
  }

  @override
  void dispose() {
    ref.read(payrollProvider.notifier).clearSelectedPayroll();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final payrollState = ref.watch(payrollProvider);
    final theme = Theme.of(context);
    final currencyFormat = NumberFormat.currency(locale: 'ko_KR', symbol: '₩');

    return Scaffold(
      appBar: AppBar(
        title: const Text('급여 상세'),
      ),
      body: payrollState.isLoading
          ? const Center(child: CircularProgressIndicator())
          : payrollState.selectedPayroll == null
              ? const Center(child: Text('급여 정보를 불러올 수 없습니다'))
              : SingleChildScrollView(
                  padding: const EdgeInsets.all(16),
                  child: _buildPayrollDetails(
                    payrollState.selectedPayroll!,
                    theme,
                    currencyFormat,
                  ),
                ),
    );
  }

  Widget _buildPayrollDetails(
    Payroll payroll,
    ThemeData theme,
    NumberFormat currencyFormat,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // 기간 및 상태 헤더
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Icon(
                  Icons.calendar_month,
                  size: 40,
                  color: theme.colorScheme.primary,
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        payroll.periodString,
                        style: theme.textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      if (payroll.storeName != null)
                        Text(
                          payroll.storeName!,
                          style: TextStyle(color: Colors.grey.shade600),
                        ),
                    ],
                  ),
                ),
                if (payroll.isConfirmed)
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.green.shade50,
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Text(
                      '확정',
                      style: TextStyle(
                        color: Colors.green.shade700,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),

        // 실수령액 카드
        Card(
          color: theme.colorScheme.primary,
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              children: [
                const Text(
                  '실수령액',
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.white70,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  currencyFormat.format(payroll.netPay),
                  style: const TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),

        // 근무 정보
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '근무 정보',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Divider(),
                _buildInfoRow(
                  '근무일수',
                  '${payroll.workDays ?? 0}일',
                  Icons.work,
                ),
                _buildInfoRow(
                  '근무시간',
                  '${payroll.workHours?.toStringAsFixed(1) ?? 0}시간',
                  Icons.access_time,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),

        // 지급 항목
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '지급 항목',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Divider(),
                _buildAmountRow('기본급', payroll.baseSalary, currencyFormat),
                _buildAmountRow(
                    '연장근무수당', payroll.overtimePay, currencyFormat),
                _buildAmountRow('야간근무수당', payroll.nightWorkPay, currencyFormat),
                _buildAmountRow(
                    '휴일근무수당', payroll.holidayWorkPay, currencyFormat),
                const Divider(),
                _buildAmountRow(
                  '총 지급액',
                  payroll.totalPay,
                  currencyFormat,
                  isTotal: true,
                  color: Colors.blue,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),

        // 공제 항목
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '공제 항목',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Divider(),
                _buildAmountRow('소득세', payroll.taxAmount, currencyFormat,
                    isDeduction: true),
                _buildAmountRow(
                    '4대보험', payroll.insuranceAmount, currencyFormat,
                    isDeduction: true),
                const Divider(),
                _buildAmountRow(
                  '총 공제액',
                  payroll.totalDeduction,
                  currencyFormat,
                  isTotal: true,
                  color: Colors.red,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 24),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value, IconData icon) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: Colors.grey.shade600),
          const SizedBox(width: 12),
          Text(
            label,
            style: TextStyle(color: Colors.grey.shade600),
          ),
          const Spacer(),
          Text(
            value,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _buildAmountRow(
    String label,
    double amount,
    NumberFormat format, {
    bool isTotal = false,
    bool isDeduction = false,
    Color? color,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(
              fontWeight: isTotal ? FontWeight.bold : FontWeight.normal,
              fontSize: isTotal ? 16 : 14,
            ),
          ),
          Text(
            '${isDeduction ? "-" : ""}${format.format(amount)}',
            style: TextStyle(
              fontWeight: isTotal ? FontWeight.bold : FontWeight.normal,
              fontSize: isTotal ? 16 : 14,
              color: color ?? (isDeduction ? Colors.red : null),
            ),
          ),
        ],
      ),
    );
  }
}
