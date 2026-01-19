@echo off
chcp 65001 >nul

REM Flutter 빌드 스크립트
REM 사용법: build.bat [dev|staging|production] [web|apk|appbundle|ios|all]

setlocal enabledelayedexpansion

set ENV=%1
set TARGET=%2

if "%ENV%"=="" set ENV=dev
if "%TARGET%"=="" set TARGET=web

echo =============================================
echo   LMS Flutter 빌드 스크립트
echo   환경: %ENV%
echo   대상: %TARGET%
echo =============================================
echo.

REM 환경 파일 복사
if "%ENV%"=="dev" (
    echo [1/4] 개발 환경 설정 적용 중...
    copy /Y .env.development .env >nul
) else if "%ENV%"=="staging" (
    echo [1/4] 스테이징 환경 설정 적용 중...
    copy /Y .env.staging .env >nul
) else if "%ENV%"=="production" (
    echo [1/4] 프로덕션 환경 설정 적용 중...
    copy /Y .env.production .env >nul
) else (
    echo 오류: 알 수 없는 환경입니다. dev, staging, production 중 선택하세요.
    exit /b 1
)

REM 의존성 설치
echo [2/4] 의존성 설치 중...
call flutter pub get
if errorlevel 1 (
    echo 오류: 의존성 설치 실패
    exit /b 1
)

REM 코드 분석
echo [3/4] 코드 분석 중...
call flutter analyze --no-fatal-infos
if errorlevel 1 (
    echo 경고: 코드 분석에서 이슈가 발견되었습니다.
)

REM 빌드 실행
echo [4/4] 빌드 실행 중...

if "%TARGET%"=="web" (
    call flutter build web --release --web-renderer canvaskit
    if errorlevel 1 exit /b 1
    echo.
    echo ✅ Web 빌드 완료: build/web/
) else if "%TARGET%"=="apk" (
    call flutter build apk --release
    if errorlevel 1 exit /b 1
    echo.
    echo ✅ APK 빌드 완료: build/app/outputs/flutter-apk/app-release.apk
) else if "%TARGET%"=="appbundle" (
    call flutter build appbundle --release
    if errorlevel 1 exit /b 1
    echo.
    echo ✅ App Bundle 빌드 완료: build/app/outputs/bundle/release/app-release.aab
) else if "%TARGET%"=="ios" (
    call flutter build ios --release --no-codesign
    if errorlevel 1 exit /b 1
    echo.
    echo ✅ iOS 빌드 완료: build/ios/iphoneos/
) else if "%TARGET%"=="all" (
    call flutter build web --release --web-renderer canvaskit
    call flutter build apk --release
    call flutter build appbundle --release
    echo.
    echo ✅ 모든 빌드 완료
) else (
    echo 오류: 알 수 없는 빌드 대상입니다. web, apk, appbundle, ios, all 중 선택하세요.
    exit /b 1
)

echo.
echo =============================================
echo   빌드 완료!
echo =============================================

endlocal
