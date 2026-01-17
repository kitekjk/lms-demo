import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/features/admin/employee/presentation/providers/employee_provider.dart';
import 'package:lms_mobile_web/features/admin/schedule/presentation/providers/schedule_provider.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';

class ScheduleFormDialog extends ConsumerStatefulWidget {
  final String? scheduleId;
  final DateTime selectedDate;

  const ScheduleFormDialog({
    super.key,
    this.scheduleId,
    required this.selectedDate,
  });

  @override
  ConsumerState<ScheduleFormDialog> createState() => _ScheduleFormDialogState();
}

class _ScheduleFormDialogState extends ConsumerState<ScheduleFormDialog> {
  final _formKey = GlobalKey<FormState>();
  String? _selectedEmployeeId;
  String? _selectedStoreId;
  DateTime? _selectedDate;
  TimeOfDay? _startTime;
  TimeOfDay? _endTime;
  bool _isLoading = false;

  bool get isEditMode => widget.scheduleId != null;

  @override
  void initState() {
    super.initState();
    _selectedDate = widget.selectedDate;
    if (isEditMode) {
      _loadSchedule();
    }
  }

  Future<void> _loadSchedule() async {
    final schedule = await ref.read(scheduleProvider(widget.scheduleId!).future);
    setState(() {
      _selectedEmployeeId = schedule.employeeId;
      _selectedStoreId = schedule.storeId;
      _selectedDate = schedule.workDate;
      _startTime = _parseTime(schedule.startTime);
      _endTime = _parseTime(schedule.endTime);
    });
  }

  TimeOfDay _parseTime(String timeString) {
    final parts = timeString.split(':');
    return TimeOfDay(hour: int.parse(parts[0]), minute: int.parse(parts[1]));
  }

  String _formatTime(TimeOfDay time) {
    return '${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final storesAsync = ref.watch(storesProvider);
    final employeesAsync = ref.watch(employeesProvider(_selectedStoreId));

    return AlertDialog(
      title: Text(isEditMode ? '일정 수정' : '일정 추가'),
      content: SizedBox(
        width: 500,
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // 매장 선택
                storesAsync.when(
                  loading: () => const LinearProgressIndicator(),
                  error: (error, _) => Text('매장 목록 로드 실패: ${error.toString()}'),
                  data: (stores) => DropdownButtonFormField<String>(
                    initialValue: _selectedStoreId,
                    decoration: const InputDecoration(
                      labelText: '매장',
                      border: OutlineInputBorder(),
                      prefixIcon: Icon(Icons.store),
                    ),
                    items: stores.map((store) {
                      return DropdownMenuItem(
                        value: store.id,
                        child: Text(store.name),
                      );
                    }).toList(),
                    onChanged: (value) {
                      setState(() {
                        _selectedStoreId = value;
                        _selectedEmployeeId = null; // Reset employee when store changes
                      });
                    },
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return '매장을 선택하세요';
                      }
                      return null;
                    },
                  ),
                ),
                const SizedBox(height: 16),
                // 근로자 선택
                employeesAsync.when(
                  loading: () => const LinearProgressIndicator(),
                  error: (error, _) => Text('근로자 목록 로드 실패: ${error.toString()}'),
                  data: (employees) {
                    if (employees.isEmpty && _selectedStoreId != null) {
                      return const Text('해당 매장에 근로자가 없습니다');
                    }
                    return DropdownButtonFormField<String>(
                      initialValue: _selectedEmployeeId,
                      decoration: const InputDecoration(
                        labelText: '근로자',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.person),
                      ),
                      items: employees.where((e) => e.isActive).map((employee) {
                        return DropdownMenuItem(
                          value: employee.id,
                          child: Text(employee.name),
                        );
                      }).toList(),
                      onChanged: _selectedStoreId == null
                          ? null
                          : (value) {
                              setState(() => _selectedEmployeeId = value);
                            },
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return '근로자를 선택하세요';
                        }
                        return null;
                      },
                    );
                  },
                ),
                const SizedBox(height: 16),
                // 날짜 선택
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.calendar_today),
                  title: Text(
                    _selectedDate != null
                        ? '${_selectedDate!.year}-${_selectedDate!.month.toString().padLeft(2, '0')}-${_selectedDate!.day.toString().padLeft(2, '0')}'
                        : '날짜 선택',
                  ),
                  trailing: const Icon(Icons.arrow_drop_down),
                  onTap: () async {
                    final date = await showDatePicker(
                      context: context,
                      initialDate: _selectedDate ?? DateTime.now(),
                      firstDate: DateTime(2020),
                      lastDate: DateTime(2030),
                    );
                    if (date != null) {
                      setState(() => _selectedDate = date);
                    }
                  },
                ),
                const SizedBox(height: 16),
                // 시작 시간
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.access_time),
                  title: Text(
                    _startTime != null ? '시작: ${_formatTime(_startTime!)}' : '시작 시간 선택',
                  ),
                  trailing: const Icon(Icons.arrow_drop_down),
                  onTap: () async {
                    final time = await showTimePicker(
                      context: context,
                      initialTime: _startTime ?? const TimeOfDay(hour: 9, minute: 0),
                    );
                    if (time != null) {
                      setState(() => _startTime = time);
                    }
                  },
                ),
                const SizedBox(height: 16),
                // 종료 시간
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.access_time_filled),
                  title: Text(
                    _endTime != null ? '종료: ${_formatTime(_endTime!)}' : '종료 시간 선택',
                  ),
                  trailing: const Icon(Icons.arrow_drop_down),
                  onTap: () async {
                    final time = await showTimePicker(
                      context: context,
                      initialTime: _endTime ?? const TimeOfDay(hour: 18, minute: 0),
                    );
                    if (time != null) {
                      setState(() => _endTime = time);
                    }
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
              : Text(isEditMode ? '수정' : '등록'),
        ),
      ],
    );
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_selectedDate == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('날짜를 선택하세요')),
      );
      return;
    }

    if (_startTime == null || _endTime == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('시작 시간과 종료 시간을 선택하세요')),
      );
      return;
    }

    // Validate end time is after start time
    final startMinutes = _startTime!.hour * 60 + _startTime!.minute;
    final endMinutes = _endTime!.hour * 60 + _endTime!.minute;
    if (endMinutes <= startMinutes) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('종료 시간은 시작 시간보다 늦어야 합니다')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      if (isEditMode) {
        await ref.read(scheduleNotifierProvider.notifier).updateSchedule(
              id: widget.scheduleId!,
              employeeId: _selectedEmployeeId!,
              storeId: _selectedStoreId!,
              workDate: _selectedDate!,
              startTime: _formatTime(_startTime!),
              endTime: _formatTime(_endTime!),
            );
      } else {
        await ref.read(scheduleNotifierProvider.notifier).createSchedule(
              employeeId: _selectedEmployeeId!,
              storeId: _selectedStoreId!,
              workDate: _selectedDate!,
              startTime: _formatTime(_startTime!),
              endTime: _formatTime(_endTime!),
            );
      }

      if (mounted) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isEditMode ? '일정이 수정되었습니다' : '일정이 등록되었습니다'),
          ),
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
