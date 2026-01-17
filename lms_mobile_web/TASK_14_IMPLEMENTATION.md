# Task 14 Implementation: Flutter API 통신 및 인증 시스템 구축

## 완료 내용

### 1. 인증 데이터 모델 구현
다음 파일들이 생성되었습니다:

- `lib/features/auth/data/models/login_request.dart` - 로그인 요청 모델
- `lib/features/auth/data/models/login_response.dart` - 로그인 응답 모델
- `lib/features/auth/data/models/user_info.dart` - 사용자 정보 모델

### 2. 커스텀 예외 처리 시스템
- `lib/core/exception/api_exception.dart` - API 예외 클래스들
  - UnauthorizedException (401)
  - ForbiddenException (403)
  - NotFoundException (404)
  - ConflictException (409)
  - ValidationException (422)
  - ServerException (500)
  - NetworkException (네트워크 오류)

- `lib/core/exception/error_handler.dart` - Dio 에러 핸들러

### 3. 인증 서비스 레이어
- `lib/features/auth/data/services/auth_service.dart` - AuthService 구현
  - `login()` - 로그인 API 호출
  - `logout()` - 로그아웃 API 호출
  - `refreshToken()` - 토큰 갱신 API 호출

### 4. Riverpod 상태 관리
- `lib/features/auth/presentation/providers/auth_provider.dart`
  - `AuthState` - 인증 상태 모델
  - `AuthNotifier` - 인증 상태 관리 로직
  - Providers:
    - `authProvider` - 인증 상태 Provider
    - `authServiceProvider` - AuthService Provider
    - `dioProvider` - Dio Provider
    - `secureStorageProvider` - SecureStorage Provider

### 5. 로그인 화면 UI
- `lib/features/auth/presentation/screens/login_screen.dart`
  - 이메일 입력 필드 (유효성 검증)
  - 비밀번호 입력 필드 (최소 6자, 표시/숨김 토글)
  - 로딩 상태 표시
  - 에러 메시지 표시
  - Material Design 3 테마 적용

### 6. 홈 화면 구현
- `lib/features/home/presentation/screens/home_screen.dart`
  - 로그인 성공 후 표시
  - 사용자 정보 표시
  - 로그아웃 기능

### 7. 라우팅 업데이트
- `lib/core/router/app_router.dart` - 홈 화면 라우트 추가

## 주요 기능

### 인증 플로우
1. 사용자가 로그인 화면에서 이메일/비밀번호 입력
2. 유효성 검증 (이메일 형식, 비밀번호 최소 길이)
3. `AuthService.login()` 호출하여 Spring Boot API 통신
4. 성공 시:
   - Access Token과 Refresh Token을 Secure Storage에 저장
   - 사용자 정보 저장
   - 홈 화면으로 이동
5. 실패 시:
   - 에러 메시지 표시 (SnackBar)

### 자동 토큰 갱신
Task 13에서 구현된 `ApiInterceptor`가 자동으로 처리:
- 모든 API 요청에 Access Token 자동 추가
- 401 응답 시 Refresh Token으로 자동 갱신
- 갱신 성공 시 원래 요청 재시도
- 갱신 실패 시 로그아웃 처리

### 에러 처리
- Dio 예외를 커스텀 예외로 변환
- HTTP 상태 코드별 적절한 예외 발생
- 네트워크 오류, 타임아웃 등 처리
- 사용자에게 명확한 에러 메시지 제공

## 테스트 방법

### 1. Flutter 앱 실행
```bash
cd lms_mobile_web
flutter run -d chrome  # 웹에서 실행
# 또는
flutter run  # Android/iOS에서 실행
```

### 2. Spring Boot 서버 실행
```bash
cd ..
./gradlew bootRun
```

### 3. 로그인 테스트
기존 데이터 로더에서 생성된 테스트 계정 사용:
- 이메일: `admin@lms.com`
- 비밀번호: `password123`

또는 다른 테스트 계정:
- 이메일: `manager@lms.com`
- 비밀번호: `password123`

### 4. 검증 항목
- ✅ 로그인 화면 표시
- ✅ 이메일 유효성 검증
- ✅ 비밀번호 최소 길이 검증
- ✅ 로그인 버튼 클릭 시 API 호출
- ✅ 로딩 인디케이터 표시
- ✅ 성공 시 홈 화면 이동
- ✅ 사용자 정보 표시
- ✅ 로그아웃 기능
- ✅ 잘못된 비밀번호 시 에러 메시지 표시

## 코드 품질
- `flutter analyze` - 0 issues
- Lint 규칙 준수
- Super parameter 사용
- 불필요한 기본값 제거
- Material Design 3 가이드라인 준수

## 다음 단계 (향후 작업)
- [ ] 회원가입 화면 구현
- [ ] 비밀번호 찾기 기능
- [ ] 자동 로그인 (Remember Me)
- [ ] 생체 인증 (지문, 얼굴 인식)
- [ ] 소셜 로그인 (Google, Apple)
- [ ] 단위 테스트 작성
- [ ] 위젯 테스트 작성
- [ ] 통합 테스트 작성
