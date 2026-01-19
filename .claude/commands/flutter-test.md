---
description: Flutter 테스트 실행 및 결과 분석
---

Flutter 테스트를 실행하고 결과를 분석합니다.

## 수행 단계

1. `flutter test` 명령어 실행
2. 실패한 테스트 식별
3. 실패 원인 분석 및 수정 제안
4. 테스트 커버리지 요약

## 옵션

- `--coverage`: 커버리지 리포트 생성
- `--reporter expanded`: 상세 출력
- 특정 파일: `flutter test test/특정_테스트.dart`

## 사용 예시

```
/flutter-test
/flutter-test --coverage
/flutter-test test/unit/auth_test.dart
```