# LMS Demo - Labor Management System

DDD(Domain-Driven Design) 및 Clean Architecture 기반의 근로자 관리 시스템입니다.

## 🎯 프로젝트 목적

이 프로젝트는 **TaskMaster AI**를 활용한 체계적인 소프트웨어 개발 프로세스를 시연하기 위해 만들어졌습니다.

- **AI 기반 태스크 관리**: TaskMaster AI를 통한 체계적인 개발 워크플로우
- **DDD 아키텍처 실습**: 도메인 주도 설계 원칙 적용
- **Clean Architecture**: 계층 분리 및 의존성 역전 원칙 준수

## 🏗️ 아키텍처

### 멀티모듈 구조

```
lms-demo/
├── domain/              # 도메인 레이어 (순수 Kotlin, 외부 의존성 없음)
│   ├── model/          # Aggregate Root, Entity, Value Object
│   ├── service/        # Domain Service
│   ├── common/         # DomainContext 등 공통 요소
│   └── exception/      # 도메인 예외
├── application/         # 애플리케이션 레이어 (UseCase, Service)
├── infrastructure/      # 인프라 레이어 (JPA, Repository 구현체)
│   ├── persistence/    # Entity, Mapper, Repository
│   ├── config/         # JPA, Security 설정
│   └── security/       # JWT, Security 구현
└── interfaces/          # 프레젠테이션 레이어 (Controller, DTO)
    └── web/            # REST API
```

### 핵심 원칙

- **도메인 레이어의 순수성**: 외부 라이브러리 의존성 없음 (순수 Kotlin)
- **의존성 역전**: domain(핵심) ← application ← interfaces/infrastructure(외부)
- **Aggregate Root 중심 설계**: 도메인 로직을 Aggregate 내부에 캡슐화
- **Value Object 활용**: 타입 안정성 및 불변성 보장

## 📦 도메인 모델

### 1. User (사용자 인증)
- **Aggregate Root**: User
- **Value Objects**: UserId, Email, Password
- **Repository**: UserRepository
- **책임**: 로그인, 인증, 권한 관리 (RBAC)

### 2. Employee (근로자)
- **Aggregate Root**: Employee
- **Value Objects**: EmployeeId, EmployeeName, RemainingLeave
- **Repository**: EmployeeRepository
- **책임**: 근로자 정보 관리, 연차 관리

### 3. Store (매장)
- **Aggregate Root**: Store
- **Value Objects**: StoreId, StoreName, StoreLocation
- **Repository**: StoreRepository
- **책임**: 매장 정보 관리

### 4. WorkSchedule (근무 일정)
- **Aggregate Root**: WorkSchedule
- **Value Objects**: WorkScheduleId, WorkDate, WorkTime
- **Repository**: WorkScheduleRepository
- **책임**: 근무 일정 생성, 확정, 변경

### 5. AttendanceRecord (출퇴근 기록)
- **Aggregate Root**: AttendanceRecord
- **Value Objects**: AttendanceRecordId, AttendanceTime
- **Repository**: AttendanceRecordRepository
- **책임**: 출퇴근 체크, 상태 평가 (정상/지각/조퇴/결근)

### 6. LeaveRequest (휴가 신청)
- **Aggregate Root**: LeaveRequest
- **Value Objects**: LeaveRequestId, LeavePeriod
- **Repository**: LeaveRequestRepository
- **책임**: 휴가 신청, 승인/거부, 취소

### 7. Payroll (급여)
- **Aggregate Root**: Payroll
- **Value Objects**: PayrollId, PayrollPeriod, PayrollAmount
- **Repository**: PayrollRepository
- **책임**: 급여 계산, 지급 관리

### 8. PayrollPolicy (급여 정책)
- **Aggregate Root**: PayrollPolicy
- **Value Objects**: PayrollPolicyId, PolicyMultiplier, PolicyEffectivePeriod
- **Repository**: PayrollPolicyRepository
- **책임**: 초과근무/야간근무/휴일근무 배율 관리

## 🤖 TaskMaster AI 개발 워크플로우

이 프로젝트는 TaskMaster AI를 활용하여 체계적으로 개발되었습니다.

### 1. PRD 기반 태스크 생성

```bash
# PRD 문서로부터 자동으로 태스크 생성
task-master parse-prd .taskmaster/docs/prd.md
```

### 2. 복잡도 분석 및 서브태스크 확장

```bash
# 태스크 복잡도 분석
task-master analyze-complexity --research

# 복잡한 태스크를 서브태스크로 세분화
task-master expand --id=2 --research
```

