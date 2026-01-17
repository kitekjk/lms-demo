import 'package:flutter/material.dart';
import 'package:lms_mobile_web/features/admin/dashboard/domain/models/dashboard_stats.dart';

class AttendanceSummaryWidget extends StatelessWidget {
  final AttendanceSummary summary;

  const AttendanceSummaryWidget({super.key, required this.summary});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '금일 출근 현황',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),
            _buildAttendanceRow(
              context,
              label: '정상 출근',
              count: summary.normal,
              color: Colors.green,
              icon: Icons.check_circle,
            ),
            const SizedBox(height: 12),
            _buildAttendanceRow(
              context,
              label: '지각',
              count: summary.late,
              color: Colors.orange,
              icon: Icons.access_time,
            ),
            const SizedBox(height: 12),
            _buildAttendanceRow(
              context,
              label: '조퇴',
              count: summary.earlyLeave,
              color: Colors.blue,
              icon: Icons.exit_to_app,
            ),
            const SizedBox(height: 12),
            _buildAttendanceRow(
              context,
              label: '결근',
              count: summary.absent,
              color: Colors.red,
              icon: Icons.cancel,
            ),
            const Divider(height: 32),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '총계',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  '${summary.total}명',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAttendanceRow(
    BuildContext context, {
    required String label,
    required int count,
    required Color color,
    required IconData icon,
  }) {
    return Row(
      children: [
        Icon(icon, color: color, size: 20),
        const SizedBox(width: 12),
        Expanded(child: Text(label, style: const TextStyle(fontSize: 16))),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(
            '$count명',
            style: TextStyle(color: color, fontWeight: FontWeight.bold),
          ),
        ),
      ],
    );
  }
}
