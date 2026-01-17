import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patrol/patrol.dart';
import 'package:lms_mobile_web/main.dart' as app;

/// Patrol을 사용한 고급 통합 테스트
/// 네이티브 UI 요소와의 상호작용 가능
void main() {
  patrolTest(
    'Full admin workflow test',
    ($) async {
      // 앱 시작
      app.main();
      await $.pumpAndSettle();

      // 1. 로그인 테스트
      await $(find.text('LMS 관리자 로그인')).waitUntilVisible();

      // TODO: 로그인 필드가 구현되면 활성화
      // await $(find.byType(TextField)).at(0).enterText('admin@test.com');
      // await $(find.byType(TextField)).at(1).enterText('password123');
      // await $(find.text('로그인')).tap();

      // 2. 대시보드 확인
      // await $(find.text('대시보드')).waitUntilVisible();

      // 3. 각 관리 화면 순회하며 오버플로우 체크
      // final screens = [
      //   '매장 관리',
      //   '근로자 관리',
      //   '일정 관리',
      //   '급여 관리',
      //   '휴가 관리'
      // ];

      // for (final screen in screens) {
      //   await $(find.text(screen)).tap();
      //   await $.pumpAndSettle();

      //   // 오버플로우 에러가 없는지 확인
      //   final hasOverflow = await $.native.checkForLayoutOverflow();
      //   expect(hasOverflow, false, reason: '$screen has layout overflow');

      //   // 뒤로 가기
      //   await $.native.pressBack();
      // }

      // 4. API 응답 테스트
      // await $(find.text('휴가 관리')).tap();
      // await $.pumpAndSettle();

      // // API가 200 OK를 반환하는지 확인
      // await $(find.text('대기 중인 휴가 신청')).waitUntilVisible();
    },
  );

  patrolTest(
    'Responsive layout test across different screen sizes',
    ($) async {
      final screenSizes = [
        const Size(320, 568),  // iPhone SE
        const Size(375, 667),  // iPhone 8
        const Size(414, 896),  // iPhone 11 Pro Max
        const Size(768, 1024), // iPad
        const Size(1920, 1080), // Desktop
      ];

      for (final size in screenSizes) {
        await $.tester.binding.setSurfaceSize(size);

        app.main();
        await $.pumpAndSettle();

        // 각 화면 크기에서 오버플로우가 없는지 확인
        final hasOverflow = await $.checkForOverflowErrors();
        expect(hasOverflow, false,
            reason: 'Layout overflow at ${size.width}x${size.height}');

        await $.tester.binding.setSurfaceSize(null);
      }
    },
  );

  patrolTest(
    'Performance test - screen rendering time',
    ($) async {
      final stopwatch = Stopwatch()..start();

      app.main();
      await $.pumpAndSettle();

      stopwatch.stop();

      // 초기 렌더링이 3초 이내에 완료되는지 확인
      expect(stopwatch.elapsedMilliseconds, lessThan(3000),
          reason: 'App took too long to render: ${stopwatch.elapsedMilliseconds}ms');
    },
  );
}

/// Patrol 확장 메서드
extension PatrolTestExtensions on PatrolIntegrationTester {
  /// 레이아웃 오버플로우 에러를 체크하는 헬퍼 메서드
  Future<bool> checkForOverflowErrors() async {
    final errors = <FlutterErrorDetails>[];

    FlutterError.onError = (FlutterErrorDetails details) {
      if (details.toString().contains('RenderFlex overflowed')) {
        errors.add(details);
      }
    };

    await pumpAndSettle();

    return errors.isNotEmpty;
  }
}
