---
적용: 항상
---

# 🧠 프로젝트 코드 작성 가이드라인

이 가이드는 Spring Boot 3.7 + Kotlin 기반의 도메인 주도 설계(DDD) 멀티모듈 프로젝트에서 정확하고 일관된 코드 생성을 수행할 수 있도록 하는 기준 문서입니다.

---

## 🚀 신규 프로젝트 Boilerplate 구성

신규 프로젝트를 초기화하거나 스캐폴딩할 때는 반드시 아래의 빌드 환경 및 구조 규칙을 준수한다.

### 1. Build Environment
- **Kotlin DSL**: 모든 Gradle 스크립트는 Kotlin DSL(`build.gradle.kts`, `settings.gradle.kts`)을 사용한다.
- **Version Catalog**: 의존성 버전 관리는 `gradle/libs.versions.toml` 파일을 통해 수행한다. (기존 `buildSrc` 방식 지양)
- **Spring Boot 버전**: Spring Boot 3.x 버전 중 최신 안정 버전(Stable Release)을 사용한다. (현재: 3.5.9)
- **Code Quality**: Spotless + ktlint를 사용하여 코드 스타일을 자동 관리한다.

**`gradle/libs.versions.toml` 표준 예시:**
```toml
[versions]
kotlin = "2.1.0"
springBoot = "3.5.9"
springDependencyManagement = "1.1.7"
spotless = "7.0.2"
ktlint = "1.5.0"

[libraries]
kotlin-reflect = { group = "org.jetbrains.kotlin", name = "kotlin-reflect" }
kotlin-stdlib = { group = "org.jetbrains.kotlin", name = "kotlin-stdlib" }
spring-boot-starter-web = { group = "org.springframework.boot", name = "spring-boot-starter-web" }
# ... 기타 라이브러리

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "springDependencyManagement" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
```

### 2. Multi-Module Configuration
- Root 프로젝트는 소스 코드를 가지지 않으며, 하위 모듈을 관리하는 역할만 수행한다.
- 공통 설정은 Root의 `build.gradle.kts` 내 `subprojects` 또는 `allprojects` 블록을 활용하지 않고, **Convention Plugin** 방식을 권장하나, 초기 단계에서는 `subprojects` 블록을 허용한다.

**`settings.gradle.kts` 구성 예시:**
```kotlin
rootProject.name = "my-project"

include("domain")
include("application")
include("infrastructure")
include("interfaces")
```

**Root `build.gradle.kts` 구성 예시:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
    }

    // Spotless 설정
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint(rootProject.libs.versions.ktlint.get())
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_no-wildcard-imports" to "disabled",
                        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                        "ktlint_standard_filename" to "disabled",
                        "max_line_length" to "120"
                    )
                )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(rootProject.libs.versions.ktlint.get())
        }
    }

    // ... 공통 의존성 및 설정
}
```

**`.editorconfig` 파일 생성 (프로젝트 루트):**
```editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_size = 4
indent_style = space
max_line_length = 120
ij_kotlin_imports_layout = *

[*.{yml,yaml}]
indent_size = 2
indent_style = space

[*.md]
trim_trailing_whitespace = false

