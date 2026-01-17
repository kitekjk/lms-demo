@echo off
chcp 65001 >nul
REM 로컬 UI 테스트 - 실시간 모니터링 (Windows)
setlocal enabledelayedexpansion

cls
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo      🎯 Flutter UI 테스트 - 로컬 모니터링 모드
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

echo 테스트 모드를 선택하세요:
echo   1^) Watch 모드 - 파일 변경 시 자동 재실행
echo   2^) 한번만 실행
echo   3^) 레이아웃 오버플로우만 체크
echo   4^) Golden 테스트 (UI 스냅샷 비교)
echo.

set /p mode="선택 (1-4): "

if "%mode%"=="1" goto watch_mode
if "%mode%"=="2" goto run_once
if "%mode%"=="3" goto overflow_check
if "%mode%"=="4" goto golden_test
goto invalid_choice

:watch_mode
echo.
echo 📡 Watch 모드 시작...
echo 파일이 변경되면 자동으로 테스트가 재실행됩니다.
echo 종료하려면 Ctrl+C를 누르세요.
echo.
call flutter test --watch
goto end

:run_once
echo.
echo 🧪 테스트 실행 중...
echo.
call flutter test
if errorlevel 1 (
    echo.
    echo ❌ 테스트 실패
    exit /b 1
) else (
    echo.
    echo ✅ 모든 테스트 통과!
)
goto end

:overflow_check
echo.
echo 📐 레이아웃 오버플로우 체크 중...
echo.

call flutter test --reporter=json > test_results.json 2>&1

findstr /C:"RenderFlex overflowed" test_results.json >nul
if !errorlevel! equ 0 (
    echo ❌ 레이아웃 오버플로우 발견!
    echo.
    echo 발견된 오버플로우:
    findstr "RenderFlex overflowed" test_results.json
    del test_results.json
    exit /b 1
) else (
    echo ✅ 레이아웃 오버플로우 없음
    del test_results.json
)
goto end

:golden_test
echo.
echo 🖼️ Golden 테스트 옵션:
echo   1^) Golden 파일과 비교 (변경사항 감지)
echo   2^) Golden 파일 업데이트 (새 스냅샷 생성)
echo.

set /p golden_mode="선택 (1-2): "

if "%golden_mode%"=="1" (
    echo.
    echo 🔍 Golden 테스트 실행 중...
    echo.
    call flutter test test\golden_test.dart
    if errorlevel 1 (
        echo.
        echo ⚠️ UI 변경 감지됨!
        echo 변경사항을 승인하려면:
        echo   local_ui_test.bat 선택 4 -^> 선택 2
    ) else (
        echo.
        echo ✅ UI 변경사항 없음
    )
) else if "%golden_mode%"=="2" (
    echo.
    echo 📸 Golden 파일 업데이트 중...
    echo.
    call flutter test --update-goldens test\golden_test.dart
    echo.
    echo ✅ Golden 파일 업데이트 완료
    echo 변경사항:
    git diff test\goldens\
) else (
    echo 잘못된 선택입니다.
    exit /b 1
)
goto end

:invalid_choice
echo 잘못된 선택입니다.
exit /b 1

:end
echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
pause