### 3. 태스크 기반 개발

```bash
# 다음 작업할 태스크 확인
task-master next

# 태스크 상세 정보 확인
task-master show 2.1

# 구현 진행 중 노트 추가
task-master update-subtask --id=2.1 --prompt="UserMapper 구현 완료, createdAt 필드 JPA Auditing으로 처리"

# 태스크 완료
task-master set-status --id=2.1 --status=done
```

### 4. 프로젝트 현황 확인

```bash
# 전체 프로젝트 상태
task-master status

# 태스크 목록 조회
task-master list
task-master list --status=done
task-master list --status=pending
```

### TaskMaster AI의 장점

✅ **체계적인 작업 관리**: PRD → 태스크 → 서브태스크로 자동 분해
✅ **진행 상황 추적**: 각 태스크의 상태 및 의존성 관리
✅ **컨텍스트 보존**: 구현 중 발견한 문제점 및 해결 방법 기록
✅ **AI 기반 분석**: 복잡도 분석 및 최적의 서브태스크 수 제안

## 🛠️ 기술 스택

- **Language**: Kotlin 2.1.0
- **Framework**: Spring Boot 3.5.9
- **Build Tool**: Gradle 8.5 with Kotlin DSL
- **Database**: JPA/Hibernate 6.6.39
- **Security**: Spring Security + JWT
- **Architecture**: DDD + Clean Architecture

## 📋 시작하기

### 필요 조건

#### 백엔드 (Spring Boot)
- JDK 17 이상
- Gradle 8.5 이상
- Docker Desktop (MySQL 실행용)

#### 프론트엔드 (Flutter)
- Flutter SDK 3.32.0 이상
- Chrome 브라우저 (웹 개발용)

### Web Clients
- **Flutter** (`lms_mobile_web/`) — mobile-primary channel (iOS/Android + responsive web).
- **React** (`lms_web/`) — desktop-primary web channel. See `lms_web/README.md`.
- **E2E**: Playwright Critical 6 scenarios in `lms_web/e2e/`. See `lms_web/README.md` for run instructions. Requires `docker-compose.e2e.yml` + backend `e2e` profile.

**Flutter SDK 설치:**

```bash
# macOS (Homebrew)
brew install --cask flutter

# Windows (Chocolatey)
choco install flutter

# 또는 공식 사이트에서 직접 다운로드
# https://docs.flutter.dev/get-started/install
```

설치 후 환경 확인:
```bash
flutter doctor
```

### 🚀 Quick Start (5분 안에 시작하기)

처음 프로젝트를 실행하는 경우, 아래 순서대로 진행하세요.

```bash
# 1. 저장소 클론
git clone https://github.com/kitekjk/lms-demo.git
cd lms-demo

# 2. Docker로 MySQL 시작
docker-compose up -d

# 3. MySQL 준비 완료 확인 (약 10-15초 대기)
docker-compose logs -f mysql
# "ready for connections" 메시지 확인 후 Ctrl+C

# 4. 백엔드 서버 실행 (새 터미널)
./gradlew :interfaces:bootRun

# 5. 프론트엔드 실행 (새 터미널)
cd lms_mobile_web
flutter pub get
flutter run -d chrome

# 6. 브라우저에서 확인
# 백엔드 API: http://localhost:8080/swagger-ui.html
# 프론트엔드: http://localhost:xxxxx (Flutter 실행 시 표시됨)
```

**테스트 계정:**
| 역할 | 이메일 | 비밀번호 |
|------|--------|----------|
| 관리자 | admin@lms.com | password123 |
| 매니저(강남점) | manager.gangnam@lms.com | password123 |
| 직원(강남점) | employee1.gangnam@lms.com | password123 |

---

### 📦 상세 설치 가이드

#### Step 1: 데이터베이스 설정 (MySQL)

프로젝트는 Docker Compose를 사용하여 MySQL을 실행합니다.

```bash
# Docker Compose로 MySQL 컨테이너 시작
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# MySQL 로그 확인 (정상 시작 확인)
docker-compose logs mysql
```

**MySQL 접속 정보:**
| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 3306 |
| Database | lms_demo |
| Username | lms |
| Password | lms1234 |

**데이터베이스 직접 접속 (선택사항):**
```bash
# Docker 컨테이너 내부 MySQL 접속
docker exec -it lms-demo-mysql mysql -ulms -plms1234 lms_demo

# 또는 외부 MySQL 클라이언트 사용
mysql -h localhost -P 3306 -ulms -plms1234 lms_demo
```