[*.{json,toml}]
indent_size = 2
indent_style = space
```

---

## 📦 프로젝트 모듈 구조

```
project-root/
├── domain/
│   └── common/                # 도메인 공영 DTO, VO, etc
│   └── exception/             # 도메인 예외
│   └── event/                 # 도메인 이벤트
│   └── model/
│       ├── {aggregate}/       # Aggregate별 서브패키지
│       └── service/           # 여러 Aggregate 관련 도메인 서비스
├── application/
│   └── {context}/             # UseCase 및 서비스
├── infrastructure/
│   └── persistence/           # DB, 외부 구현체
├── interfaces/
│   └── web/                   # REST Controller 등 API 계층
```

---

## 📌 모듈별 책임

### 0. 공통
- Dto 클래스는 data class로 만들고 dto 패키지명으로 분리

### 1. domain 모듈

- 비즈니스 로직, 도메인 모델 정의
- 순수 Kotlin 코드로 작성
- 외부 라이브러리(Spring, JPA 등) 금지
- Aggregate Root 내에 비즈니스 로직을 만듬
- 여러 Aggregate와 연관된 비즈니스 로직은 도메인 서비스로 분리

#### 1.1 domain context

- 공용으로 사용되는 요소를 추상화한 interface
- serviceName(요청도메인), userId, userName, roleId, requestId(uuid), requestedAt(Instant), clientIp 등등
- http, kafka 이벤트등에서 해더 값으로 전달 받으며 이를 파싱하여 DomainContext 로 만듬
- Aggregate 와 도메인 서비스의 함수는 항상 1번째 인자로 domain context를 받음

#### 1.2 domain 기본 모듈 구조

**하위 구성요소 및 역할:**

| 구성요소 | 설명 |
|----------|------|
| Aggregate Root | 도메인 로직과 상태 변경의 진입점 |
| Entity | 식별자가 존재하며 변경 가능한 객체 |
| Value Object | 식별자 없고 불변, 의미 기반 타입 |
| Repository Interface | 도메인에서 정의하는 저장소 인터페이스 |
| Domain Service | 복수 Aggregate 간 도메인 규칙, domain/service에 위치 |

**패키지 구조 예시:**
```
domain/model/order/
├── Order.kt              # Aggregate Root
├── OrderItem.kt          # Entity
├── OrderId.kt            # VO
├── OrderRepository.kt    # Repository 인터페이스

domain/service/
└── OrderPolicyService.kt # 도메인 서비스
```

#### 1.3 domain 이벤트 모듈 구조
- Aggregate 변화가 생기면 항상 1개의 이벤트를 생성 및 발행한다.
- DomainEvent, DomainEventBase 를 상속 받는다.

**DomainEvent 예시:**
```kotlin
interface DomainEvent<T> {
    val eventId: UUID
    val occurredOn: Long
    val context: DomainContext
    val payload: T
}

