import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:lms_mobile_web/features/admin/dashboard/presentation/screens/admin_dashboard_screen.dart';

/// Golden 테스트: UI 스냅샷을 저장하고 변경사항을 자동으로 감지
///
/// 실행 방법:
/// flutter test --update-goldens  # Golden 파일 업데이트
/// flutter test                   # Golden 파일과 비교
void main() {
  testWidgets('Dashboard screen golden test', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(
          home: AdminDashboardScreen(),
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Golden 이미지와 비교
    await expectLater(
      find.byType(AdminDashboardScreen),
      matchesGoldenFile('goldens/dashboard_screen.png'),
    );
  });

  testWidgets('Dashboard stat card golden test - small viewport',
      (WidgetTester tester) async {
    // 작은 화면 크기로 테스트
    await tester.binding.setSurfaceSize(const Size(375, 667));

    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(
          home: AdminDashboardScreen(),
        ),
      ),
    );

    await tester.pumpAndSettle();

    await expectLater(
      find.byType(AdminDashboardScreen),
      matchesGoldenFile('goldens/dashboard_screen_small.png'),
    );

    await tester.binding.setSurfaceSize(null);
  });

  testWidgets('Dashboard stat card golden test - large viewport',
      (WidgetTester tester) async {
    // 큰 화면 크기로 테스트
    await tester.binding.setSurfaceSize(const Size(1920, 1080));

    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(
          home: AdminDashboardScreen(),
        ),
      ),
    );

    await tester.pumpAndSettle();

    await expectLater(
      find.byType(AdminDashboardScreen),
      matchesGoldenFile('goldens/dashboard_screen_large.png'),
    );

    await tester.binding.setSurfaceSize(null);
  });
}
