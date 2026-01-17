# UI 테스트 자동화 - 빠른 시작 가이드

## 🎯 개요

이 프로젝트는 Flutter 웹 앱의 UI 버그를 자동으로 감지하고 테스트하는 여러 방법을 제공합니다.

## ⚡ 빠른 시작

### 1. 로컬에서 테스트 실행

**Windows:**
```cmd
cd lms_mobile_web
test_runner.bat
```

**Mac/Linux:**
```bash
cd lms_mobile_web
chmod +x test_runner.sh
./test_runner.sh
```

### 2. 특정 테스트만 실행

```bash
# 코드 분석만
./test_runner.sh analyze

# 단위 테스트만
./test_runner.sh unit

# Golden 테스트만
./test_runner.sh golden

# 통합 테스트만
./test_runner.sh integration
```

## 📦 설정된 자동화 도구

### ✅ 이미 설정된 것들

1. **Flutter Integration Tests** (`integration_test/`)
   - 전체 앱 워크플로우 테스트
   - 레이아웃 오버플로우 자동 감지
   - 다양한 화면 크기에서 테스트

2. **Golden Tests** (`test/golden_test.dart`)
   - UI 스냅샷 저장 및 비교
   - 시각적 변경사항 자동 감지

3. **GitHub Actions** (`.github/workflows/flutter_test.yml`)
   - PR/Push 시 자동 실행
   - 레이아웃 에러 자동 체크
   - 빌드 성공 여부 확인

4. **테스트 스크립트** (`test_runner.sh`, `test_runner.bat`)
   - 원클릭 테스트 실행
   - 자동 에러 리포팅

## 🚀 추가 설정 가능한 도구

### Playwright (E2E 테스트)

**설치:**
```bash
npm install -D @playwright/test
```

**테스트 작성:**
```javascript
// e2e/admin_flow.spec.ts
test('admin workflow', async ({ page }) => {
  await page.goto('http://localhost:3000');
  await page.click('text=매장 관리');
  await expect(page).toHaveURL(/.*stores/);
});
```

**실행:**
```bash
npx playwright test
npx playwright test --ui  # UI 모드로 실행
```

### Lighthouse CI (성능 모니터링)

**설치:**
```bash
npm install -g @lhci/cli
```

**실행:**
```bash
# 개발 서버 시작 후
lhci autorun --config=lighthouserc.json
```

### Percy (시각적 회귀 테스트)

**설정:**
```bash
npm install --save-dev @percy/cli @percy/playwright
```

**실행:**
```bash
export PERCY_TOKEN=your_token
npx percy exec -- playwright test
```

## 🔄 CI/CD 워크플로우

GitHub Actions가 자동으로:

1. **코드 푸시 시**
   - ✅ 코드 분석
   - ✅ 모든 테스트 실행
   - ✅ 레이아웃 오버플로우 체크

2. **PR 생성 시**
   - ✅ Golden 파일 변경 감지
   - ✅ 테스트 결과를 PR에 코멘트
   - ✅ 실패 시 merge 차단

3. **배포 전**
   - ✅ 웹 빌드 성공 확인
   - ✅ 성능 메트릭 체크

## 📊 테스트 결과 확인

### 로컬
```bash
flutter test --reporter=expanded
```

### GitHub Actions
1. Repository → Actions 탭
2. 최근 워크플로우 실행 확인
3. 실패한 테스트 로그 확인

## 🐛 발견된 버그 자동 리포팅

### 현재 감지 가능한 항목:

- ✅ RenderFlex overflow errors
- ✅ API 500 errors
- ✅ Layout constraint violations
- ✅ Widget tree errors
- ✅ Performance regressions
- ✅ Visual changes

## 💡 사용 예시

### 시나리오 1: 새 기능 개발

```bash
# 1. 기능 개발
# 2. 로컬 테스트
./test_runner.sh

# 3. Golden 파일 업데이트 (UI 변경 시)
flutter test --update-goldens

# 4. 커밋 & 푸시
git add .
git commit -m "feat: 새 기능 추가"
git push

# 5. GitHub Actions가 자동으로 테스트
# 6. PR 리뷰 & 머지
```

### 시나리오 2: 버그 수정

```bash
# 1. 버그 재현 테스트 작성
# integration_test/bug_fix_test.dart

# 2. 테스트 실행 (실패 확인)
flutter test integration_test/bug_fix_test.dart

# 3. 버그 수정

# 4. 테스트 재실행 (통과 확인)
flutter test integration_test/bug_fix_test.dart

# 5. 전체 테스트
./test_runner.sh

# 6. 커밋 & 푸시
```

## 🔧 문제 해결

### Golden 테스트 실패
```bash
# Golden 파일 재생성
flutter test --update-goldens

# 변경사항 확인
git diff test/goldens/
```

### 느린 테스트
```bash
# 병렬 실행
flutter test --concurrency=4

# 특정 테스트만
flutter test --name="Dashboard"
```

### 테스트 디버깅
```bash
# verbose 모드
flutter test --verbose

# 특정 테스트만 실행
flutter test test/specific_test.dart
```

## 📚 더 알아보기

- [전체 테스트 가이드](./TESTING_GUIDE.md)
- [Flutter 테스트 공식 문서](https://docs.flutter.dev/testing)
- [GitHub Actions 문서](https://docs.github.com/en/actions)

## 🤝 기여하기

새로운 테스트를 추가할 때:

1. `test/` 또는 `integration_test/` 에 파일 추가
2. 테스트 실행하여 통과 확인
3. `test_runner.sh` 에 필요시 추가
4. PR 생성

---

**문의사항**: Issues 탭에서 질문해주세요!