**Docker 없이 로컬 MySQL 사용 시:**

로컬에 MySQL이 이미 설치되어 있다면 Docker 없이 사용할 수 있습니다.
자세한 설정 방법은 [로컬 MySQL 설정 가이드](./docs/LOCAL_MYSQL_SETUP.md)를 참고하세요.

```sql
-- 빠른 설정 (MySQL CLI에서 실행)
CREATE DATABASE lms_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lms'@'localhost' IDENTIFIED BY 'lms1234';
GRANT ALL PRIVILEGES ON lms_demo.* TO 'lms'@'localhost';
FLUSH PRIVILEGES;
```

#### Step 2: 백엔드 서버 실행

```bash
# 프로젝트 루트 디렉토리에서
cd lms-demo

# 의존성 다운로드 및 빌드
./gradlew build

# 서버 실행 (local 프로파일 - 자동으로 테이블 생성 및 샘플 데이터 로드)
./gradlew :interfaces:bootRun
```

**프로파일별 동작:**
| 프로파일 | DDL 모드 | 초기 데이터 | 용도 |
|----------|----------|-------------|------|
| local (기본) | create-drop | O (data.sql) | 로컬 개발 |
| dev | update | X | 개발 서버 |
| prod | validate | X | 운영 서버 |

**서버 실행 확인:**
```bash
# 서버가 정상 시작되면 아래 URL 접속 가능
# Swagger UI: http://localhost:8080/swagger-ui.html
# API Docs: http://localhost:8080/api-docs
```

**초기 데이터 (자동 생성):**
- 매장 3개 (강남점, 홍대점, 신촌점)
- 사용자 6명 (관리자 1, 매니저 2, 직원 3)
- 근로자 5명
- 급여 정책 4개 (초과근무, 야간, 주말, 공휴일)

#### Step 3: 프론트엔드 실행 (Flutter Web)

```bash
# Flutter 프로젝트 디렉토리로 이동
cd lms_mobile_web

# 의존성 설치
flutter pub get

# 웹 브라우저에서 실행
flutter run -d chrome

# 또는 특정 포트로 실행
flutter run -d chrome --web-port=3000
```

**Flutter 환경 확인:**
```bash
# Flutter 설치 상태 확인
flutter doctor

# 사용 가능한 디바이스 목록
flutter devices
```

**프론트엔드 환경 설정:**

`.env.development` 파일이 기본으로 사용됩니다:
```env
API_BASE_URL=http://localhost:8080/api
ENV=development
DEBUG=true
```

웹 환경에서는 `env_config.dart`의 기본값(`http://localhost:8080/api`)이 사용됩니다.

#### Step 4: 전체 시스템 테스트

1. **로그인 테스트:**
   - Flutter 앱에서 `admin@lms.com` / `password123` 로그인
   - 또는 Swagger UI에서 `/api/auth/login` API 테스트

2. **주요 기능 확인:**
   - 대시보드: 매장별 통계 확인
   - 근로자 관리: 목록 조회, 등록, 수정
   - 근무 일정: 캘린더 뷰, 일정 등록
   - 출퇴근: 체크인/체크아웃

---

### 🔧 개발 환경 설정

#### 환경 변수 설정 (선택사항)

기본값이 설정되어 있어 별도 설정 없이 실행 가능합니다. 커스텀 설정이 필요한 경우:

```bash
# 백엔드 환경 변수
cp .env.example .env
# .env 파일 편집

# 프론트엔드 환경 변수
cd lms_mobile_web
cp .env.example .env.development
# .env.development 파일 편집
```

#### IDE 설정 (IntelliJ IDEA)

1. **프로젝트 열기:** `File > Open > lms-demo 폴더 선택`
2. **JDK 설정:** `File > Project Structure > SDK > JDK 17+`
3. **Gradle 설정:** 자동으로 인식됨
4. **실행 구성:**
   - Main class: `com.lms.LmsApplication`
   - Active profiles: `local`

#### IDE 설정 (VS Code - Flutter)

1. **Flutter 프로젝트 열기:** `lms_mobile_web` 폴더
2. **확장 설치:** Flutter, Dart 확장
3. **실행:** `F5` 또는 `flutter run -d chrome`

---

### 🛑 트러블슈팅

#### MySQL 연결 실패
```bash
# Docker 컨테이너 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose down
docker-compose up -d

# 로그 확인
docker-compose logs mysql
```

#### 포트 충돌
```bash
# 8080 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8080

# 3306 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :3306
```