abstract class DomainEventBase<T> : DomainEvent<T> {
    override val eventId: UUID = UUID.randomUUID()
    override val occurredOn: Long = Date().time
}
```

#### 1.4 domain 예외 모듈 구조

- 도메인 내에서 발생한 예외는 항상 DomainException을 상속받은 구체적인 예외 클래스를 만들어서 사용
- **DomainException은 abstract class로 선언하여 직접 사용 금지**
- 각 오류 케이스별로 구체적인 예외 클래스를 생성
- 에러 코드는 예외 클래스 내부에 캡슐화하여 숨김
- 예외 케이스에 따라 추가적인 정보를 생성자로 전달 가능

**DomainException 기본 구조:**
```kotlin
// 추상 클래스로 선언하여 직접 인스턴스화 방지
abstract class DomainException(
    val code: String,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
```

**구체적인 예외 클래스 예시:**
```kotlin
// 인증 관련 예외
class AuthenticationFailedException(
    message: String = "이메일 또는 비밀번호가 일치하지 않습니다.",
    cause: Throwable? = null
) : DomainException("AUTH001", message, cause)

class InactiveUserException(
    message: String = "비활성화된 사용자입니다.",
    cause: Throwable? = null
) : DomainException("AUTH002", message, cause)

// 등록 관련 예외 (추가 정보 포함 예시)
class DuplicateEmailException(
    email: String,
    cause: Throwable? = null
) : DomainException("REG001", "이미 등록된 이메일입니다: $email", cause)

class InvalidRoleException(
    role: String,
    cause: Throwable? = null
) : DomainException("REG002", "유효하지 않은 역할입니다: $role", cause)
```

**사용 예시:**
```kotlin
// ❌ 잘못된 사용 (DomainException 직접 사용)
throw DomainException("AUTH001", "인증 실패")

// ✅ 올바른 사용 (구체적인 예외 클래스 사용)
throw AuthenticationFailedException()

// ✅ 추가 정보를 포함한 예외
throw DuplicateEmailException(email = "user@example.com")
```

**이점:**
- 타입 안전성 향상 (컴파일 타임에 예외 타입 체크)
- 에러 코드 캡슐화로 코드 가독성 향상
- 특정 예외에 대한 catch 블록 작성 용이
- IDE 자동완성 지원으로 개발 생산성 향상

### 2. application 모듈

- UseCase 단위로 정의하고 AppService 를 postfix 로 선언
- AppService 는 서로 참조 금지
- 트랜잭션 경계 책임
- Spring Context 의존 허용 (`@Service`, `@Transactional` 등)
- domain만 의존 (infrastructure에 의존 금지)
- 별도의 interfaces 를 사용하지 않고 class로 선언하고 하나의 public 함수만 사용(단일책임원칙)
- @Transactional 을 함수가 아닌 class에 선언
- 비즈니스 로직은 도메인에서 다루게 하고, orchastration 역할만 사용

**구조 예시:**
```kotlin
@Service
@Transactional
class PlaceOrderAppService(
    private val orderRepository: OrderRepository
) {
    fun execute(command: PlaceOrderCommand): OrderResult { ... }
}
```

### 3. infrastructure 모듈

- 기술 구현 (JPA, Redis, Kafka 등)
- Repository, 외부 API 구현체 등
- domain의 인터페이스를 구현

#### 3.1 JPA Auditing 설정

**생성일시, 수정일시는 JPA Auditing을 사용하여 자동 관리한다.**
- `@CreatedDate`, `@LastModifiedDate` 어노테이션 사용
- `@EntityListeners(AuditingEntityListener::class)` 추가
- BaseEntity를 상속받아 공통 타임스탬프 관리

**BaseEntity 예시:**
```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set
}
```

**JPA Auditing 활성화 (Application 또는 Config 클래스):**
```kotlin
@Configuration
@EnableJpaAuditing
class JpaConfig
```

**Entity에서 사용:**
```kotlin
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    var id: String,
    var email: String,
    // ... 기타 필드
) : BaseEntity()  // BaseEntity 상속으로 createdAt, updatedAt 자동 관리
```

#### 3.2 Enum 타입 변환

**Enum을 데이터베이스에 저장할 때는 반드시 `AttributeConverter`를 사용한다.**
- JPA의 `@Enumerated` 어노테이션 사용 금지
- `@Converter(autoApply = true)`를 사용하여 전역 적용
- 명시적인 타입 변환으로 데이터베이스 값 제어

**AttributeConverter 예시:**
```kotlin
@Converter(autoApply = true)
class RoleConverter : AttributeConverter<Role, String> {
    override fun convertToDatabaseColumn(attribute: Role?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): Role? =
        dbData?.let { Role.valueOf(it) }
}
```

**Repository 예시:**
```kotlin
@Repository
class OrderRepositoryImpl(
    private val jpaRepo: JpaOrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order { ... }
}
```

### 4. interfaces 모듈

- 외부 요청과 내부 시스템 간의 API 인터페이스 역할
- REST Controller, 메시지 수신 핸들러 등
- DTO ↔ Command 변환 수행, UseCase 호출 담당

**예시:**
```kotlin
@RestController
@RequestMapping("/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase
) {
    @PostMapping
    fun placeOrder(@RequestBody req: PlaceOrderRequest): ResponseEntity<OrderResponse> {
        val command = req.toCommand()
        val result = placeOrderUseCase.execute(command)
        return ResponseEntity.ok(OrderResponse.from(result))
    }
}
```

---

## 📚 코드 작성 규칙

### ✅ 공통

- Kotlin idiomatic style (`val`, null-safety, `data class`, `sealed class`)
- VO는 `@JvmInline` 또는 `data class`
- 객체 생성을 위한 `create()` 정적 메서드 권장
- enum보다는 sealed class 선호

### ✅ domain

- Spring, JPA, 외부 라이브러리 금지
- 테스트 가능한 순수 Kotlin 코드
- ID와 VO로 명확한 경계 표현

### ✅ application

- UseCase는 인터페이스 + Service 조합
- 트랜잭션은 ApplicationService에서 선언
- 외부 기술 의존 없이 domain만 호출

### ✅ infrastructure

- 기술 구현체는 domain 인터페이스 구현
- Spring Data JPA 등은 여기에만 위치
- 외부 API 연동도 여기에 구현
- **Enum 타입은 반드시 AttributeConverter 사용** (`@Enumerated` 금지)
- `@Converter(autoApply = true)`로 전역 적용하여 명시적 타입 변환

### ✅ interfaces

- Controller는 UseCase만 호출
- DTO ↔ Command 변환 로직만 포함
- 비즈니스 로직 작성 금지

---

## 📂 예시 구조: Order Aggregate

```
domain/
└── model/
    ├── order/
    │   ├── Order.kt
    │   ├── OrderItem.kt
    │   ├── OrderId.kt
    │   ├── OrderStatus.kt
    │   ├── OrderRepository.kt
    └── service/
        └── OrderPolicyService.kt

