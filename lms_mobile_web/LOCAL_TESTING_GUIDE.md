# 로컬 UI 테스트 가이드

## 🎯 빠른 시작

### 방법 1: 인터랙티브 스크립트 (가장 쉬움)

**Windows:**
```cmd
cd lms_mobile_web
local_ui_test.bat
```

**Mac/Linux:**
```bash
cd lms_mobile_web
chmod +x local_ui_test.sh
./local_ui_test.sh
```

메뉴에서 원하는 테스트 모드를 선택하세요:
- **1) Watch 모드** - 파일 변경 시 자동 재실행 ⭐ 추천!
- **2) 한번만 실행** - 전체 테스트 한 번만 실행
- **3) 레이아웃 오버플로우** - UI 레이아웃 문제만 체크
- **4) Golden 테스트** - UI 스냅샷 비교

### 방법 2: VSCode 통합 (개발하면서 테스트)

1. **VSCode에서 Command Palette 열기**
   - Windows/Linux: `Ctrl + Shift + P`
   - Mac: `Cmd + Shift + P`

2. **"Tasks: Run Task" 선택**

3. **원하는 테스트 선택:**
   - `Flutter: Watch Tests` ⭐ 실시간 테스트
   - `Flutter: Run All Tests` - 전체 테스트
   - `Flutter: Check Layout Overflow` - 레이아웃 체크
   - `Flutter: Update Golden Files` - UI 스냅샷 업데이트
   - `Flutter: Run Integration Tests` - 통합 테스트
   - `Flutter: Analyze Code` - 코드 분석

### 방법 3: 커맨드라인 직접 실행

```bash
# 전체 테스트
flutter test

# Watch 모드 (파일 변경 시 자동 실행)
flutter test --watch

# 특정 파일만 테스트
flutter test test/golden_test.dart

# 레이아웃 오버플로우 체크
flutter test --reporter=json | grep "RenderFlex overflowed"

# Golden 파일 업데이트
flutter test --update-goldens
```

---

## 📱 실시간 UI 테스트 (Watch 모드)

가장 추천하는 방법입니다!

### 시작하기

```bash
flutter test --watch
```

또는

```bash
./local_ui_test.sh  # 그 다음 '1' 선택
```

### 작동 방식

1. ✅ 테스트가 한 번 실행됨
2. 👀 파일 변경을 감시 시작
3. ✏️ 코드를 수정하고 저장
4. 🔄 자동으로 테스트 재실행
5. ✅ 즉시 결과 확인

### 예시 워크플로우

```bash
# 1. Watch 모드 시작
flutter test --watch

# 2. 다른 터미널에서 코드 수정
# admin_dashboard_screen.dart 파일을 열어서 패딩 변경

# 3. 파일 저장 (Ctrl+S)

# 4. 자동으로 테스트 실행되고 결과 표시!
```

---

## 🖼️ Golden 테스트 (UI 스냅샷)

### UI 변경 감지하기

```bash
# 1. 현재 UI와 저장된 스냅샷 비교
flutter test test/golden_test.dart
```

**결과:**
- ✅ 통과 → UI 변경 없음
- ❌ 실패 → UI가 변경됨 (의도적? 버그?)

### UI 변경 승인하기

변경이 의도적이라면:

```bash
# Golden 파일 업데이트
flutter test --update-goldens test/golden_test.dart

# 변경사항 확인
git diff test/goldens/
```

### 실제 사용 예시

**시나리오: 대시보드 카드 크기 변경**

```bash
# 1. 변경 전 스냅샷 생성
flutter test --update-goldens test/golden_test.dart

# 2. 코드 수정 (패딩 변경 등)
# admin_dashboard_screen.dart 수정

# 3. 변경 감지
flutter test test/golden_test.dart
# → ❌ 실패 (예상된 동작)

# 4. 변경사항 시각적으로 확인
# test/goldens/ 폴더의 이미지 파일 확인

# 5. 문제 없으면 승인
flutter test --update-goldens test/golden_test.dart
git add test/goldens/
git commit -m "fix: update dashboard card padding"
```

---

## 📐 레이아웃 오버플로우 자동 감지

### 방법 1: 인터랙티브 스크립트

```bash
./local_ui_test.sh  # '3' 선택
```

### 방법 2: 직접 명령어

```bash
flutter test --reporter=json > test_results.json
grep "RenderFlex overflowed" test_results.json
```