#### Flutter 빌드 오류
```bash
# 캐시 정리 후 재시도
flutter clean
flutter pub get
flutter run -d chrome
```

#### Gradle 빌드 오류
```bash
# Gradle 캐시 정리
./gradlew clean build --refresh-dependencies
```

---

### 빌드 명령어 모음

#### 백엔드

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 제외하고 빌드
./gradlew build -x test

# 코드 포맷팅
./gradlew spotlessApply

# 애플리케이션 실행
./gradlew :interfaces:bootRun

# JAR 파일 생성
./gradlew :interfaces:bootJar
```

#### 프론트엔드

```bash
# 의존성 설치
flutter pub get

# 코드 분석
flutter analyze

# 테스트 실행
flutter test

# 웹 빌드 (배포용)
flutter build web --release

# 개발 서버 실행
flutter run -d chrome
```

---

### 종료 및 정리

```bash
# 백엔드 서버 종료: Ctrl+C

# Flutter 개발 서버 종료: Ctrl+C 또는 'q' 입력

# MySQL 컨테이너 중지
docker-compose stop

# MySQL 컨테이너 및 볼륨 완전 삭제 (데이터 초기화)
docker-compose down -v
```

**TaskMaster AI 사용 시 추가 설정:**
```bash
# Claude Code MCP 설정
claude mcp add task-master-ai --scope user -- npx task-master-ai

# TaskMaster AI CLI 설정
task-master models --setup
# API 키 입력 (tasks.json은 이미 Git에 포함되어 있음)
```

**MCP 설정 방식 비교:**

| 방식 | 보안성 | 편의성 | 권장 여부 |
|------|--------|--------|-----------|
| `claude mcp add --scope user` | ✅ 높음 (토큰이 사용자 레벨) | ✅ 높음 (한 번만 설정) | ⭐ 권장 |
| `.mcp.json` 파일 | ⚠️ 낮음 (프로젝트별 관리) | ⚠️ 중간 (프로젝트마다 설정) | ❌ 비권장 |

**주요 파일 관리:**
- ✅ `.taskmaster/tasks/tasks.json` - Git에 포함 (작업 정보 유지)
- ✅ `.taskmaster/docs/` - Git에 포함 (PRD 문서)
- ✅ `CLAUDE.md`, `.aiassistant/rules/` - Git에 포함 (AI 가이드)
- ❌ `.env` - Git 제외 (환경별 설정)
- ❌ `.taskmaster/config.json` - Git 제외 (개인 API 키)
- ⚠️ `.mcp.json` - 더 이상 사용 안 함 (사용자 레벨 MCP 사용)

### 코드 품질 관리 (Spotless)

이 프로젝트는 [Spotless](https://github.com/diffplug/spotless)와 [ktlint](https://github.com/pinterest/ktlint)를 사용하여 코드 스타일을 자동으로 관리합니다.

```bash
# 코드 포맷팅 검사
./gradlew spotlessCheck

# 코드 자동 포맷팅
./gradlew spotlessApply
```

#### 주요 설정

- **최대 라인 길이**: 120자
- **들여쓰기**: 공백 4칸
- **줄 끝**: LF (Unix 스타일)
- **파일 끝**: 빈 줄 추가
- **후행 공백**: 자동 제거

#### 권장 워크플로우

커밋 전에 항상 `spotlessApply`를 실행하여 코드 스타일을 통일하세요:

```bash
# 작업 완료 후 코드 포맷팅 적용
./gradlew spotlessApply

# 테스트 및 빌드
./gradlew build

# Git 커밋
git add .
git commit -m "feat: 새로운 기능 추가"
```

`.editorconfig` 파일이 포함되어 있어 대부분의 IDE에서 자동으로 코드 스타일이 적용됩니다.

## 📁 프로젝트 구조

```
lms-demo/
├── .taskmaster/               # TaskMaster AI 설정 및 문서
│   ├── docs/prd.md           # 프로덕트 요구사항 문서
│   └── CLAUDE.md             # Claude Code 통합 가이드
├── .claude/                  # Claude Code 설정
│   └── commands/             # 커스텀 명령어
├── domain/                   # 도메인 레이어 (순수 Kotlin)
│   └── src/main/kotlin/com/lms/domain/
│       ├── model/            # Aggregate Root, Entity, Value Object
│       │   ├── user/         # User Aggregate
│       │   ├── employee/     # Employee Aggregate
│       │   ├── store/        # Store Aggregate
│       │   ├── schedule/     # WorkSchedule Aggregate
│       │   ├── attendance/   # AttendanceRecord Aggregate
│       │   ├── leave/        # LeaveRequest Aggregate
│       │   └── payroll/      # Payroll, PayrollPolicy Aggregate
│       ├── service/          # Domain Service
│       ├── common/           # DomainContext
│       └── exception/        # Domain Exception
├── application/              # 애플리케이션 레이어
│   └── src/main/kotlin/com/lms/application/
├── infrastructure/           # 인프라 레이어
│   └── src/main/kotlin/com/lms/infrastructure/
│       ├── persistence/      # JPA Entity, Mapper, Repository
│       ├── config/           # 설정
│       └── security/         # 보안 구현
└── interfaces/               # 프레젠테이션 레이어
    └── src/main/kotlin/com/lms/interfaces/
        └── web/              # REST API Controller, DTO
