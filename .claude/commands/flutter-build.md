---
description: Flutter 앱 빌드 (web/android/ios)
---

Flutter 앱을 빌드합니다.

## 수행 단계

1. 의존성 확인 (`flutter pub get`)
2. 코드 분석 (`flutter analyze`)
3. 빌드 실행
4. 빌드 결과 확인

## 빌드 옵션

- **Web**: `flutter build web --release`
- **Android APK**: `flutter build apk --release`
- **Android Bundle**: `flutter build appbundle --release`
- **iOS**: `flutter build ios --release` (macOS 필요)

## 사용 예시

```
/flutter-build web
/flutter-build apk
/flutter-build appbundle
```