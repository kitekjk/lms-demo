import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/auth/presentation/providers/admin_auth_provider.dart';

class AdminSidebar extends ConsumerWidget {
  const AdminSidebar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentLocation = GoRouterState.of(context).uri.toString();
    final theme = Theme.of(context);

    return Drawer(
      child: Column(
        children: [
          // Drawer Header
          DrawerHeader(
            decoration: BoxDecoration(color: theme.colorScheme.primary),
            child: const Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.admin_panel_settings, size: 48, color: Colors.white),
                SizedBox(height: 12),
                Text(
                  'LMS 관리자',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),

          // Menu Items
          Expanded(
            child: ListView(
              padding: EdgeInsets.zero,
              children: [
                _buildMenuItem(
                  context,
                  icon: Icons.dashboard,
                  title: '대시보드',
                  route: RouteNames.adminDashboard,
                  isSelected: currentLocation == RouteNames.adminDashboard,
                ),
                _buildMenuItem(
                  context,
                  icon: Icons.store,
                  title: '매장 관리',
                  route: RouteNames.adminStores,
                  isSelected: currentLocation == RouteNames.adminStores,
                ),
                _buildMenuItem(
                  context,
                  icon: Icons.people,
                  title: '근로자 관리',
                  route: RouteNames.adminEmployees,
                  isSelected: currentLocation == RouteNames.adminEmployees,
                ),
                _buildMenuItem(
                  context,
                  icon: Icons.calendar_today,
                  title: '근무 일정',
                  route: RouteNames.adminSchedules,
                  isSelected: currentLocation == RouteNames.adminSchedules,
                ),
                _buildMenuItem(
                  context,
                  icon: Icons.access_time,
                  title: '출퇴근 기록',
                  route: RouteNames.adminAttendance,
                  isSelected: currentLocation == RouteNames.adminAttendance,
                ),
                _buildMenuItem(
                  context,
                  icon: Icons.beach_access,
                  title: '휴가 관리',
                  route: RouteNames.adminLeaves,
                  isSelected: currentLocation == RouteNames.adminLeaves,
                ),
                _buildMenuItem(
                  context,
                  icon: Icons.attach_money,
                  title: '급여 관리',
                  route: RouteNames.adminPayroll,
                  isSelected: currentLocation == RouteNames.adminPayroll,
                ),
                const Divider(),
                ListTile(
                  leading: const Icon(Icons.logout),
                  title: const Text('로그아웃'),
                  onTap: () async {
                    await ref.read(adminAuthProvider.notifier).logout();
                    if (context.mounted) {
                      context.go(RouteNames.adminLogin);
                    }
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMenuItem(
    BuildContext context, {
    required IconData icon,
    required String title,
    required String route,
    required bool isSelected,
  }) {
    final theme = Theme.of(context);

    return ListTile(
      leading: Icon(icon, color: isSelected ? theme.colorScheme.primary : null),
      title: Text(
        title,
        style: TextStyle(
          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
          color: isSelected ? theme.colorScheme.primary : null,
        ),
      ),
      selected: isSelected,
      selectedTileColor: theme.colorScheme.primary.withOpacity(0.1),
      onTap: () {
        context.go(route);
        // Close drawer on mobile
        if (Scaffold.of(context).hasDrawer) {
          Navigator.pop(context);
        }
      },
    );
  }
}
