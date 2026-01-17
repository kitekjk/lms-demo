import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/employee/presentation/providers/employee_provider.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class EmployeeListScreen extends ConsumerStatefulWidget {
  const EmployeeListScreen({super.key});

  @override
  ConsumerState<EmployeeListScreen> createState() => _EmployeeListScreenState();
}

class _EmployeeListScreenState extends ConsumerState<EmployeeListScreen> {
  String? _selectedStoreId;

  @override
  Widget build(BuildContext context) {
    final employeesAsync = ref.watch(employeesProvider(_selectedStoreId));
    final storesAsync = ref.watch(storesProvider);

    return AdminLayout(
      title: '근로자 관리',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '근로자 목록',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              Row(
                children: [
                  // 매장 필터
                  storesAsync.when(
                    loading: () => const SizedBox(width: 200, child: LinearProgressIndicator()),
                    error: (_, __) => const SizedBox.shrink(),
                    data: (stores) => SizedBox(
                      width: 200,
                      child: DropdownButtonFormField<String?>(
                        initialValue: _selectedStoreId,
                        decoration: const InputDecoration(
                          labelText: '매장 필터',
                          border: OutlineInputBorder(),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        items: [
                          const DropdownMenuItem<String?>(
                            child: Text('전체 매장'),
                          ),
                          ...stores.map((store) => DropdownMenuItem(
                            value: store.id,
                            child: Text(store.name),
                          )),
                        ],
                        onChanged: (value) {
                          setState(() => _selectedStoreId = value);
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  ElevatedButton.icon(
                    onPressed: () {
                      context.push('${RouteNames.adminEmployees}/new');
                    },
                    icon: const Icon(Icons.add),
                    label: const Text('근로자 추가'),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 24),
          Expanded(
            child: employeesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (error, stack) => Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.error_outline, size: 48, color: Colors.red),
                    const SizedBox(height: 16),
                    Text('오류: ${error.toString()}'),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () => ref.invalidate(employeesProvider),
                      child: const Text('다시 시도'),
                    ),
                  ],
                ),
              ),
              data: (employees) {
                if (employees.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.people_outlined, size: 48, color: Colors.grey),
                        const SizedBox(height: 16),
                        const Text('등록된 근로자가 없습니다'),
                        const SizedBox(height: 16),
                        ElevatedButton.icon(
                          onPressed: () {
                            context.push('${RouteNames.adminEmployees}/new');
                          },
                          icon: const Icon(Icons.add),
                          label: const Text('근로자 추가'),
                        ),
                      ],
                    ),
                  );
                }

                return Card(
                  child: SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: DataTable(
                      columns: const [
                        DataColumn(label: Text('이름')),
                        DataColumn(label: Text('사용자 ID')),
                        DataColumn(label: Text('근로자 유형')),
                        DataColumn(label: Text('잔여 연차')),
                        DataColumn(label: Text('상태')),
                        DataColumn(label: Text('등록일')),
                        DataColumn(label: Text('작업')),
                      ],
                      rows: employees.map((employee) {
                        final dateFormat = DateFormat('yyyy-MM-dd');

                        return DataRow(
                          cells: [
                            DataCell(Text(employee.name)),
                            DataCell(Text(employee.userId)),
                            DataCell(Text(employee.employeeType.displayName)),
                            DataCell(Text('${employee.remainingLeave}일')),
                            DataCell(
                              Chip(
                                label: Text(employee.isActive ? '활성' : '비활성'),
                                backgroundColor: employee.isActive
                                    ? Colors.green.shade100
                                    : Colors.red.shade100,
                                labelStyle: TextStyle(
                                  color: employee.isActive ? Colors.green.shade900 : Colors.red.shade900,
                                ),
                              ),
                            ),
                            DataCell(Text(dateFormat.format(employee.createdAt))),
                            DataCell(
                              Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  IconButton(
                                    icon: const Icon(Icons.edit, size: 20),
                                    tooltip: '수정',
                                    onPressed: () {
                                      context.push('${RouteNames.adminEmployees}/${employee.id}/edit');
                                    },
                                  ),
                                  if (employee.isActive)
                                    IconButton(
                                      icon: const Icon(Icons.block, size: 20, color: Colors.orange),
                                      tooltip: '비활성화',
                                      onPressed: () => _confirmDeactivate(context, ref, employee.id, employee.name),
                                    ),
                                ],
                              ),
                            ),
                          ],
                        );
                      }).toList(),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  void _confirmDeactivate(BuildContext context, WidgetRef ref, String employeeId, String employeeName) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('근로자 비활성화'),
        content: Text('정말 "$employeeName" 근로자를 비활성화하시겠습니까?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.of(context).pop();
              await ref.read(employeeNotifierProvider.notifier).deactivateEmployee(employeeId);
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('근로자가 비활성화되었습니다')),
                );
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
            child: const Text('비활성화'),
          ),
        ],
      ),
    );
  }
}
