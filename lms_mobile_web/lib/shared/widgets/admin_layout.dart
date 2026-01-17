import 'package:flutter/material.dart';
import 'package:lms_mobile_web/shared/widgets/admin_app_bar.dart';
import 'package:lms_mobile_web/shared/widgets/admin_sidebar.dart';

class AdminLayout extends StatelessWidget {
  final String title;
  final Widget child;

  const AdminLayout({super.key, required this.title, required this.child});

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isMobile = constraints.maxWidth < 768;

        return Scaffold(
          appBar: AdminAppBar(title: title),
          drawer: isMobile ? const AdminSidebar() : null,
          body: Row(
            children: [
              // Sidebar (desktop only)
              if (!isMobile) const SizedBox(width: 250, child: AdminSidebar()),
              // Main content
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(24.0),
                  child: child,
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
