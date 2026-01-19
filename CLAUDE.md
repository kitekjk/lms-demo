# Claude Code Instructions

## Task Master AI Instructions
**Import Task Master's development workflow commands and guidelines, treat as if import is in the main CLAUDE.md file.**
@./.taskmaster/CLAUDE.md


## Guideline
@./.aiassistant/rules/guideline.md


## Flutter Development Guidelines

### 모델 선택 가이드
- **Haiku**: 코드 탐색, 테스트 실행, 로그 분석 (저비용)
- **Sonnet**: 일반 개발, UI 구현, 버그 수정 (기본 권장)
- **Opus**: 복잡한 아키텍처, 대규모 리팩토링 (고급)

### 자주 사용하는 명령어
```bash
flutter pub get          # 의존성 설치
flutter analyze          # 코드 분석
flutter test             # 테스트 실행
flutter build web        # 웹 빌드
flutter run -d chrome    # 크롬에서 실행
dart format lib/         # 코드 포맷팅
```

### 커스텀 슬래시 명령어
- `/flutter-test` - 테스트 실행 및 분석
- `/flutter-analyze` - 코드 품질 검사
- `/flutter-build` - 앱 빌드

### 비용 최적화 팁
1. `/clear` - 태스크 간 컨텍스트 초기화
2. `/compact` - 긴 대화 압축
3. `/cost` - 현재 비용 확인
4. `/model haiku` - 간단한 작업 시 모델 전환

### Flutter 프로젝트 구조
```
lms_mobile_web/
├── lib/
│   ├── main.dart              # 앱 진입점
│   ├── app.dart               # 앱 설정
│   ├── core/                  # 핵심 유틸리티
│   │   ├── api/               # API 클라이언트
│   │   ├── config/            # 환경 설정
│   │   ├── router/            # 라우팅
│   │   └── storage/           # 로컬 저장소
│   ├── features/              # 기능별 모듈
│   │   ├── auth/              # 인증
│   │   ├── attendance/        # 출퇴근
│   │   ├── schedule/          # 일정
│   │   ├── leave/             # 휴가
│   │   ├── payroll/           # 급여
│   │   └── admin/             # 관리자
│   └── shared/                # 공유 위젯
├── test/                      # 테스트
└── integration_test/          # 통합 테스트
```

### 코드 작성 규칙
- `const` 생성자 최대한 활용
- 위젯 파일 500줄 이하 유지
- Riverpod으로 상태 관리
- 비즈니스 로직은 Provider에서 처리


## 알려진 이슈 및 해결책

### 1. BoxConstraints 무한 너비 오류 (RenderFlex overflowed)
**증상**: `Row` 내에서 버튼이나 텍스트가 화면을 벗어남
```
A RenderFlex overflowed by X pixels on the right/bottom
```
**해결책**: `Row` 내 버튼은 반드시 `ConstrainedBox`로 감싸기
```dart
// ❌ 잘못된 코드
Row(
  mainAxisAlignment: MainAxisAlignment.spaceBetween,
  children: [
    ElevatedButton(...),  // 무한 너비 문제 발생
  ],
)

// ✅ 올바른 코드
Row(
  mainAxisAlignment: MainAxisAlignment.spaceBetween,
  children: [
    ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 200),
      child: ElevatedButton(...),
    ),
  ],
)
```

### 2. Flutter Web에서 .env 파일 로딩 불가
**증상**: Web 실행 시 FileNotFoundError 발생
**원인**: Flutter Web은 dotenv 파일을 assets에서 로드할 수 없음
**해결책**: `kIsWeb` 체크로 플랫폼별 분기 처리
```dart
import 'package:flutter/foundation.dart' show kIsWeb;

class EnvConfig {
  static String get apiBaseUrl {
    if (kIsWeb) return 'http://localhost:8080/api';  // Web 기본값
    return dotenv.env['API_BASE_URL'] ?? 'http://localhost:8080/api';
  }

  static Future<void> load() async {
    if (kIsWeb) {
      print('Running on web - using default environment values');
      return;
    }
    await dotenv.load();
  }
}
```

### 3. 한국어 날짜 포맷 오류 (LocaleDataException)
**증상**: `Locale data has not been initialized` 에러
**해결책**: `main.dart`에서 초기화 필수
```dart
import 'package:intl/date_symbol_data_local.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('ko_KR');  // 반드시 추가!
  runApp(const ProviderScope(child: MyApp()));
}
```

### 4. Backend JPQL Enum 타입 불일치 (500 Error)
**증상**: API 호출 시 500 에러, `QueryArgumentException: Argument of type String did not match parameter type Enum`
**원인**: JPQL 쿼리에 Enum 대신 String을 전달
**해결책**: Repository에서 Enum 타입 직접 전달
```kotlin
// ❌ 잘못된 코드
@Query("SELECT e FROM Entity e WHERE e.status = :status")
fun findByStatus(@Param("status") status: String)  // String 사용
jpaRepository.findByStatus(Status.PENDING.name)    // .name 호출

// ✅ 올바른 코드
@Query("SELECT e FROM Entity e WHERE e.status = :status")
fun findByStatus(@Param("status") status: Status)  // Enum 타입
jpaRepository.findByStatus(Status.PENDING)         // Enum 직접 전달
```

### 5. API 필수 파라미터 누락 (400 Error)
**증상**: `employeeId 또는 storeId 파라미터가 필요합니다`
**해결책**: API 호출 전 필수 파라미터 확인
```dart
// Provider에서 필수 파라미터 체크
if (_selectedStoreId != null) {
  final filter = ScheduleFilter(storeId: _selectedStoreId);
  schedulesAsync = ref.watch(schedulesProvider(filter));
} else {
  // storeId가 없으면 API 호출하지 않음
  return const Center(child: Text('매장을 선택하세요'));
}
```

### 6. DropdownButtonFormField deprecated 'value' 경고
**증상**: `'value' is deprecated. Use initialValue instead.`
**해결책**: Flutter 3.33+ 버전에서는 `initialValue` 사용
```dart
// ❌ 구버전
DropdownButtonFormField(
  value: _selectedValue,
  ...
)

// ✅ 신버전 (Flutter 3.33+)
DropdownButtonFormField(
  initialValue: _selectedValue,
  ...
)
```

### 7. Windows Batch 파일 한글 깨짐
**증상**: `.bat` 파일 실행 시 한글이 깨져서 출력됨
**해결책**: 파일 시작에 UTF-8 인코딩 설정
```batch
@echo off
chcp 65001 >nul
REM 이제 한글이 정상 출력됩니다
```

