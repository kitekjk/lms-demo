import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/attendance/presentation/providers/attendance_provider.dart';

class CheckInOutScreen extends ConsumerStatefulWidget {
  const CheckInOutScreen({super.key});

  @override
  ConsumerState<CheckInOutScreen> createState() => _CheckInOutScreenState();
}

class _CheckInOutScreenState extends ConsumerState<CheckInOutScreen> {
  final _workScheduleIdController = TextEditingController(text: 'SCHEDULE_001');

  @override
  void dispose() {
    _workScheduleIdController.dispose();
    super.dispose();
  }

  Future<void> _handleCheckIn() async {
    try {
      await ref
          .read(attendanceProvider.notifier)
          .checkIn(_workScheduleIdController.text);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('출근이 완료되었습니다'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString()), backgroundColor: Colors.red),
        );
      }
    }
  }

  Future<void> _handleCheckOut() async {
    try {
      await ref.read(attendanceProvider.notifier).checkOut();

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('퇴근이 완료되었습니다'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString()), backgroundColor: Colors.red),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final attendanceState = ref.watch(attendanceProvider);
    final now = DateTime.now();
    final timeFormat = DateFormat('HH:mm:ss');
    final dateFormat = DateFormat('yyyy년 MM월 dd일 EEEE', 'ko_KR');
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('출퇴근 체크'),
        actions: [
          IconButton(
            icon: const Icon(Icons.history),
            onPressed: () {
              context.push(RouteNames.attendanceRecords);
            },
            tooltip: '출퇴근 기록',
          ),
        ],
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // 현재 날짜 표시
                Text(
                  dateFormat.format(now),
                  style: theme.textTheme.titleLarge,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),

                // 실시간 시계
                StreamBuilder(
                  stream: Stream.periodic(const Duration(seconds: 1)),
                  builder: (context, snapshot) {
                    return Text(
                      timeFormat.format(DateTime.now()),
                      style: theme.textTheme.displayLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                      textAlign: TextAlign.center,
                    );
                  },
                ),
                const SizedBox(height: 48),

                // 오늘의 출퇴근 기록
                if (attendanceState.todayRecord != null) ...[
                  Card(
                    elevation: 4,
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          if (attendanceState.todayRecord!.hasCheckedIn) ...[
                            _buildTimeRow(
                              '출근 시간',
                              timeFormat.format(
                                attendanceState.todayRecord!.checkInTime!,
                              ),
                              Colors.green,
                            ),
                          ],
                          if (attendanceState.todayRecord!.hasCheckedOut) ...[
                            const SizedBox(height: 8),
                            const Divider(),
                            const SizedBox(height: 8),
                            _buildTimeRow(
                              '퇴근 시간',
                              timeFormat.format(
                                attendanceState.todayRecord!.checkOutTime!,
                              ),
                              Colors.red,
                            ),
                            if (attendanceState.todayRecord!.actualWorkHours !=
                                null) ...[
                              const SizedBox(height: 8),
                              const Divider(),
                              const SizedBox(height: 8),
                              _buildTimeRow(
                                '근무 시간',
                                '${attendanceState.todayRecord!.actualWorkHours!.toStringAsFixed(1)}시간',
                                theme.colorScheme.primary,
                              ),
                            ],
                          ],
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                ],

                // Work Schedule ID 입력 (개발용 - 실제 환경에서는 자동으로 가져와야 함)
                if (attendanceState.todayRecord == null ||
                    !attendanceState.todayRecord!.hasCheckedIn) ...[
                  TextFormField(
                    controller: _workScheduleIdController,
                    decoration: const InputDecoration(
                      labelText: 'Work Schedule ID',
                      hintText: '근무 일정 ID를 입력하세요',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 16),
                ],

                // 출근 버튼
                if (attendanceState.todayRecord == null ||
                    !attendanceState.todayRecord!.hasCheckedIn)
                  ElevatedButton(
                    onPressed: attendanceState.isLoading
                        ? null
                        : _handleCheckIn,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 20),
                      backgroundColor: Colors.green,
                      foregroundColor: Colors.white,
                    ),
                    child: attendanceState.isLoading
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Text('출근 체크', style: TextStyle(fontSize: 20)),
                  ),

                // 퇴근 버튼
                if (attendanceState.todayRecord != null &&
                    attendanceState.todayRecord!.hasCheckedIn &&
                    !attendanceState.todayRecord!.hasCheckedOut)
                  ElevatedButton(
                    onPressed: attendanceState.isLoading
                        ? null
                        : _handleCheckOut,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 20),
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                    child: attendanceState.isLoading
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white,
                            ),
                          )
                        : const Text('퇴근 체크', style: TextStyle(fontSize: 20)),
                  ),

                // 완료 메시지
                if (attendanceState.todayRecord != null &&
                    attendanceState.todayRecord!.isComplete)
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.blue.shade50,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.blue.shade200),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.check_circle, color: Colors.blue.shade700),
                        const SizedBox(width: 8),
                        Text(
                          '오늘의 출퇴근이 완료되었습니다',
                          style: TextStyle(
                            color: Colors.blue.shade700,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTimeRow(String label, String time, Color color) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
        ),
        Text(
          time,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ],
    );
  }
}
