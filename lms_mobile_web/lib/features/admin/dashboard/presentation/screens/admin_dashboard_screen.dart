import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/features/admin/auth/presentation/providers/admin_auth_provider.dart';
import 'package:lms_mobile_web/features/admin/dashboard/domain/models/dashboard_stats.dart';
import 'package:lms_mobile_web/features/admin/dashboard/presentation/widgets/attendance_summary_widget.dart';
import 'package:lms_mobile_web/features/admin/dashboard/presentation/widgets/recent_activities_widget.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class AdminDashboardScreen extends ConsumerWidget {
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(adminAuthProvider);
    final user = authState.user;

    // TODO: Replace with actual API call
    final stats = DashboardStats.mock();

    return AdminLayout(
      title: '대시보드',
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // 환영 메시지
            Card(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Row(
                  children: [
                    Icon(
                      Icons.admin_panel_settings,
                      size: 48,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '환영합니다!',
                            style: Theme.of(context).textTheme.headlineSmall
                                ?.copyWith(fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            '${user?.email ?? ''} (${user?.role == 'SUPER_ADMIN' ? '슈퍼 관리자' : '매니저'})',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                          if (user?.storeName != null) ...[
                            const SizedBox(height: 4),
                            Text(
                              '담당 매장: ${user!.storeName}',
                              style: Theme.of(context).textTheme.bodySmall,
                            ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // 통계 카드
            Text(
              '주요 지표',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),

            GridView.count(
              crossAxisCount: 4,
              mainAxisSpacing: 16,
              crossAxisSpacing: 16,
              childAspectRatio: 1.5,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              children: [
                _buildStatCard(
                  context,
                  icon: Icons.store,
                  title: '총 매장',
                  value: '${stats.totalStores}',
                  color: Colors.blue,
                ),
                _buildStatCard(
                  context,
                  icon: Icons.people,
                  title: '총 직원',
                  value: '${stats.totalEmployees}',
                  color: Colors.green,
                ),
                _buildStatCard(
                  context,
                  icon: Icons.check_circle,
                  title: '금일 출근',
                  value: '${stats.todayAttendance}',
                  color: Colors.orange,
                ),
                _buildStatCard(
                  context,
                  icon: Icons.beach_access,
                  title: '휴가 중',
                  value: '${stats.onLeave}',
                  color: Colors.purple,
                ),
              ],
            ),
            const SizedBox(height: 24),

            // 출근 현황 및 최근 활동
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: AttendanceSummaryWidget(
                    summary: stats.attendanceSummary,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: RecentActivitiesWidget(
                    activities: stats.recentActivities,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatCard(
    BuildContext context, {
    required IconData icon,
    required String title,
    required String value,
    required Color color,
  }) {
    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, size: 28, color: color),
                const Spacer(),
                Flexible(
                  child: Text(
                    value,
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: color,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              title,
              style: const TextStyle(fontSize: 12, color: Colors.grey),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}
