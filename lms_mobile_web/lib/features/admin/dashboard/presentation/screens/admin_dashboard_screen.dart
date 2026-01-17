import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/auth/presentation/providers/admin_auth_provider.dart';

class AdminDashboardScreen extends ConsumerWidget {
  const AdminDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(adminAuthProvider);
    final user = authState.user;

    return Scaffold(
      appBar: AppBar(
        title: const Text('관리자 대시보드'),
        actions: [
          if (user != null)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(user.email, style: const TextStyle(fontSize: 12)),
                    Text(
                      user.role == 'SUPER_ADMIN' ? '슈퍼 관리자' : '매니저',
                      style: const TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await ref.read(adminAuthProvider.notifier).logout();
              if (context.mounted) {
                context.go(RouteNames.adminLogin);
              }
            },
            tooltip: '로그아웃',
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
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

            // 관리 메뉴 (추후 구현)
            Text(
              '관리 메뉴',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: GridView.count(
                crossAxisCount: 4,
                mainAxisSpacing: 16,
                crossAxisSpacing: 16,
                children: [
                  _buildMenuCard(
                    context,
                    icon: Icons.store,
                    title: '매장 관리',
                    color: Colors.blue,
                    onTap: () {
                      // TODO: Navigate to store management
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('매장 관리 기능은 준비 중입니다')),
                      );
                    },
                  ),
                  _buildMenuCard(
                    context,
                    icon: Icons.people,
                    title: '직원 관리',
                    color: Colors.green,
                    onTap: () {
                      // TODO: Navigate to employee management
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('직원 관리 기능은 준비 중입니다')),
                      );
                    },
                  ),
                  _buildMenuCard(
                    context,
                    icon: Icons.calendar_today,
                    title: '근무 일정',
                    color: Colors.orange,
                    onTap: () {
                      // TODO: Navigate to schedule management
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('근무 일정 기능은 준비 중입니다')),
                      );
                    },
                  ),
                  _buildMenuCard(
                    context,
                    icon: Icons.access_time,
                    title: '근태 관리',
                    color: Colors.purple,
                    onTap: () {
                      // TODO: Navigate to attendance management
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('근태 관리 기능은 준비 중입니다')),
                      );
                    },
                  ),
                  _buildMenuCard(
                    context,
                    icon: Icons.beach_access,
                    title: '휴가 승인',
                    color: Colors.teal,
                    onTap: () {
                      // TODO: Navigate to leave approval
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('휴가 승인 기능은 준비 중입니다')),
                      );
                    },
                  ),
                  _buildMenuCard(
                    context,
                    icon: Icons.attach_money,
                    title: '급여 관리',
                    color: Colors.indigo,
                    onTap: () {
                      // TODO: Navigate to payroll management
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('급여 관리 기능은 준비 중입니다')),
                      );
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMenuCard(
    BuildContext context, {
    required IconData icon,
    required String title,
    required Color color,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 4,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 64, color: color),
            const SizedBox(height: 12),
            Text(
              title,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