### 자동 실행 (저장 시마다)

VSCode에서 `settings.json`에 추가:

```json
{
  "dart.testOnSave": true,
  "dart.testAdditionalArgs": [
    "--reporter=expanded"
  ]
}
```

이제 파일 저장 시마다 자동으로 테스트 실행!

---

## 🔍 특정 화면만 테스트

### 단일 테스트 파일

```bash
# Golden 테스트만
flutter test test/golden_test.dart

# 통합 테스트만
flutter test integration_test/admin_screens_test.dart
```

### 테스트 이름으로 필터링

```bash
# "Dashboard"가 포함된 테스트만
flutter test --name="Dashboard"

# "overflow"가 포함된 테스트만
flutter test --name="overflow"
```

---

## 🚀 개발 워크플로우 예시

### 시나리오 1: 새 기능 개발

```bash
# 1. Watch 모드 시작 (한 번만)
flutter test --watch

# 2. VSCode에서 새 기능 개발
# 3. 파일 저장 시마다 자동 테스트
# 4. 에러 발생 시 즉시 수정
# 5. 모든 테스트 통과될 때까지 반복
```

### 시나리오 2: UI 버그 수정

```bash
# 1. 레이아웃 오버플로우 발견
./local_ui_test.sh
# → '3' 선택 (레이아웃 체크)

# 2. 오버플로우 위치 확인
# 에러 메시지에서 파일 위치 확인

# 3. 수정 (예: padding 줄이기)

# 4. 재테스트
./local_ui_test.sh
# → '3' 선택

# 5. ✅ 통과!
```

### 시나리오 3: 리팩토링

```bash
# 1. Golden 스냅샷 생성 (변경 전)
flutter test --update-goldens

# 2. 리팩토링 수행

# 3. UI 변경 여부 확인
flutter test test/golden_test.dart

# 4. 변경 없으면 ✅ 성공!
#    변경 있으면 의도한 것인지 확인
```

---

## 💡 VSCode 단축키 설정

`keybindings.json`에 추가:

```json
[
  {
    "key": "ctrl+shift+t",
    "command": "workbench.action.tasks.runTask",
    "args": "Flutter: Watch Tests"
  },
  {
    "key": "ctrl+shift+g",
    "command": "workbench.action.tasks.runTask",
    "args": "Flutter: Update Golden Files"
  }
]
```

이제:
- `Ctrl+Shift+T` → Watch 모드 시작
- `Ctrl+Shift+G` → Golden 파일 업데이트

---

## 🐛 문제 해결

### 테스트가 느릴 때

```bash
# 병렬 실행
flutter test --concurrency=4

# 특정 테스트만
flutter test test/golden_test.dart
```

### Watch 모드가 파일 변경을 감지 못할 때

```bash
# Flutter 툴 재시작
flutter clean
flutter pub get
flutter test --watch
```

### Golden 테스트가 계속 실패할 때

```bash
# 캐시 삭제 후 재생성
rm -rf test/goldens/
flutter test --update-goldens
```

---

## 📊 테스트 커버리지 확인

```bash
# 커버리지 리포트 생성
flutter test --coverage

# HTML 리포트로 변환
genhtml coverage/lcov.info -o coverage/html

# 브라우저로 열기
open coverage/html/index.html  # Mac
start coverage/html/index.html # Windows
```

---

## ⚡ 추천 워크플로우

**개발 중:**
```bash
# 터미널 1: Watch 모드
flutter test --watch

# 터미널 2: 앱 실행
flutter run -d web-server --web-port 3000

# VSCode: 코드 편집
# → 저장 시마다 자동 테스트!
```

**커밋 전:**
```bash
# 전체 테스트 + 레이아웃 체크
./test_runner.sh

# 또는
flutter test
flutter analyze
```

**UI 변경 후:**
```bash
# Golden 테스트로 확인
flutter test test/golden_test.dart

# 변경 승인
flutter test --update-goldens
```

---

## 🎬 다음 단계

1. ✅ Watch 모드로 개발 시작
2. ✅ Golden 테스트로 UI 보호
3. ✅ 커밋 전 전체 테스트
4. ✅ GitHub Actions가 CI에서 자동 검증

**질문이나 문제가 있으면 [TESTING_GUIDE.md](./TESTING_GUIDE.md)를 참고하세요!**
