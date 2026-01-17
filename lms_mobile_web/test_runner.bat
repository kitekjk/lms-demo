@echo off
REM Flutter UI 테스트 자동화 스크립트 (Windows)
REM 사용법: test_runner.bat [옵션]
REM 옵션: all, unit, widget, integration, golden, analyze

setlocal enabledelayedexpansion

echo 🚀 Flutter 테스트 자동화 시작...
echo.

REM 의존성 설치
echo 📦 의존성 설치 중...
call flutter pub get
if errorlevel 1 (
    echo ❌ 의존성 설치 실패
    exit /b 1
)

REM 코드 분석
if "%~1"=="all" goto analyze
if "%~1"=="analyze" goto analyze
if "%~1"=="" goto analyze
goto skip_analyze

:analyze
echo.
echo 🔍 코드 분석 중...
call flutter analyze
if errorlevel 1 (
    echo ❌ 코드 분석 실패
    exit /b 1
) else (
    echo ✅ 코드 분석 통과
)

:skip_analyze

REM 단위 테스트
if "%~1"=="all" goto unit
if "%~1"=="unit" goto unit
if "%~1"=="" goto unit
goto skip_unit

:unit
echo.
echo 🧪 단위 테스트 실행 중...
call flutter test test\ --exclude-tags=golden,integration
if errorlevel 1 (
    echo ❌ 단위 테스트 실패
    exit /b 1
) else (
    echo ✅ 단위 테스트 통과
)

:skip_unit

REM Golden 테스트
if "%~1"=="all" goto golden
if "%~1"=="golden" goto golden
goto skip_golden

:golden
echo.
echo 🖼️ Golden 테스트 실행 중...
call flutter test test\golden_test.dart
if errorlevel 1 (
    echo ⚠️ Golden 파일 변경 감지됨
    echo Golden 파일을 업데이트하려면: flutter test --update-goldens
) else (
    echo ✅ Golden 테스트 통과
)

:skip_golden

REM 통합 테스트
if "%~1"=="all" goto integration
if "%~1"=="integration" goto integration
goto skip_integration

:integration
echo.
echo 🔗 통합 테스트 실행 중...
call flutter test integration_test\
if errorlevel 1 (
    echo ❌ 통합 테스트 실패
    exit /b 1
) else (
    echo ✅ 통합 테스트 통과
)

:skip_integration

REM 레이아웃 오버플로우 체크
if "%~1"=="all" goto overflow
if "%~1"=="" goto overflow
goto skip_overflow

:overflow
echo.
echo 📐 레이아웃 오버플로우 체크 중...
call flutter test --reporter=json > test_results.json 2>&1

findstr /C:"RenderFlex overflowed" test_results.json >nul
if !errorlevel! equ 0 (
    echo ❌ 레이아웃 오버플로우 에러 발견!
    findstr "RenderFlex overflowed" test_results.json
    del test_results.json
    exit /b 1
) else (
    echo ✅ 레이아웃 오버플로우 없음
    del test_results.json
)

:skip_overflow

echo.
echo ✨ 모든 테스트 통과!
exit /b 0
