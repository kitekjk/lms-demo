import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/dashboard/domain/models/dashboard_stats.dart';

class RecentActivitiesWidget extends StatelessWidget {
  final List<RecentActivity> activities;

  const RecentActivitiesWidget({super.key, required this.activities});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '최근 활동',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            if (activities.isEmpty)
              const Center(
                child: Padding(
                  padding: EdgeInsets.all(32.0),
                  child: Text('최근 활동이 없습니다'),
                ),
              )
            else
              ListView.separated(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: activities.length,
                separatorBuilder: (context, index) => const Divider(),
                itemBuilder: (context, index) {
                  final activity = activities[index];
                  return _buildActivityTile(context, activity);
                },
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildActivityTile(BuildContext context, RecentActivity activity) {
    final timeFormat = DateFormat('HH:mm');
    final dateFormat = DateFormat('MM/dd');

    IconData icon;
    Color color;

    switch (activity.type) {
      case 'attendance':
        icon = Icons.access_time;
        color = Colors.green;
        break;
      case 'leave_request':
        icon = Icons.beach_access;
        color = Colors.orange;
        break;
      case 'schedule':
        icon = Icons.calendar_today;
        color = Colors.blue;
        break;
      default:
        icon = Icons.info;
        color = Colors.grey;
    }

    final now = DateTime.now();
    final isToday =
        activity.timestamp.year == now.year &&
        activity.timestamp.month == now.month &&
        activity.timestamp.day == now.day;

    final timeText = isToday
        ? timeFormat.format(activity.timestamp)
        : dateFormat.format(activity.timestamp);

    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: CircleAvatar(
        backgroundColor: color.withOpacity(0.1),
        child: Icon(icon, color: color, size: 20),
      ),
      title: Text(
        activity.employeeName,
        style: const TextStyle(fontWeight: FontWeight.w500),
      ),
      subtitle: Text(activity.action),
      trailing: Text(
        timeText,
        style: TextStyle(fontSize: 12, color: Colors.grey[600]),
      ),
    );
  }
}
