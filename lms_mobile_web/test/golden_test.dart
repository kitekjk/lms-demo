@Tags(['golden'])
library;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:lms_mobile_web/features/admin/dashboard/domain/models/dashboard_stats.dart';
import 'package:lms_mobile_web/features/admin/dashboard/presentation/widgets/attendance_summary_widget.dart';
import 'package:lms_mobile_web/features/admin/dashboard/presentation/widgets/recent_activities_widget.dart';

/// Golden 테스트: UI 스냅샷을 저장하고 변경사항을 자동으로 감지
///
/// 실행 방법:
/// flutter test --update-goldens  # Golden 파일 업데이트
/// flutter test                   # Golden 파일과 비교
///
/// 참고: Golden 테스트는 로컬 환경에서만 실행됩니다.
/// CI에서는 폰트 렌더링 차이로 인해 제외됩니다.
void main() {
  final mockStats = DashboardStats.mock();

  testWidgets('AttendanceSummaryWidget golden test',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AttendanceSummaryWidget(
            summary: mockStats.attendanceSummary,
          ),
        ),
      ),
    );

    await tester.pumpAndSettle();

    await expectLater(
      find.byType(AttendanceSummaryWidget),
      matchesGoldenFile('goldens/attendance_summary_widget.png'),
    );
  });

  testWidgets('RecentActivitiesWidget golden test',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: RecentActivitiesWidget(
            activities: mockStats.recentActivities,
          ),
        ),
      ),
    );

    await tester.pumpAndSettle();

    await expectLater(
      find.byType(RecentActivitiesWidget),
      matchesGoldenFile('goldens/recent_activities_widget.png'),
    );
  });

  testWidgets('AttendanceSummaryWidget golden test - small viewport',
      (WidgetTester tester) async {
    await tester.binding.setSurfaceSize(const Size(375, 667));

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AttendanceSummaryWidget(
            summary: mockStats.attendanceSummary,
          ),
        ),
      ),
    );

    await tester.pumpAndSettle();

    await expectLater(
      find.byType(AttendanceSummaryWidget),
      matchesGoldenFile('goldens/attendance_summary_widget_small.png'),
    );

    await tester.binding.setSurfaceSize(null);
  });
}
