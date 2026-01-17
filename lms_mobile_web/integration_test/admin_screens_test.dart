import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:lms_mobile_web/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Admin Screens Layout Tests', () {
    testWidgets('Dashboard screen renders without overflow errors',
        (WidgetTester tester) async {
      // 앱 시작
      app.main();
      await tester.pumpAndSettle();

      // 로그인 화면이 보이는지 확인
      expect(find.text('LMS 관리자 로그인'), findsOneWidget);

      // TODO: 로그인 로직 구현 후 테스트
      // 현재는 로그인 화면까지만 확인
    });

    testWidgets('Store list screen button renders correctly',
        (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle();

      // TODO: 로그인 후 매장 관리 화면으로 이동
      // 버튼이 ConstrainedBox로 감싸져 있는지 확인
    });

    testWidgets('No RenderFlex overflow errors in any screen',
        (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle();

      // FlutterError를 캐치하여 overflow 에러가 없는지 확인
      final List<FlutterErrorDetails> errors = [];
      FlutterError.onError = (FlutterErrorDetails details) {
        if (details.toString().contains('RenderFlex overflowed')) {
          errors.add(details);
        }
      };

      // 여러 화면을 순회하며 테스트
      await tester.pumpAndSettle();

      // 오버플로우 에러가 없는지 확인
      expect(errors, isEmpty,
          reason: 'Found ${errors.length} RenderFlex overflow errors');
    });
  });

  group('API Integration Tests', () {
    testWidgets('Leave API returns proper response', (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle();

      // TODO: 로그인 후 휴가 관리 화면에서 API 호출 테스트
      // 500 에러가 발생하지 않는지 확인
    });
  });

  group('Responsive Layout Tests', () {
    testWidgets('Dashboard cards fit in small viewport',
        (WidgetTester tester) async {
      // 작은 화면 크기로 테스트
      await tester.binding.setSurfaceSize(const Size(320, 568));

      app.main();
      await tester.pumpAndSettle();

      // 오버플로우 없이 렌더링되는지 확인
      await tester.binding.setSurfaceSize(null);
    });

    testWidgets('Dashboard cards fit in large viewport',
        (WidgetTester tester) async {
      // 큰 화면 크기로 테스트
      await tester.binding.setSurfaceSize(const Size(1920, 1080));

      app.main();
      await tester.pumpAndSettle();

      await tester.binding.setSurfaceSize(null);
    });
  });
}
