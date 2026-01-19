import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/schedule/domain/models/schedule_state.dart';
import 'package:lms_mobile_web/features/schedule/domain/models/work_schedule.dart';
import 'package:lms_mobile_web/features/schedule/presentation/providers/schedule_provider.dart';
import 'package:table_calendar/table_calendar.dart';

class MyScheduleScreen extends ConsumerStatefulWidget {
  const MyScheduleScreen({super.key});

  @override
  ConsumerState<MyScheduleScreen> createState() => _MyScheduleScreenState();
}

class _MyScheduleScreenState extends ConsumerState<MyScheduleScreen> {
  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;
  CalendarFormat _calendarFormat = CalendarFormat.month;

  @override
  void initState() {
    super.initState();
    _selectedDay = _focusedDay;
    _loadSchedules();
  }

  void _loadSchedules() {
    final startDate = DateTime(_focusedDay.year, _focusedDay.month - 1, 1);
    final endDate = DateTime(_focusedDay.year, _focusedDay.month + 2, 0);

    ref.read(scheduleProvider.notifier).loadSchedules(
          startDate: startDate,
          endDate: endDate,
        );
  }

  @override
  Widget build(BuildContext context) {
    final scheduleState = ref.watch(scheduleProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('내 근무 일정'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadSchedules,
            tooltip: '새로고침',
          ),
        ],
      ),
      body: scheduleState.isLoading
          ? const Center(child: CircularProgressIndicator())
          : Column(
              children: [
                TableCalendar<WorkSchedule>(
                  firstDay: DateTime.utc(2020, 1, 1),
                  lastDay: DateTime.utc(2030, 12, 31),
                  focusedDay: _focusedDay,
                  calendarFormat: _calendarFormat,
                  locale: 'ko_KR',
                  selectedDayPredicate: (day) => isSameDay(_selectedDay, day),
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
                    _focusedDay = focusedDay;
                    _loadSchedules();
                  },
                  eventLoader: (day) {
                    return scheduleState.getSchedulesForDate(day);
                  },
                  calendarStyle: CalendarStyle(
                    markerDecoration: BoxDecoration(
                      color: theme.colorScheme.primary,
                      shape: BoxShape.circle,
                    ),
                    todayDecoration: BoxDecoration(
                      color: theme.colorScheme.primary.withValues(alpha: 0.3),
                      shape: BoxShape.circle,
                    ),
                    selectedDecoration: BoxDecoration(
                      color: theme.colorScheme.primary,
                      shape: BoxShape.circle,
                    ),
                  ),
                  headerStyle: HeaderStyle(
                    formatButtonVisible: true,
                    titleCentered: true,
                    formatButtonShowsNext: false,
                    formatButtonDecoration: BoxDecoration(
                      border: Border.all(color: theme.colorScheme.primary),
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                ),
                const Divider(),
                Expanded(
                  child: _buildScheduleList(scheduleState, theme),
                ),
              ],
            ),
    );
  }

  Widget _buildScheduleList(ScheduleState scheduleState, ThemeData theme) {
    if (_selectedDay == null) {
      return const Center(child: Text('날짜를 선택하세요'));
    }

    final daySchedules = scheduleState.getSchedulesForDate(_selectedDay!);
    final dateFormat = DateFormat('yyyy년 MM월 dd일 EEEE', 'ko_KR');

    if (daySchedules.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.event_busy, size: 64, color: Colors.grey.shade400),
            const SizedBox(height: 16),
            Text(
              dateFormat.format(_selectedDay!),
              style: theme.textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(
              '근무 일정이 없습니다',
              style: TextStyle(color: Colors.grey.shade600),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: daySchedules.length + 1,
      itemBuilder: (context, index) {
        if (index == 0) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: Text(
              dateFormat.format(_selectedDay!),
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
          );
        }

        final schedule = daySchedules[index - 1];
        return _buildScheduleCard(schedule, theme);
      },
    );
  }

  Widget _buildScheduleCard(WorkSchedule schedule, ThemeData theme) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.schedule,
                  color: schedule.isConfirmed ? Colors.green : Colors.orange,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${schedule.startTime} - ${schedule.endTime}',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '근무 시간: ${schedule.workHours.toStringAsFixed(1)}시간',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: Colors.grey.shade600,
                        ),
                      ),
                    ],
                  ),
                ),
                _buildStatusChip(schedule),
              ],
            ),
            if (schedule.storeName != null) ...[
              const SizedBox(height: 12),
              Row(
                children: [
                  Icon(Icons.store, size: 16, color: Colors.grey.shade600),
                  const SizedBox(width: 8),
                  Text(
                    schedule.storeName!,
                    style: TextStyle(color: Colors.grey.shade600),
                  ),
                ],
              ),
            ],
            if (schedule.isWeekendWork) ...[
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.purple.shade50,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  '주말 근무',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.purple.shade700,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildStatusChip(WorkSchedule schedule) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: schedule.isConfirmed
            ? Colors.green.shade50
            : Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        schedule.isConfirmed ? '확정' : '미확정',
        style: TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          color: schedule.isConfirmed
              ? Colors.green.shade700
              : Colors.orange.shade700,
        ),
      ),
    );
  }
}
