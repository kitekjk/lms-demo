# LMS Demo - Project Architecture

## 📁 Project Structure

This project follows **Domain-Driven Design (DDD)** principles with a **multi-module architecture**.

```
lms-demo/
├── domain/              # Pure business logic (no external dependencies)
│   └── src/main/kotlin/com/lms/domain/
│       ├── model/       # Aggregate roots, entities, value objects
│       ├── service/     # Domain services (multi-aggregate logic)
│       ├── exception/   # Domain-specific exceptions
│       ├── event/       # Domain events
│       └── common/      # Common domain DTOs, VOs
│
├── application/         # Use cases and orchestration
│   └── src/main/kotlin/com/lms/application/
│       └── (service)/   # Application services (@Service, @Transactional)
│
├── infrastructure/      # Technical implementations
│   └── src/main/kotlin/com/lms/infrastructure/
│       ├── persistence/ # JPA repository implementations
│       ├── security/    # Security configurations (JWT, filters)
│       └── config/      # Technical configs (DB, cache, properties)
│
└── interfaces/          # External interfaces (REST API)
    └── src/main/kotlin/com/lms/interfaces/
        ├── web/
        │   ├── controller/  # REST controllers
        │   └── dto/         # Request/Response DTOs
        └── LmsApplication.kt  # Spring Boot entry point
```

## 🎯 Module Responsibilities

### domain
**Purpose:** Pure business logic and domain model

**Rules:**
- ✅ Pure Kotlin code only
- ❌ NO Spring dependencies
- ❌ NO JPA annotations
- ❌ NO infrastructure concerns

**Contents:**
- Aggregate Roots (business entities with identity)
- Value Objects (immutable, identity-less)
- Domain Services (cross-aggregate logic)
- Repository Interfaces (defined by domain, implemented by infrastructure)
- Domain Events

### application
**Purpose:** Use cases and application orchestration

**Rules:**
- ✅ Depends on domain module only
- ✅ Spring Context allowed (@Service, @Transactional)
- ❌ NO direct infrastructure dependencies
- ✅ One public function per AppService (single responsibility)

**Pattern:**
```kotlin
@Service
@Transactional
class PlaceOrderAppService(
    private val orderRepository: OrderRepository
) {
    fun execute(command: PlaceOrderCommand): OrderResult {
        // Orchestration logic
    }
}
```

### infrastructure
**Purpose:** Technical implementations

**Rules:**
- ✅ Implements domain interfaces
- ✅ JPA, Spring Data, external APIs
- ✅ Security configurations

**Contents:**
- JPA Repository Implementations
- JWT Token Provider
- Security Filters
- Configuration Properties

### interfaces
**Purpose:** External API layer

**Rules:**
- ✅ REST Controllers
- ✅ DTO ↔ Command conversion
- ❌ NO business logic
- ✅ Calls application services only

**Pattern:**
```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val placeOrderAppService: PlaceOrderAppService
) {
    @PostMapping
    fun placeOrder(@RequestBody req: PlaceOrderRequest): ResponseEntity<OrderResponse> {
        val command = req.toCommand()
        val result = placeOrderAppService.execute(command)
        return ResponseEntity.ok(OrderResponse.from(result))
    }
}
```

## 🔧 Configuration Files

### application.yml
Main configuration file with common settings:
- Spring application name
- Profile activation
- HikariCP connection pool
- JPA/Hibernate settings
- Jackson JSON settings
- JWT configuration
- Logging levels

### application-dev.yml
Development profile:
- Local database connection
- `ddl-auto: update` for schema auto-update
- Verbose logging (DEBUG, TRACE)
- `show-sql: true`

### application-prod.yml
Production profile:
- Environment variable-based configuration
- `ddl-auto: validate` (no auto-update)
- Minimal logging (WARN, INFO)
- `show-sql: false`
- Larger connection pool

### .env.example
Environment variables template:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET_KEY`
- `SPRING_PROFILES_ACTIVE`

## 🔐 Configuration Properties

### JwtProperties
Binds `jwt.*` configuration from application.yml:
```kotlin
@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secretKey: String = "",
    var accessTokenExpiration: Long = 3600000,
    var refreshTokenExpiration: Long = 604800000
)
```

## 🏗️ Technology Stack

- **Language:** Kotlin 1.9.22
- **Framework:** Spring Boot 3.2.2
- **Database:** MySQL 8.3.0
- **ORM:** Hibernate 6.4.1
- **Security:** JWT (JJWT 0.12.5)
- **Testing:** Kotest 5.8.0
- **Build:** Gradle 8.5 with Kotlin DSL

## 📦 Dependency Management

Using **Gradle Version Catalog** (`gradle/libs.versions.toml`):
- Centralized version management
- Type-safe dependency references
- `libs.spring.boot.starter.web` format

## 🔄 Data Flow

```
HTTP Request
    ↓
[REST Controller] (interfaces)
    ↓ DTO → Command
[Application Service] (application) @Transactional
    ↓ Orchestrates
[Domain Service] (domain) ← Business Logic
    ↓ Modifies
[Aggregate Root] (domain) ← State + Rules
    ↓ Persists
[Repository Impl] (infrastructure) ← JPA
    ↓
Database
```

## 🎨 DDD Patterns

### Aggregate
- Consistency boundary
- Has single Aggregate Root
- Accessed only through root
- Example: Order (root) contains OrderItems

### Repository
- Interface in domain layer
- Implementation in infrastructure
- Provides collection-like abstraction

### Domain Service
- Cross-aggregate business logic
- Stateless
- Lives in domain/service

### Application Service
- One use case per service
- Transaction boundary
- Calls domain objects and repositories

### Value Object
- Immutable
- No identity
- Equality by value
- Example: Money, Address, Email

## 📝 Coding Conventions

### Naming
- `*AppService` - Application services
- `*Repository` - Repository interfaces
- `*RepositoryImpl` - Repository implementations
- `*Request`, `*Response` - DTOs
- `*Command`, `*Result` - Application layer
- `*Event` - Domain events

### Package Names
- `dto` - Data Transfer Objects
- `service` - Services (domain or application)
- `model` - Domain entities
- `controller` - REST controllers
- `config` - Configuration classes

## 🧪 Testing Strategy

### Unit Tests
- Domain logic (pure Kotlin)
- No Spring context
- Fast execution

### Integration Tests
- Spring Boot context
- Database integration
- `@SpringBootTest`

### Test Structure (Kotest)
```kotlin
class MyTest : StringSpec({
    "테스트 케이스 설명" {
        // Given
        // When
        // Then
        result shouldBe expected
    }
})
```

## 🚀 Running the Application

### Development
```bash
# With dev profile (default)
./gradlew :interfaces:bootRun

# Or with environment variable
SPRING_PROFILES_ACTIVE=dev ./gradlew :interfaces:bootRun
```

### Production
```bash
# Set environment variables first
export DB_URL=jdbc:mysql://prod-db:3306/lms_demo
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET_KEY=your-256-bit-secret
export SPRING_PROFILES_ACTIVE=prod

# Run application
./gradlew :interfaces:bootRun
```

### Building
```bash
# Clean and build all modules
./gradlew clean build

# Build specific module
./gradlew :interfaces:build

# Skip tests
./gradlew build -x test
```

## 📖 References

- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Gradle Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)
