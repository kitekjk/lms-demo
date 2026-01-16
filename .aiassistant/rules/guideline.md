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

**`gradle/libs.versions.toml` 표준 예시:**
```toml
[versions]
kotlin = "1.9.22"
springBoot = "3.2.2"
dependencyManagement = "1.1.4"

[libraries]
kotlin-reflect = { group = "org.jetbrains.kotlin", name = "kotlin-reflect" }
kotlin-stdlib = { group = "org.jetbrains.kotlin", name = "kotlin-stdlib" }
spring-boot-starter-web = { group = "org.springframework.boot", name = "spring-boot-starter-web" }
# ... 기타 라이브러리

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "dependencyManagement" }
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
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }
    
    // ... 공통 의존성 및 설정
}
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

- 도메인 내에서 발생한 예외는 항상 DomainException 상속 받은 예외를 만들어서 사용
- DomainException, DomainExceptionBase 를 상속 받는다
- 예외 케이스에 따라 추가적인 정보를 같이 포함 시킬 수 있다.
- 예외 코드(ErrorCode)를 체계화 하여 정리 한다.

**DomainException 예시:**
```kotlin
open class DomainException(
    val code: String,
    message: String,
    originalError: Throwable? = null
) : RuntimeException(message, originalError)

enum class ErrorCode(val code: String, val message: String) {
    ImpossibleCancel("OA050001", "취소 불가한 상태 입니다."),
    NoRefundPolicy("OA052001", "환불 정책이 없습니다."),
    InvalidItem("OA010001", "정상적인 상품아이템이 아닙니다.")
}
```

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

**예시:**
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

## ✅ 이 문서는 자동 인식하여 코드 생성 시 참조됩니다.
