import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/payroll/presentation/providers/payroll_provider.dart';
import 'package:lms_mobile_web/features/admin/payroll/presentation/widgets/payroll_detail_dialog.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class PayrollManagementScreen extends ConsumerStatefulWidget {
  const PayrollManagementScreen({super.key});

  @override
  ConsumerState<PayrollManagementScreen> createState() => _PayrollManagementScreenState();
}

class _PayrollManagementScreenState extends ConsumerState<PayrollManagementScreen> {
  DateTime _selectedMonth = DateTime.now();

  String get _period {
    return '${_selectedMonth.year}-${_selectedMonth.month.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    return AdminLayout(
      title: '급여 관리',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 헤더 및 필터
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        '급여 내역 조회',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      const Spacer(),
                      ElevatedButton.icon(
                        onPressed: () => _showBatchExecuteDialog(context),
                        icon: const Icon(Icons.calculate),
                        label: const Text('급여 일괄 계산'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blue,
                          foregroundColor: Colors.white,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      // 기간 선택
                      Expanded(
                        child: Card(
                          elevation: 0,
                          color: Colors.grey.shade100,
                          child: ListTile(
                            leading: const Icon(Icons.calendar_month),
                            title: Text(
                              DateFormat('yyyy년 MM월', 'ko_KR').format(_selectedMonth),
                              style: const TextStyle(
                                fontWeight: FontWeight.w500,
                                fontSize: 16,
                              ),
                            ),
                            subtitle: const Text('조회 기간'),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.chevron_left),
                                  onPressed: () {
                                    setState(() {
                                      _selectedMonth = DateTime(
                                        _selectedMonth.year,
                                        _selectedMonth.month - 1,
                                      );
                                    });
                                  },
                                ),
                                IconButton(
                                  icon: const Icon(Icons.chevron_right),
                                  onPressed: () {
                                    setState(() {
                                      _selectedMonth = DateTime(
                                        _selectedMonth.year,
                                        _selectedMonth.month + 1,
                                      );
                                    });
                                  },
                                ),
                                IconButton(
                                  icon: const Icon(Icons.today),
                                  onPressed: () {
                                    setState(() {
                                      _selectedMonth = DateTime.now();
                                    });
                                  },
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // 급여 내역 목록
          Expanded(
            child: _buildPayrollList(),
          ),
        ],
      ),
    );
  }

  Widget _buildPayrollList() {
    final payrollsAsync = ref.watch(payrollsByPeriodProvider(_period));
    final currencyFormat = NumberFormat.currency(locale: 'ko_KR', symbol: '₩', decimalDigits: 0);

    return payrollsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, _) => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 16),
            Text('오류: ${error.toString()}'),
          ],
        ),
      ),
      data: (payrolls) {
        if (payrolls.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.inbox_outlined, size: 64, color: Colors.grey.shade400),
                const SizedBox(height: 16),
                Text(
                  '해당 기간의 급여 내역이 없습니다',
                  style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                ),
                const SizedBox(height: 24),
                ElevatedButton.icon(
                  onPressed: () => _showBatchExecuteDialog(context),
                  icon: const Icon(Icons.calculate),
                  label: const Text('급여 일괄 계산'),
                ),
              ],
            ),
          );
        }

        // 통계 계산
        final totalEmployees = payrolls.length;
        final confirmedCount = payrolls.where((p) => p.isConfirmed).length;
        final totalNetPay = payrolls.fold<double>(0, (sum, p) => sum + p.netPay);

        return Card(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 통계 헤더
              Container(
                padding: const EdgeInsets.all(16.0),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  border: Border(
                    bottom: BorderSide(color: Colors.grey.shade300),
                  ),
                ),
                child: Row(
                  children: [
                    _buildStatCard(
                      '총 인원',
                      '$totalEmployees명',
                      Icons.people,
                      Colors.blue,
                    ),
                    const SizedBox(width: 16),
                    _buildStatCard(
                      '확정',
                      '$confirmedCount명',
                      Icons.check_circle,
                      Colors.green,
                    ),
                    const SizedBox(width: 16),
                    _buildStatCard(
                      '미확정',
                      '${totalEmployees - confirmedCount}명',
                      Icons.pending,
                      Colors.orange,
                    ),
                    const SizedBox(width: 16),
                    _buildStatCard(
                      '총 지급액',
                      currencyFormat.format(totalNetPay),
                      Icons.attach_money,
                      Colors.purple,
                    ),
                  ],
                ),
              ),
              // 급여 목록
              Expanded(
                child: ListView.separated(
                  itemCount: payrolls.length,
                  separatorBuilder: (context, index) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final payroll = payrolls[index];
                    return ListTile(
                      leading: CircleAvatar(
                        backgroundColor: payroll.isConfirmed
                            ? Colors.green.withOpacity(0.1)
                            : Colors.orange.withOpacity(0.1),
                        child: Icon(
                          payroll.isConfirmed ? Icons.check_circle : Icons.pending,
                          color: payroll.isConfirmed ? Colors.green : Colors.orange,
                          size: 20,
                        ),
                      ),
                      title: Row(
                        children: [
                          Text(
                            payroll.employeeName ?? payroll.employeeId,
                            style: const TextStyle(fontWeight: FontWeight.w500),
                          ),
                          const SizedBox(width: 8),
                          if (payroll.storeName != null)
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                              decoration: BoxDecoration(
                                color: Colors.blue.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                payroll.storeName!,
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: Colors.blue,
                                ),
                              ),
                            ),
                          const SizedBox(width: 8),
                          Chip(
                            label: Text(
                              payroll.isConfirmed ? '확정' : '미확정',
                              style: const TextStyle(fontSize: 12),
                            ),
                            backgroundColor: payroll.isConfirmed
                                ? Colors.green.withOpacity(0.1)
                                : Colors.orange.withOpacity(0.1),
                            labelStyle: TextStyle(
                              color: payroll.isConfirmed ? Colors.green : Colors.orange,
                            ),
                            padding: EdgeInsets.zero,
                            visualDensity: VisualDensity.compact,
                          ),
                        ],
                      ),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              _buildSubtitleItem(
                                Icons.calendar_today,
                                '${payroll.workDays ?? 0}일 근무',
                              ),
                              const SizedBox(width: 16),
                              _buildSubtitleItem(
                                Icons.access_time,
                                '${payroll.workHours?.toStringAsFixed(1) ?? '0'}시간',
                              ),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              Text(
                                '기본급: ${currencyFormat.format(payroll.baseSalary)}',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey.shade700,
                                ),
                              ),
                              const SizedBox(width: 8),
                              if (payroll.totalAllowance > 0)
                                Text(
                                  '수당: ${currencyFormat.format(payroll.totalAllowance)}',
                                  style: const TextStyle(
                                    fontSize: 12,
                                    color: Colors.green,
                                  ),
                                ),
                            ],
                          ),
                        ],
                      ),
                      trailing: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text(
                            '실지급액',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey.shade600,
                            ),
                          ),
                          Text(
                            currencyFormat.format(payroll.netPay),
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: Colors.blue,
                            ),
                          ),
                        ],
                      ),
                      onTap: () => _showDetailDialog(context, payroll),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildStatCard(String label, String value, IconData icon, Color color) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey.shade300),
        ),
        child: Row(
          children: [
            Icon(icon, color: color, size: 24),
            const SizedBox(width: 8),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey.shade600,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  value,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: color,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSubtitleItem(IconData icon, String text) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 14, color: Colors.grey.shade600),
        const SizedBox(width: 4),
        Text(
          text,
          style: TextStyle(fontSize: 12, color: Colors.grey.shade700),
        ),
      ],
    );
  }

  void _showBatchExecuteDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('급여 일괄 계산'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '${DateFormat('yyyy년 MM월', 'ko_KR').format(_selectedMonth)}의 모든 근로자에 대한 급여를 일괄 계산합니다.',
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.orange.shade50,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.orange.shade200),
              ),
              child: Row(
                children: [
                  Icon(Icons.warning_amber, color: Colors.orange.shade700),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      '이미 계산된 급여는 다시 계산되지 않습니다.',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.orange.shade900,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.of(context).pop();
              await ref.read(payrollNotifierProvider.notifier).executeBatch(_period);
              if (mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('급여 계산이 완료되었습니다')),
                );
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
            child: const Text('계산 실행'),
          ),
        ],
      ),
    );
  }

  void _showDetailDialog(BuildContext context, payroll) {
    showDialog(
      context: context,
      builder: (context) => PayrollDetailDialog(payroll: payroll),
    );
  }
}
