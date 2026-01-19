import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/schedule/presentation/providers/schedule_provider.dart';
import 'package:lms_mobile_web/features/admin/schedule/presentation/widgets/schedule_form_dialog.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';
import 'package:table_calendar/table_calendar.dart';

class ScheduleCalendarScreen extends ConsumerStatefulWidget {
  const ScheduleCalendarScreen({super.key});

  @override
  ConsumerState<ScheduleCalendarScreen> createState() => _ScheduleCalendarScreenState();
}

class _ScheduleCalendarScreenState extends ConsumerState<ScheduleCalendarScreen> {
  DateTime _focusedDay = DateTime.now();
  DateTime _selectedDay = DateTime.now();
  CalendarFormat _calendarFormat = CalendarFormat.month;
  String? _selectedStoreId;

  @override
  Widget build(BuildContext context) {
    // Get schedules for the selected month
    final startOfMonth = DateTime(_focusedDay.year, _focusedDay.month, 1);
    final endOfMonth = DateTime(_focusedDay.year, _focusedDay.month + 1, 0);
    final storesAsync = ref.watch(storesProvider);

    // Only fetch schedules if a store is selected
    AsyncValue<List<dynamic>>? schedulesAsync;
    if (_selectedStoreId != null) {
      final filter = ScheduleFilter(
        storeId: _selectedStoreId,
        startDate: startOfMonth,
        endDate: endOfMonth,
      );
      schedulesAsync = ref.watch(schedulesProvider(filter));
    }

    return AdminLayout(
      title: '근무 일정 관리',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '근무 일정',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              Row(
                children: [
                  // Store filter
                  storesAsync.when(
                    loading: () => const SizedBox(width: 200, child: LinearProgressIndicator()),
                    error: (_, __) => const SizedBox.shrink(),
                    data: (stores) => SizedBox(
                      width: 200,
                      child: DropdownButtonFormField<String?>(
                        key: ValueKey(_selectedStoreId),
                        value: _selectedStoreId,
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
                          setState(() {
                            _selectedStoreId = value;
                          });
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 16),
                  ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 200),
                    child: ElevatedButton.icon(
                      onPressed: () => _showAddScheduleDialog(context),
                      icon: const Icon(Icons.add),
                      label: const Text('일정 추가'),
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 24),
          Expanded(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Calendar
                Expanded(
                  flex: 2,
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: schedulesAsync == null
                          ? Center(
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Icon(Icons.store_outlined, size: 64, color: Colors.grey.shade400),
                                  const SizedBox(height: 16),
                                  Text(
                                    '매장을 선택하면 일정을 조회할 수 있습니다',
                                    style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                                  ),
                                ],
                              ),
                            )
                          : schedulesAsync.when(
                              loading: () => const Center(child: CircularProgressIndicator()),
                              error: (error, _) => Center(child: Text('오류: ${error.toString()}')),
                              data: (schedules) {
                                return TableCalendar(
                                  firstDay: DateTime.utc(2020, 1, 1),
                                  lastDay: DateTime.utc(2030, 12, 31),
                                  focusedDay: _focusedDay,
                                  selectedDayPredicate: (day) => isSameDay(_selectedDay, day),
                                  calendarFormat: _calendarFormat,
                                  onDaySelected: (selectedDay, focusedDay) {
                                    setState(() {
                                      _selectedDay = selectedDay;
                                      _focusedDay = focusedDay;
                                    });
                                  },
                                  onFormatChanged: (format) {
                                    setState(() {
                                      _calendarFormat = format;
                                    });
                                  },
                                  onPageChanged: (focusedDay) {
                                    setState(() {
                                      _focusedDay = focusedDay;
                                    });
                                  },
                                  eventLoader: (day) {
                                    return schedules
                                        .where((schedule) => isSameDay(schedule.workDate, day))
                                        .toList();
                                  },
                                  calendarStyle: CalendarStyle(
                                    markersMaxCount: 3,
                                    markerDecoration: BoxDecoration(
                                      color: Theme.of(context).colorScheme.primary,
                                      shape: BoxShape.circle,
                                    ),
                                  ),
                                  headerStyle: const HeaderStyle(
                                    formatButtonVisible: true,
                                    titleCentered: true,
                                  ),
                                );
                              },
                            ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                // Schedule list for selected day
                Expanded(
                  flex: 1,
                  child: _buildScheduleList(),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildScheduleList() {
    // Only fetch schedules if a store is selected
    AsyncValue<List<dynamic>>? schedulesAsync;
    if (_selectedStoreId != null) {
      final filter = ScheduleFilter(
        storeId: _selectedStoreId,
        startDate: _selectedDay,
        endDate: _selectedDay,
      );
      schedulesAsync = ref.watch(schedulesProvider(filter));
    }
    final dateFormat = DateFormat('yyyy년 MM월 dd일 (E)', 'ko_KR');

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              dateFormat.format(_selectedDay),
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: schedulesAsync == null
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.store_outlined, size: 64, color: Colors.grey.shade400),
                          const SizedBox(height: 16),
                          Text(
                            '매장을 선택하면 일정을 조회할 수 있습니다',
                            style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                          ),
                        ],
                      ),
                    )
                  : schedulesAsync.when(
                      loading: () => const Center(child: CircularProgressIndicator()),
                      error: (error, _) => Center(child: Text('오류: ${error.toString()}')),
                      data: (schedules) {
                        if (schedules.isEmpty) {
                          return Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const Icon(Icons.calendar_today_outlined, size: 48, color: Colors.grey),
                                const SizedBox(height: 16),
                                const Text('일정이 없습니다'),
                                const SizedBox(height: 16),
                                ElevatedButton.icon(
                                  onPressed: () => _showAddScheduleDialog(context),
                                  icon: const Icon(Icons.add),
                                  label: const Text('일정 추가'),
                                ),
                              ],
                            ),
                          );
                        }

                        return ListView.separated(
                    itemCount: schedules.length,
                    separatorBuilder: (context, index) => const Divider(),
                    itemBuilder: (context, index) {
                      final schedule = schedules[index];
                      return ListTile(
                        leading: CircleAvatar(
                          backgroundColor: schedule.isWeekendWork
                              ? Colors.orange.withOpacity(0.1)
                              : Colors.blue.withOpacity(0.1),
                          child: Icon(
                            schedule.isWeekendWork ? Icons.weekend : Icons.work,
                            color: schedule.isWeekendWork ? Colors.orange : Colors.blue,
                            size: 20,
                          ),
                        ),
                        title: Text(
                          schedule.employeeName ?? schedule.employeeId,
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        ),
                        subtitle: Text(
                          '${schedule.startTime} - ${schedule.endTime} (${schedule.workHours}시간)',
                        ),
                        trailing: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            if (schedule.isConfirmed)
                              Chip(
                                label: const Text('확정', style: TextStyle(fontSize: 12)),
                                backgroundColor: Colors.green.shade100,
                                labelStyle: TextStyle(color: Colors.green.shade900),
                              ),
                            const SizedBox(width: 8),
                            IconButton(
                              icon: const Icon(Icons.edit, size: 20),
                              tooltip: '수정',
                              onPressed: () => _showEditScheduleDialog(context, schedule.id),
                            ),
                            IconButton(
                              icon: const Icon(Icons.delete, size: 20, color: Colors.red),
                              tooltip: '삭제',
                              onPressed: () => _confirmDelete(context, schedule.id),
                            ),
                          ],
                        ),
                        );
                      },
                    );
                  },
                ),
            ),
          ],
        ),
      ),
    );
  }

  void _showAddScheduleDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => ScheduleFormDialog(
        selectedDate: _selectedDay,
      ),
    );
  }

  void _showEditScheduleDialog(BuildContext context, String scheduleId) {
    showDialog(
      context: context,
      builder: (context) => ScheduleFormDialog(
        scheduleId: scheduleId,
        selectedDate: _selectedDay,
      ),
    );
  }

  void _confirmDelete(BuildContext context, String scheduleId) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('일정 삭제'),
        content: const Text('정말 이 일정을 삭제하시겠습니까?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.of(context).pop();
              await ref.read(scheduleNotifierProvider.notifier).deleteSchedule(scheduleId);
              if (mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('일정이 삭제되었습니다')),
                );
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('삭제'),
          ),
        ],
      ),
    );
  }
}
