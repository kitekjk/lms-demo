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
- **의존성 역전**: domain → application → infrastructure/interfaces
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

- **Language**: Kotlin 1.9.22
- **Framework**: Spring Boot 3.2.2
- **Build Tool**: Gradle 8.5 with Kotlin DSL
- **Database**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **Architecture**: DDD + Clean Architecture

## 📋 시작하기

### 필요 조건

- JDK 17 이상
- Gradle 8.5 이상

### 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 제외하고 빌드
./gradlew build -x test

# 애플리케이션 실행
./gradlew :interfaces:bootRun
```

### 환경 변수 설정

`.env.example`을 복사하여 `.env` 파일을 생성하고 필요한 값을 설정하세요.

```bash
cp .env.example .env
```

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

## 📚 참고 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 아키텍처 상세 설명
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
