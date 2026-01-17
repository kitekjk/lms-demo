import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/features/admin/auth/presentation/providers/admin_auth_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class AdminDashboardScreen extends ConsumerWidget {
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(adminAuthProvider);
    final user = authState.user;

    return AdminLayout(
      title: '대시보드',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // 환영 메시지
          Card(
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
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
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // 통계 요약 (추후 구현)
          Text(
            '주요 지표',
            style: Theme.of(
              context,
            ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),

          // 통계 카드 그리드
          Expanded(
            child: GridView.count(
              crossAxisCount: 4,
              mainAxisSpacing: 16,
              crossAxisSpacing: 16,
              childAspectRatio: 1.5,
              children: [
                _buildStatCard(
                  context,
                  icon: Icons.store,
                  title: '총 매장',
                  value: '-',
                  color: Colors.blue,
                ),
                _buildStatCard(
                  context,
                  icon: Icons.people,
                  title: '총 직원',
                  value: '-',
                  color: Colors.green,
                ),
                _buildStatCard(
                  context,
                  icon: Icons.access_time,
                  title: '금일 출근',
                  value: '-',
                  color: Colors.orange,
                ),
                _buildStatCard(
                  context,
                  icon: Icons.beach_access,
                  title: '휴가 대기',
                  value: '-',
                  color: Colors.purple,
                ),
              ],
            ),
          ),
        ],
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
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, size: 32, color: color),
                const Spacer(),
                Text(
                  value,
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: color,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              title,
              style: const TextStyle(fontSize: 14, color: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }
}
