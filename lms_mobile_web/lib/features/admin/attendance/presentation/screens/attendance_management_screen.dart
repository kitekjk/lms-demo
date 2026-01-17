import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/attendance/domain/models/attendance_status.dart';
import 'package:lms_mobile_web/features/admin/attendance/presentation/providers/attendance_provider.dart';
import 'package:lms_mobile_web/features/admin/attendance/presentation/widgets/attendance_adjust_dialog.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class AttendanceManagementScreen extends ConsumerStatefulWidget {
  const AttendanceManagementScreen({super.key});

  @override
  ConsumerState<AttendanceManagementScreen> createState() => _AttendanceManagementScreenState();
}

class _AttendanceManagementScreenState extends ConsumerState<AttendanceManagementScreen> {
  String? _selectedStoreId;
  DateTime _startDate = DateTime.now().subtract(const Duration(days: 30));
  DateTime _endDate = DateTime.now();

  @override
  Widget build(BuildContext context) {
    final storesAsync = ref.watch(storesProvider);

    return AdminLayout(
      title: '출퇴근 기록 관리',
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
                  Text(
                    '출퇴근 기록 조회',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      // 매장 선택
                      Expanded(
                        flex: 2,
                        child: storesAsync.when(
                          loading: () => const LinearProgressIndicator(),
                          error: (error, _) => Text('매장 목록 로드 실패: ${error.toString()}'),
                          data: (stores) => DropdownButtonFormField<String>(
                            value: _selectedStoreId,
                            decoration: const InputDecoration(
                              labelText: '매장 선택',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.store),
                            ),
                            hint: const Text('매장을 선택하세요'),
                            items: stores.map((store) {
                              return DropdownMenuItem(
                                value: store.id,
                                child: Text(store.name),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() => _selectedStoreId = value);
                            },
                          ),
                        ),
                      ),
                      const SizedBox(width: 16),
                      // 시작 날짜
                      Expanded(
                        child: ListTile(
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(4),
                            side: BorderSide(color: Colors.grey.shade400),
                          ),
                          leading: const Icon(Icons.calendar_today),
                          title: Text(
                            DateFormat('yyyy-MM-dd').format(_startDate),
                            style: const TextStyle(fontSize: 14),
                          ),
                          subtitle: const Text('시작일', style: TextStyle(fontSize: 12)),
                          onTap: () async {
                            final date = await showDatePicker(
                              context: context,
                              initialDate: _startDate,
                              firstDate: DateTime(2020),
                              lastDate: DateTime.now(),
                            );
                            if (date != null) {
                              setState(() => _startDate = date);
                            }
                          },
                        ),
                      ),
                      const SizedBox(width: 16),
                      // 종료 날짜
                      Expanded(
                        child: ListTile(
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(4),
                            side: BorderSide(color: Colors.grey.shade400),
                          ),
                          leading: const Icon(Icons.calendar_today),
                          title: Text(
                            DateFormat('yyyy-MM-dd').format(_endDate),
                            style: const TextStyle(fontSize: 14),
                          ),
                          subtitle: const Text('종료일', style: TextStyle(fontSize: 12)),
                          onTap: () async {
                            final date = await showDatePicker(
                              context: context,
                              initialDate: _endDate,
                              firstDate: DateTime(2020),
                              lastDate: DateTime.now(),
                            );
                            if (date != null) {
                              setState(() => _endDate = date);
                            }
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // 출퇴근 기록 목록
          Expanded(
            child: _selectedStoreId == null
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.store_outlined, size: 64, color: Colors.grey.shade400),
                        const SizedBox(height: 16),
                        Text(
                          '매장을 선택하여 출퇴근 기록을 조회하세요',
                          style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                        ),
                      ],
                    ),
                  )
                : _buildRecordsList(),
          ),
        ],
      ),
    );
  }

  Widget _buildRecordsList() {
    final filter = AttendanceFilter(
      storeId: _selectedStoreId!,
      startDate: _startDate,
      endDate: _endDate,
    );

    final recordsAsync = ref.watch(attendanceRecordsProvider(filter));
    final dateFormat = DateFormat('yyyy-MM-dd (E)', 'ko_KR');
    final timeFormat = DateFormat('HH:mm');

    return recordsAsync.when(
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
      data: (records) {
        if (records.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.inbox_outlined, size: 64, color: Colors.grey.shade400),
                const SizedBox(height: 16),
                Text(
                  '해당 기간의 출퇴근 기록이 없습니다',
                  style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                ),
              ],
            ),
          );
        }

        return Card(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    Text(
                      '총 ${records.length}건',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                    const Spacer(),
                    // 통계 요약
                    _buildSummaryChip(
                      '정상',
                      records.where((r) => r.status == AttendanceStatus.normal).length,
                      Colors.green,
                    ),
                    const SizedBox(width: 8),
                    _buildSummaryChip(
                      '지각',
                      records.where((r) => r.status == AttendanceStatus.late).length,
                      Colors.orange,
                    ),
                    const SizedBox(width: 8),
                    _buildSummaryChip(
                      '조퇴',
                      records.where((r) => r.status == AttendanceStatus.earlyLeave).length,
                      Colors.blue,
                    ),
                    const SizedBox(width: 8),
                    _buildSummaryChip(
                      '결근',
                      records.where((r) => r.status == AttendanceStatus.absent).length,
                      Colors.red,
                    ),
                  ],
                ),
              ),
              const Divider(height: 1),
              Expanded(
                child: ListView.separated(
                  itemCount: records.length,
                  separatorBuilder: (context, index) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final record = records[index];
                    return ListTile(
                      leading: CircleAvatar(
                        backgroundColor: _getStatusColor(record.status).withOpacity(0.1),
                        child: Icon(
                          _getStatusIcon(record.status),
                          color: _getStatusColor(record.status),
                          size: 20,
                        ),
                      ),
                      title: Row(
                        children: [
                          Text(
                            record.employeeName ?? record.employeeId,
                            style: const TextStyle(fontWeight: FontWeight.w500),
                          ),
                          const SizedBox(width: 8),
                          Chip(
                            label: Text(
                              record.status.displayName,
                              style: const TextStyle(fontSize: 12),
                            ),
                            backgroundColor: _getStatusColor(record.status).withOpacity(0.1),
                            labelStyle: TextStyle(color: _getStatusColor(record.status)),
                            padding: EdgeInsets.zero,
                            visualDensity: VisualDensity.compact,
                          ),
                        ],
                      ),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: 4),
                          Text(dateFormat.format(record.attendanceDate)),
                          const SizedBox(height: 2),
                          Row(
                            children: [
                              Icon(Icons.login, size: 14, color: Colors.grey.shade600),
                              const SizedBox(width: 4),
                              Text(
                                record.checkInTime != null
                                    ? timeFormat.format(record.checkInTime!)
                                    : '-',
                                style: TextStyle(color: Colors.grey.shade700),
                              ),
                              const SizedBox(width: 16),
                              Icon(Icons.logout, size: 14, color: Colors.grey.shade600),
                              const SizedBox(width: 4),
                              Text(
                                record.checkOutTime != null
                                    ? timeFormat.format(record.checkOutTime!)
                                    : '-',
                                style: TextStyle(color: Colors.grey.shade700),
                              ),
                              const SizedBox(width: 16),
                              if (record.actualWorkHours != null) ...[
                                Icon(Icons.access_time, size: 14, color: Colors.grey.shade600),
                                const SizedBox(width: 4),
                                Text(
                                  '${record.actualWorkHours!.toStringAsFixed(1)}시간',
                                  style: TextStyle(color: Colors.grey.shade700),
                                ),
                              ],
                            ],
                          ),
                          if (record.note != null) ...[
                            const SizedBox(height: 2),
                            Text(
                              '비고: ${record.note}',
                              style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                            ),
                          ],
                        ],
                      ),
                      trailing: IconButton(
                        icon: const Icon(Icons.edit, size: 20),
                        tooltip: '수정',
                        onPressed: () => _showAdjustDialog(context, record),
                      ),
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

  Widget _buildSummaryChip(String label, int count, Color color) {
    return Chip(
      label: Text('$label: $count', style: const TextStyle(fontSize: 12)),
      backgroundColor: color.withOpacity(0.1),
      labelStyle: TextStyle(color: color),
      padding: EdgeInsets.zero,
      visualDensity: VisualDensity.compact,
    );
  }

  Color _getStatusColor(AttendanceStatus status) {
    switch (status) {
      case AttendanceStatus.normal:
        return Colors.green;
      case AttendanceStatus.late:
        return Colors.orange;
      case AttendanceStatus.earlyLeave:
        return Colors.blue;
      case AttendanceStatus.absent:
        return Colors.red;
      case AttendanceStatus.checkedIn:
        return Colors.purple;
    }
  }

  IconData _getStatusIcon(AttendanceStatus status) {
    switch (status) {
      case AttendanceStatus.normal:
        return Icons.check_circle;
      case AttendanceStatus.late:
        return Icons.schedule;
      case AttendanceStatus.earlyLeave:
        return Icons.exit_to_app;
      case AttendanceStatus.absent:
        return Icons.cancel;
      case AttendanceStatus.checkedIn:
        return Icons.work;
    }
  }

  void _showAdjustDialog(BuildContext context, record) {
    showDialog(
      context: context,
      builder: (context) => AttendanceAdjustDialog(record: record),
    );
  }
}
