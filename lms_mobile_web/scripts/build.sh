#!/bin/bash

# Flutter 빌드 스크립트
# 사용법: ./build.sh [dev|staging|production] [web|apk|appbundle|ios|all]

set -e

ENV=${1:-dev}
TARGET=${2:-web}

echo "============================================="
echo "  LMS Flutter 빌드 스크립트"
echo "  환경: $ENV"
echo "  대상: $TARGET"
echo "============================================="
echo

# 환경 파일 복사
case $ENV in
    dev)
        echo "[1/4] 개발 환경 설정 적용 중..."
        cp .env.development .env
        ;;
    staging)
        echo "[1/4] 스테이징 환경 설정 적용 중..."
        cp .env.staging .env
        ;;
    production)
        echo "[1/4] 프로덕션 환경 설정 적용 중..."
        cp .env.production .env
        ;;
    *)
        echo "오류: 알 수 없는 환경입니다. dev, staging, production 중 선택하세요."
        exit 1
        ;;
esac

# 의존성 설치
echo "[2/4] 의존성 설치 중..."
flutter pub get

# 코드 분석
echo "[3/4] 코드 분석 중..."
flutter analyze --no-fatal-infos || echo "경고: 코드 분석에서 이슈가 발견되었습니다."

# 빌드 실행
echo "[4/4] 빌드 실행 중..."

case $TARGET in
    web)
        flutter build web --release --web-renderer canvaskit
        echo
        echo "✅ Web 빌드 완료: build/web/"
        ;;
    apk)
        flutter build apk --release
        echo
        echo "✅ APK 빌드 완료: build/app/outputs/flutter-apk/app-release.apk"
        ;;
    appbundle)
        flutter build appbundle --release
        echo
        echo "✅ App Bundle 빌드 완료: build/app/outputs/bundle/release/app-release.aab"
        ;;
    ios)
        flutter build ios --release --no-codesign
        echo
        echo "✅ iOS 빌드 완료: build/ios/iphoneos/"
        ;;
    all)
        flutter build web --release --web-renderer canvaskit
        flutter build apk --release
        flutter build appbundle --release
        echo
        echo "✅ 모든 빌드 완료"
        ;;
    *)
        echo "오류: 알 수 없는 빌드 대상입니다. web, apk, appbundle, ios, all 중 선택하세요."
        exit 1
        ;;
esac

echo
echo "============================================="
echo "  빌드 완료!"
echo "============================================="