```

## 🎓 주요 설계 결정

### 1. User와 Employee 분리

- **User**: 인증/인가 관심사 (로그인, 권한)
- **Employee**: 비즈니스 관심사 (근무, 연차, 급여)
- **관계**: 1:1 (userId 참조)

### 2. 순수 도메인 레이어

```kotlin
// ❌ 잘못된 예: 도메인에 JPA 의존성
@Entity
data class User(...)

// ✅ 올바른 예: 순수 Kotlin
data class User private constructor(
    val id: UserId,
    val email: Email,
    val password: Password,
    val role: Role,
    ...
)
```

### 3. Value Object 활용

```kotlin
// ❌ Primitive Obsession
data class User(val id: String, val email: String)

// ✅ Value Object 사용
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId는 비어있을 수 없습니다." }
    }
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "유효하지 않은 이메일 형식입니다."
        }
    }
}
```

### 4. DomainContext 패턴

모든 도메인 메서드는 첫 번째 인자로 `DomainContext`를 받아 요청 메타데이터를 추적합니다.

```kotlin
interface DomainContext {
    val serviceName: String
    val userId: String
    val userName: String
    val roleId: String
    val requestId: UUID
    val requestedAt: Instant
    val clientIp: String
}

// 사용 예시
fun login(context: DomainContext): User {
    require(isActive) { "비활성화된 사용자입니다." }
    return this.copy(lastLoginAt = context.requestedAt)
}
```

## 🔮 향후 개선 과제 (Future Improvements)

### Multi-Instance 환경에서의 배치 처리

현재 급여 자동 산정 배치는 `@Scheduled` 어노테이션을 사용하여 구현되어 있습니다. Multi-Instance 환경에서 애플리케이션을 수평 확장할 경우, 모든 인스턴스가 동시에 스케줄된 작업을 실행하게 되어 **중복 실행 문제**가 발생할 수 있습니다.

**고려 가능한 솔루션**:
- ShedLock (분산 락)
- Spring Batch + Quartz (별도 모듈)
- Temporal Workflow (워크플로우 오케스트레이션)

프로덕션 환경으로 전환 시 적절한 솔루션을 선택하여 적용할 필요가 있습니다.

---

## 📚 참고 문서

- [DEMO_SCENARIOS.md](./docs/DEMO_SCENARIOS.md) - 역할별 시연 시나리오 가이드
- [LOCAL_MYSQL_SETUP.md](./docs/LOCAL_MYSQL_SETUP.md) - 로컬 MySQL 설정 가이드
- [ARCHITECTURE.md](./docs/ARCHITECTURE.md) - 아키텍처 상세 설명
- [CLAUDE.md](./CLAUDE.md) - Claude Code 가이드
- [.taskmaster/CLAUDE.md](./.taskmaster/CLAUDE.md) - TaskMaster AI 통합 가이드
- [.taskmaster/docs/prd.md](./.taskmaster/docs/prd.md) - 프로덕트 요구사항 문서

## 🤝 기여하기

이 프로젝트는 TaskMaster AI 워크플로우 시연을 목적으로 하므로, 기여 시에도 TaskMaster AI를 활용한 프로세스를 따라주세요.

1. 이슈 생성 또는 기능 제안
2. TaskMaster로 태스크 추가: `task-master add-task --prompt="기능 설명"`
3. 서브태스크로 세분화: `task-master expand --id=<task-id>`
4. 구현 및 진행 상황 기록
5. Pull Request 생성

## 📝 라이선스

MIT License

## 👨‍💻 개발자

Built with ❤️ using **TaskMaster AI** and **Claude Code**

---

**TaskMaster AI**: AI 기반 태스크 관리 도구
**Claude Code**: AI 페어 프로그래밍 도구
