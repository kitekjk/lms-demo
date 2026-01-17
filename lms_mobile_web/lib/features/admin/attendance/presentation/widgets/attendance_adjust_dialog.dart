import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/attendance/domain/models/attendance_record.dart';
import 'package:lms_mobile_web/features/admin/attendance/presentation/providers/attendance_provider.dart';

class AttendanceAdjustDialog extends ConsumerStatefulWidget {
  final AttendanceRecord record;

  const AttendanceAdjustDialog({
    super.key,
    required this.record,
  });

  @override
  ConsumerState<AttendanceAdjustDialog> createState() => _AttendanceAdjustDialogState();
}

class _AttendanceAdjustDialogState extends ConsumerState<AttendanceAdjustDialog> {
  final _formKey = GlobalKey<FormState>();
  final _reasonController = TextEditingController();

  DateTime? _adjustedCheckInTime;
  DateTime? _adjustedCheckOutTime;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _adjustedCheckInTime = widget.record.checkInTime;
    _adjustedCheckOutTime = widget.record.checkOutTime;
  }

  @override
  void dispose() {
    _reasonController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final dateFormat = DateFormat('yyyy-MM-dd (E)', 'ko_KR');
    final timeFormat = DateFormat('HH:mm');

    return AlertDialog(
      title: const Text('출퇴근 시간 수정'),
      content: SizedBox(
        width: 500,
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // 기본 정보
                Card(
                  color: Colors.grey.shade100,
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '근로자: ${widget.record.employeeName ?? widget.record.employeeId}',
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '날짜: ${dateFormat.format(widget.record.attendanceDate)}',
                          style: TextStyle(color: Colors.grey.shade700),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // 원본 시간
                Text(
                  '원본 시간',
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(
                      child: _buildInfoCard(
                        '출근',
                        widget.record.checkInTime != null
                            ? timeFormat.format(widget.record.checkInTime!)
                            : '-',
                        Icons.login,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: _buildInfoCard(
                        '퇴근',
                        widget.record.checkOutTime != null
                            ? timeFormat.format(widget.record.checkOutTime!)
                            : '-',
                        Icons.logout,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 24),

                // 수정할 시간
                Text(
                  '수정할 시간',
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 8),

                // 출근 시간 수정
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.login),
                  title: Text(
                    _adjustedCheckInTime != null
                        ? '출근: ${timeFormat.format(_adjustedCheckInTime!)}'
                        : '출근 시간 선택',
                  ),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (_adjustedCheckInTime != widget.record.checkInTime)
                        const Icon(Icons.edit, size: 16, color: Colors.orange),
                      const SizedBox(width: 8),
                      const Icon(Icons.arrow_drop_down),
                    ],
                  ),
                  onTap: () => _selectDateTime(context, true),
                ),
                const Divider(),

                // 퇴근 시간 수정
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.logout),
                  title: Text(
                    _adjustedCheckOutTime != null
                        ? '퇴근: ${timeFormat.format(_adjustedCheckOutTime!)}'
                        : '퇴근 시간 선택',
                  ),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      if (_adjustedCheckOutTime != widget.record.checkOutTime)
                        const Icon(Icons.edit, size: 16, color: Colors.orange),
                      const SizedBox(width: 8),
                      const Icon(Icons.arrow_drop_down),
                    ],
                  ),
                  onTap: () => _selectDateTime(context, false),
                ),
                const SizedBox(height: 16),

                // 수정 사유
                TextFormField(
                  controller: _reasonController,
                  decoration: const InputDecoration(
                    labelText: '수정 사유 *',
                    hintText: '출퇴근 시간을 수정하는 이유를 입력하세요',
                    border: OutlineInputBorder(),
                    prefixIcon: Icon(Icons.notes),
                  ),
                  maxLines: 3,
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return '수정 사유를 입력하세요';
                    }
                    if (value.trim().length < 5) {
                      return '수정 사유는 최소 5자 이상 입력하세요';
                    }
                    return null;
                  },
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _isLoading ? null : () => Navigator.of(context).pop(),
          child: const Text('취소'),
        ),
        ElevatedButton(
          onPressed: _isLoading ? null : _handleSubmit,
          child: _isLoading
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Text('수정'),
        ),
      ],
    );
  }

  Widget _buildInfoCard(String label, String value, IconData icon) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          children: [
            Icon(icon, size: 20, color: Colors.grey.shade600),
            const SizedBox(width: 8),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                ),
                const SizedBox(height: 2),
                Text(
                  value,
                  style: const TextStyle(fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _selectDateTime(BuildContext context, bool isCheckIn) async {
    final currentTime = isCheckIn ? _adjustedCheckInTime : _adjustedCheckOutTime;
    final initialDate = currentTime ?? widget.record.attendanceDate;

    // 날짜 선택
    final date = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: widget.record.attendanceDate.subtract(const Duration(days: 7)),
      lastDate: widget.record.attendanceDate.add(const Duration(days: 7)),
    );

    if (date == null) return;

    // 시간 선택
    final time = await showTimePicker(
      context: context,
      initialTime: currentTime != null
          ? TimeOfDay.fromDateTime(currentTime)
          : isCheckIn
              ? const TimeOfDay(hour: 9, minute: 0)
              : const TimeOfDay(hour: 18, minute: 0),
    );

    if (time == null) return;

    final selectedDateTime = DateTime(
      date.year,
      date.month,
      date.day,
      time.hour,
      time.minute,
    );

    setState(() {
      if (isCheckIn) {
        _adjustedCheckInTime = selectedDateTime;
      } else {
        _adjustedCheckOutTime = selectedDateTime;
      }
    });
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    // 시간 유효성 검증
    if (_adjustedCheckInTime != null &&
        _adjustedCheckOutTime != null &&
        _adjustedCheckOutTime!.isBefore(_adjustedCheckInTime!)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('퇴근 시간은 출근 시간보다 늦어야 합니다')),
      );
      return;
    }

    // 변경사항이 없는지 확인
    if (_adjustedCheckInTime == widget.record.checkInTime &&
        _adjustedCheckOutTime == widget.record.checkOutTime) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('변경된 시간이 없습니다')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      await ref.read(attendanceNotifierProvider.notifier).adjustRecord(
            recordId: widget.record.id,
            adjustedCheckInTime: _adjustedCheckInTime,
            adjustedCheckOutTime: _adjustedCheckOutTime,
            reason: _reasonController.text.trim(),
          );

      if (mounted) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('출퇴근 시간이 수정되었습니다')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('오류: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }
}
