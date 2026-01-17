# Flutter UI 테스트 및 자동화 가이드

## 📋 목차

1. [테스트 전략 개요](#테스트-전략-개요)
2. [테스트 실행 방법](#테스트-실행-방법)
3. [CI/CD 자동화](#cicd-자동화)
4. [추가 추천 도구](#추가-추천-도구)

---

## 테스트 전략 개요

### 1. Unit Tests (단위 테스트)
**목적**: 개별 함수/클래스 로직 검증

```bash
flutter test test/unit/
```

### 2. Widget Tests (위젯 테스트)
**목적**: UI 컴포넌트 렌더링 및 상호작용 검증

```bash
flutter test test/widget/
```

### 3. Integration Tests (통합 테스트)
**목적**: 전체 앱 워크플로우 검증

```bash
flutter test integration_test/
```

### 4. Golden Tests (시각적 회귀 테스트)
**목적**: UI 변경사항 자동 감지

```bash
# Golden 파일 생성/업데이트
flutter test --update-goldens

# Golden 파일과 비교
flutter test test/golden_test.dart
```

---

## 테스트 실행 방법

### 로컬에서 실행

#### 1. 전체 테스트 실행
```bash
cd lms_mobile_web

# 모든 테스트 실행
flutter test

# 특정 테스트 파일만 실행
flutter test test/golden_test.dart

# 통합 테스트 실행
flutter test integration_test/admin_screens_test.dart
```

#### 2. 레이아웃 오버플로우 체크
```bash
# 테스트 실행 후 오버플로우 에러 확인
flutter test --reporter=json | grep "RenderFlex overflowed"

# 에러가 발견되면 종료 코드 1 반환
flutter test 2>&1 | tee test_output.txt
grep -q "RenderFlex overflowed" test_output.txt && exit 1 || exit 0
```

#### 3. 성능 프로파일링
```bash
# 프로파일 모드로 빌드
flutter build web --profile

# DevTools로 성능 분석
flutter pub global run devtools
```

### CI/CD에서 실행

GitHub Actions가 자동으로 다음을 수행합니다:
- ✅ 코드 분석 (flutter analyze)
- ✅ 단위 테스트 실행
- ✅ 통합 테스트 실행
- ✅ 레이아웃 오버플로우 체크
- ✅ Golden 파일 변경 감지
- ✅ 웹 빌드 성공 여부 확인

---

## CI/CD 자동화

### GitHub Actions 워크플로우

`.github/workflows/flutter_test.yml` 파일이 다음을 자동화합니다:

#### 1. 자동 테스트 (Push/PR 시)
```yaml
# master 브랜치에 push하거나 PR을 만들 때 자동 실행
on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]
```

#### 2. 레이아웃 오버플로우 자동 감지
- 테스트 실행 중 "RenderFlex overflowed" 에러가 발견되면 빌드 실패
- PR에 자동으로 코멘트 추가

#### 3. Golden 파일 변경 감지
- UI가 변경되면 자동으로 감지
- 변경된 파일 목록을 artifact로 업로드

---

## 추가 추천 도구

### 1. Playwright (E2E 테스트)
웹 버전의 완전한 E2E 테스트

```bash
npm install -D @playwright/test

# Playwright 테스트 실행
npx playwright test
```

**예제 설정**:
```javascript
// playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  use: {
    baseURL: 'http://localhost:3000',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: {
    command: 'cd lms_mobile_web && flutter run -d web-server --web-port 3000',
    port: 3000,
    timeout: 120000,
  },
});
```

```javascript
// e2e/admin_flow.spec.ts
import { test, expect } from '@playwright/test';

test('admin can navigate all screens without errors', async ({ page }) => {
  await page.goto('/');

  // 로그인
  await page.fill('input[type="email"]', 'admin@test.com');
  await page.fill('input[type="password"]', 'password123');
  await page.click('button:has-text("로그인")');

  // 대시보드 확인
  await expect(page.locator('text=대시보드')).toBeVisible();

  // 각 관리 화면 확인
  const screens = ['매장 관리', '근로자 관리', '일정 관리', '급여 관리'];
  for (const screen of screens) {
    await page.click(`text=${screen}`);
    await page.waitForLoadState('networkidle');

    // 스크린샷 촬영 (시각적 회귀 테스트)
    await page.screenshot({
      path: `screenshots/${screen.replace(' ', '_')}.png`,
      fullPage: true
    });
  }
});

test('check for console errors', async ({ page }) => {
  const errors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
    }
  });

  await page.goto('/');

  // 콘솔 에러가 없는지 확인
  expect(errors).toHaveLength(0);
});
```

### 2. Lighthouse CI (성능 자동화)
웹 성능, 접근성, SEO 자동 측정

```bash
npm install -g @lhci/cli

# Lighthouse 실행
lhci autorun --config=lighthouserc.json
```

**설정 예제**:
```json
// lighthouserc.json
{
  "ci": {
    "collect": {
      "url": ["http://localhost:3000"],
      "numberOfRuns": 3
    },
    "assert": {
      "assertions": {
        "categories:performance": ["error", {"minScore": 0.9}],
        "categories:accessibility": ["error", {"minScore": 0.9}],
        "first-contentful-paint": ["error", {"maxNumericValue": 2000}],
        "interactive": ["error", {"maxNumericValue": 3000}]
      }
    }
  }
}
```

### 3. Percy (시각적 회귀 테스트 - 클라우드)
자동으로 스크린샷을 비교하고 UI 변경사항을 감지

```bash
npm install --save-dev @percy/cli @percy/playwright

# Percy 테스트 실행
npx percy exec -- playwright test
```

### 4. Sentry (에러 모니터링)
프로덕션 환경의 실시간 에러 추적

```yaml
# pubspec.yaml
dependencies:
  sentry_flutter: ^7.0.0
```

```dart
// main.dart
import 'package:sentry_flutter/sentry_flutter.dart';

Future<void> main() async {
  await SentryFlutter.init(
    (options) {
      options.dsn = 'YOUR_SENTRY_DSN';
      options.tracesSampleRate = 1.0;
    },
    appRunner: () => runApp(const MyApp()),
  );
}
```

---

## 자동화된 테스트 실행 시나리오

### 시나리오 1: 로컬 개발
```bash
# 1. 코드 변경 후 저장
# 2. 자동으로 hot reload
# 3. watch 모드로 테스트 실행
flutter test --watch

# 4. Golden 파일 변경사항 확인
flutter test --update-goldens test/golden_test.dart
git diff test/goldens/
```

### 시나리오 2: PR 생성 시
1. GitHub Actions가 자동으로 트리거
2. 모든 테스트 실행
3. 레이아웃 오버플로우 체크
4. Golden 파일 변경 감지
5. 결과를 PR에 코멘트로 추가
6. 실패 시 merge 차단

### 시나리오 3: 프로덕션 배포
```bash
# 1. 테스트 통과 확인
flutter test

# 2. 웹 빌드
flutter build web --release

# 3. Lighthouse CI로 성능 체크
lhci autorun

# 4. 배포
firebase deploy  # 또는 다른 호스팅 서비스
```

---

## 커스텀 테스트 헬퍼

### 오버플로우 감지 헬퍼
```dart
// test/helpers/overflow_detector.dart
class OverflowDetector {
  static Future<bool> hasOverflowErrors(WidgetTester tester) async {
    final errors = <FlutterErrorDetails>[];

    FlutterError.onError = (details) {
      if (details.toString().contains('RenderFlex overflowed')) {
        errors.add(details);
      }
    };

    await tester.pumpAndSettle();
    return errors.isNotEmpty;
  }
}
```

### API 모킹 헬퍼
```dart
// test/helpers/api_mock.dart
import 'package:mockito/mockito.dart';
import 'package:dio/dio.dart';

class MockDio extends Mock implements Dio {}

Dio createMockDio({
  Map<String, dynamic>? mockResponse,
  int statusCode = 200,
}) {
  final dio = MockDio();

  when(dio.get(any)).thenAnswer((_) async => Response(
    data: mockResponse ?? {},
    statusCode: statusCode,
    requestOptions: RequestOptions(path: ''),
  ));

  return dio;
}
```

---

## 문제 해결

### 테스트가 느릴 때
```bash
# 병렬 실행
flutter test --concurrency=4

# 특정 테스트만 실행
flutter test --name="Dashboard"
```

### Golden 테스트 실패
```bash
# Golden 파일 재생성
flutter test --update-goldens

# 차이점 확인
git diff test/goldens/
```

### 메모리 부족
```bash
# 힙 크기 증가
flutter test --dart-define=FLUTTER_TEST_MAX_HEAP=4096
```

---

## 결론

이 가이드를 따라 다음을 자동화할 수 있습니다:
- ✅ 코드 푸시 시 자동 테스트
- ✅ UI 변경 자동 감지
- ✅ 레이아웃 오버플로우 자동 체크
- ✅ 성능 회귀 방지
- ✅ 크로스 브라우저 테스트

**다음 단계**: PR에 자동 코멘트를 추가하는 GitHub Action 설정
