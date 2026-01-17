import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/attendance/presentation/providers/attendance_provider.dart';

class AttendanceRecordsScreen extends ConsumerStatefulWidget {
  const AttendanceRecordsScreen({super.key});

  @override
  ConsumerState<AttendanceRecordsScreen> createState() =>
      _AttendanceRecordsScreenState();
}

class _AttendanceRecordsScreenState
    extends ConsumerState<AttendanceRecordsScreen> {
  DateTime _startDate = DateTime.now().subtract(const Duration(days: 30));
  DateTime _endDate = DateTime.now();

  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      ref
          .read(attendanceProvider.notifier)
          .loadRecords(startDate: _startDate, endDate: _endDate);
    });
  }

  Future<void> _selectDate(BuildContext context, bool isStartDate) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isStartDate ? _startDate : _endDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );

    if (picked != null) {
      setState(() {
        if (isStartDate) {
          _startDate = picked;
        } else {
          _endDate = picked;
        }
      });

      ref
          .read(attendanceProvider.notifier)
          .loadRecords(startDate: _startDate, endDate: _endDate);
    }
  }

  @override
  Widget build(BuildContext context) {
    final attendanceState = ref.watch(attendanceProvider);
    final dateFormat = DateFormat('yyyy-MM-dd');
    final timeFormat = DateFormat('HH:mm');
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('출퇴근 기록')),
      body: Column(
        children: [
          // 날짜 필터
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _selectDate(context, true),
                    icon: const Icon(Icons.calendar_today),
                    label: Text(dateFormat.format(_startDate)),
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 8.0),
                  child: Text('~'),
                ),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _selectDate(context, false),
                    icon: const Icon(Icons.calendar_today),
                    label: Text(dateFormat.format(_endDate)),
                  ),
                ),
              ],
            ),
          ),

          // 기록 목록
          Expanded(
            child: attendanceState.isLoading
                ? const Center(child: CircularProgressIndicator())
                : attendanceState.records.isEmpty
                ? const Center(child: Text('출퇴근 기록이 없습니다'))
                : ListView.builder(
                    padding: const EdgeInsets.all(16.0),
                    itemCount: attendanceState.records.length,
                    itemBuilder: (context, index) {
                      final record = attendanceState.records[index];
                      return Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor: record.isComplete
                                ? Colors.green
                                : Colors.orange,
                            child: Icon(
                              record.isComplete
                                  ? Icons.check
                                  : Icons.access_time,
                              color: Colors.white,
                            ),
                          ),
                          title: Text(
                            dateFormat.format(record.attendanceDate),
                            style: const TextStyle(fontWeight: FontWeight.bold),
                          ),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const SizedBox(height: 4),
                              if (record.checkInTime != null)
                                Text(
                                  '출근: ${timeFormat.format(record.checkInTime!)}',
                                  style: const TextStyle(color: Colors.green),
                                ),
                              if (record.checkOutTime != null)
                                Text(
                                  '퇴근: ${timeFormat.format(record.checkOutTime!)}',
                                  style: const TextStyle(color: Colors.red),
                                ),
                              if (record.actualWorkHours != null)
                                Text(
                                  '근무: ${record.actualWorkHours!.toStringAsFixed(1)}시간',
                                  style: TextStyle(
                                    color: theme.colorScheme.primary,
                                  ),
                                ),
                            ],
                          ),
                          trailing: _buildStatusChip(record.status),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusChip(String status) {
    Color color;
    String label;

    switch (status) {
      case 'PRESENT':
        color = Colors.green;
        label = '정상';
      case 'LATE':
        color = Colors.orange;
        label = '지각';
      case 'ABSENT':
        color = Colors.red;
        label = '결근';
      case 'ON_LEAVE':
        color = Colors.blue;
        label = '휴가';
      default:
        color = Colors.grey;
        label = status;
    }

    return Chip(
      label: Text(
        label,
        style: const TextStyle(color: Colors.white, fontSize: 12),
      ),
      backgroundColor: color,
      padding: EdgeInsets.zero,
    );
  }
}
