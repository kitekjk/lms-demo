import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/payroll/domain/models/payroll.dart';

class PayrollDetailDialog extends StatelessWidget {
  final Payroll payroll;

  const PayrollDetailDialog({
    super.key,
    required this.payroll,
  });

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(locale: 'ko_KR', symbol: '₩', decimalDigits: 0);
    final dateTimeFormat = DateFormat('yyyy-MM-dd HH:mm', 'ko_KR');

    return AlertDialog(
      title: Row(
        children: [
          const Text('급여 명세서'),
          const Spacer(),
          _buildStatusChip(),
        ],
      ),
      content: SizedBox(
        width: 600,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // 근로자 정보
              _buildSection(
                '근로자 정보',
                [
                  _buildInfoRow('이름', payroll.employeeName ?? payroll.employeeId),
                  if (payroll.storeName != null)
                    _buildInfoRow('매장', payroll.storeName!),
                  _buildInfoRow('지급 기간', payroll.periodString),
                ],
              ),
              const Divider(height: 32),

              // 근무 정보
              _buildSection(
                '근무 정보',
                [
                  _buildInfoRow('근무 일수', '${payroll.workDays ?? 0}일'),
                  _buildInfoRow('근무 시간', '${payroll.workHours?.toStringAsFixed(1) ?? '0'}시간'),
                ],
              ),
              const Divider(height: 32),

              // 급여 내역
              _buildSection(
                '급여 내역',
                [
                  _buildAmountRow('기본급', payroll.baseSalary, currencyFormat),
                  if (payroll.overtimePay > 0)
                    _buildAmountRow(
                      '초과근무 수당',
                      payroll.overtimePay,
                      currencyFormat,
                      color: Colors.green,
                    ),
                  if (payroll.nightWorkPay > 0)
                    _buildAmountRow(
                      '야간근무 수당',
                      payroll.nightWorkPay,
                      currencyFormat,
                      color: Colors.green,
                    ),
                  if (payroll.holidayWorkPay > 0)
                    _buildAmountRow(
                      '휴일근무 수당',
                      payroll.holidayWorkPay,
                      currencyFormat,
                      color: Colors.green,
                    ),
                  const Divider(),
                  _buildAmountRow(
                    '총 지급액',
                    payroll.totalPay,
                    currencyFormat,
                    isBold: true,
                  ),
                ],
              ),
              const Divider(height: 32),

              // 공제 내역
              _buildSection(
                '공제 내역',
                [
                  _buildAmountRow(
                    '세금',
                    payroll.taxAmount,
                    currencyFormat,
                    color: Colors.red,
                  ),
                  _buildAmountRow(
                    '4대보험',
                    payroll.insuranceAmount,
                    currencyFormat,
                    color: Colors.red,
                  ),
                  const Divider(),
                  _buildAmountRow(
                    '총 공제액',
                    payroll.totalDeduction,
                    currencyFormat,
                    isBold: true,
                    color: Colors.red,
                  ),
                ],
              ),
              const Divider(height: 32),

              // 실지급액
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.blue.shade200, width: 2),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      '실지급액',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      currencyFormat.format(payroll.netPay),
                      style: const TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: Colors.blue,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),

              // 계산 정보
              _buildSection(
                '계산 정보',
                [
                  _buildInfoRow('계산일시', dateTimeFormat.format(payroll.calculatedAt)),
                  if (payroll.confirmedAt != null)
                    _buildInfoRow('확정일시', dateTimeFormat.format(payroll.confirmedAt!)),
                ],
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('닫기'),
        ),
      ],
    );
  }

  Widget _buildStatusChip() {
    final color = payroll.isConfirmed ? Colors.green : Colors.orange;
    return Chip(
      label: Text(
        payroll.isConfirmed ? '확정' : '미확정',
        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
      ),
      backgroundColor: color.withOpacity(0.1),
      labelStyle: TextStyle(color: color),
      padding: EdgeInsets.zero,
      visualDensity: VisualDensity.compact,
    );
  }

  Widget _buildSection(String title, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 12),
        ...children,
      ],
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade700,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAmountRow(
    String label,
    double amount,
    NumberFormat format, {
    bool isBold = false,
    Color? color,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(
              fontSize: 14,
              fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
              color: color,
            ),
          ),
          Text(
            format.format(amount),
            style: TextStyle(
              fontSize: 14,
              fontWeight: isBold ? FontWeight.bold : FontWeight.w500,
              color: color,
            ),
          ),
        ],
      ),
    );
  }
}
