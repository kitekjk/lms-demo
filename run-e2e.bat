@echo off
chcp 65001 >nul
echo === Starting E2E environment ===

start "E2E MySQL" cmd /k "docker-compose -f docker-compose.e2e.yml up"
timeout /t 15

start "E2E Backend" cmd /k "gradlew.bat :interfaces:bootRun --args=--spring.profiles.active=e2e"
timeout /t 25

start "React Dev" cmd /k "cd lms_web && npm run dev"
timeout /t 5

echo.
echo === Services starting. Wait ~30s for backend health. ===
echo Run tests from lms_web/:  npm run test:e2e