application/
└── order/
    ├── PlaceOrderAppService.kt

infrastructure/
└── persistence/
    └── order/
        └── OrderRepositoryImpl.kt

interfaces/
└── web/
    └── order/
        └── OrderController.kt
```

---

## 🔒 주의사항 요약

- ❌ Controller에서 비즈니스 로직 수행 금지
- ❌ application → infrastructure 의존 금지
- ❌ domain에서 Spring 의존 금지
- ✅ 모든 도메인 로직은 테스트 가능하게 작성

---

## 테스트 코드 작성

- Kotest을 사용
- Given-When-Then 패턴을 이용
- Kotest의 shouldBe, shouldNotBe, shouldThrow 등의 매처를 사용
- 다양한 엣지 케이스에 대한 테스트 코드 작성
- 모든 입력 유효성 검사 테스트

---

## 📋 프로젝트 초기화 체크리스트

신규 프로젝트 생성 시 반드시 수행해야 할 작업들:

### 1. Git 설정

**`.gitignore` 파일 생성:**
```gitignore
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# IntelliJ IDEA
.idea/
*.iml
*.iws
out/

# Eclipse
.classpath
.project
.settings/
bin/

# MacOS
.DS_Store

# Windows
Thumbs.db

# 환경 변수
.env
*.env

# 로그
*.log

# TaskMaster AI (선택적)
.taskmaster/state.json
.taskmaster/reports/
```

**Git 초기화 및 첫 커밋:**
```bash
git init
git add .
git commit -m "chore: 프로젝트 초기 구성

- Multi-module DDD 구조
- Spring Boot 3.5.9 + Kotlin 2.1.0
- Spotless + ktlint 코드 품질 관리
- JPA Auditing, AttributeConverter 설정

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

### 2. GitHub Repository 생성

```bash
# GitHub CLI 사용 (권장)
gh repo create <repository-name> --public --source=. --remote=origin

# 또는 수동으로 원격 저장소 추가
git remote add origin https://github.com/<username>/<repository-name>.git

# 푸시
git branch -M master
git push -u origin master
```

### 3. README.md 작성

다음 섹션을 포함해야 함:
- 프로젝트 개요 및 목적
- 기술 스택
- 아키텍처 구조
- 빌드 및 실행 방법
- 코드 품질 관리 (Spotless)
- 환경 변수 설정
- 프로젝트 구조
- 개발 가이드 (필요시)

### 4. 환경 변수 템플릿

**`.env.example` 파일 생성:**
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

# JWT
JWT_SECRET_KEY=your-secret-key-here
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# 기타 설정
SPRING_PROFILES_ACTIVE=local
```

### 5. 코드 품질 검증

```bash
# Spotless 포맷팅 적용
./gradlew spotlessApply

# 빌드 및 테스트
./gradlew clean build

# 모든 검증이 통과하는지 확인
```

### 6. TaskMaster AI 설정 (선택적)

TaskMaster AI를 사용하는 경우:

```bash
# TaskMaster 초기화
task-master init

# PRD 문서 작성 (.taskmaster/docs/prd.md)
# 태스크 생성
task-master parse-prd .taskmaster/docs/prd.md

# 복잡도 분석
task-master analyze-complexity --research
```

### 7. 필수 설정 파일 확인

다음 파일들이 올바르게 생성되었는지 확인:
- ✅ `gradle/libs.versions.toml` - 의존성 버전 관리
- ✅ `build.gradle.kts` - Root Gradle 설정 (Spotless 포함)
- ✅ `.editorconfig` - 에디터 설정
- ✅ `.gitignore` - Git 제외 파일
- ✅ `README.md` - 프로젝트 문서
- ✅ `.env.example` - 환경 변수 템플릿
- ✅ 멀티모듈 구조 (`domain`, `application`, `infrastructure`, `interfaces`)

### 8. 최종 검증

```bash
# 1. Git 상태 확인
git status

# 2. 빌드 성공 확인
./gradlew clean build

# 3. Spotless 검증
./gradlew spotlessCheck

# 4. GitHub에 푸시
git push origin master
```

---

## ✅ 이 문서는 자동 인식하여 코드 생성 시 참조됩니다.
