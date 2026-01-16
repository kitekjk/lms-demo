# RBAC 권한 관리 가이드

## 역할 정의

### SUPER_ADMIN (슈퍼 관리자)
- **권한**: 모든 리소스에 대한 전체 권한
- **책임**:
  - 사용자 생성 및 관리 (MANAGER, EMPLOYEE 등록)
  - 시스템 설정 관리
  - 모든 매장 및 근로자 데이터 접근
  - 급여 정책 설정

### MANAGER (매니저)
- **권한**: 소속 매장 관련 데이터 관리 권한
- **책임**:
  - 소속 매장의 근로자 관리 (등록, 수정, 조회)
  - 소속 매장의 근무 일정 관리
  - 소속 매장의 출퇴근 기록 조회
  - 소속 매장 근로자의 휴가 신청 승인/거부
  - 소속 매장 근로자의 급여 조회

### EMPLOYEE (근로자)
- **권한**: 본인 데이터에 대한 조회 및 제한적 수정 권한
- **책임**:
  - 본인 정보 조회
  - 본인 근무 일정 조회
  - 출퇴근 체크
  - 휴가 신청
  - 본인 급여 조회

## @PreAuthorize 사용 예시

### 1. 역할 기반 접근 제어

```kotlin
// SUPER_ADMIN만 접근 가능
@PreAuthorize("hasRole('SUPER_ADMIN')")
fun createUser() { }

// SUPER_ADMIN 또는 MANAGER 접근 가능
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
fun getEmployees() { }

// EMPLOYEE만 접근 가능
@PreAuthorize("hasRole('EMPLOYEE')")
fun checkIn() { }
```

### 2. 본인 확인 권한 제어

```kotlin
// EMPLOYEE는 본인 정보만 조회 가능
@PreAuthorize("hasRole('EMPLOYEE') and #employeeId == authentication.principal")
fun getEmployeeInfo(@PathVariable employeeId: String) { }

// MANAGER는 소속 매장, EMPLOYEE는 본인만
@PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.belongsToSameStore(#employeeId)) or (hasRole('EMPLOYEE') and #employeeId == authentication.principal)")
fun updateEmployee(@PathVariable employeeId: String) { }
```

### 3. 매장 기반 권한 제어

```kotlin
// SUPER_ADMIN 또는 해당 매장의 MANAGER만 접근 가능
@PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityUtils.belongsToStore(#storeId))")
fun getStoreEmployees(@PathVariable storeId: String) { }
```

## SecurityUtils 활용

### 현재 사용자 정보 조회

```kotlin
import com.lms.infrastructure.security.SecurityUtils

class SomeService {
    fun doSomething() {
        // 현재 사용자 ID
        val userId = SecurityUtils.getCurrentUserId()

        // 현재 사용자 역할
        val role = SecurityUtils.getCurrentUserRole()

        // 현재 사용자 매장 ID
        val storeId = SecurityUtils.getCurrentStoreId()

        // 인증 여부 확인
        if (SecurityUtils.isAuthenticated()) {
            // ...
        }

        // 역할 확인
        if (SecurityUtils.isSuperAdmin()) {
            // SUPER_ADMIN만 실행
        }

        if (SecurityUtils.isManager()) {
            // MANAGER만 실행
        }

        // 본인 확인
        if (SecurityUtils.isCurrentUser(employeeId)) {
            // 본인일 때만 실행
        }

        // 매장 소속 확인
        if (SecurityUtils.belongsToStore(storeId)) {
            // 해당 매장 소속일 때만 실행
        }
    }
}
```

## 권한 체크 패턴

### 1. Controller Layer
- `@PreAuthorize` 어노테이션 사용
- 선언적 보안으로 가독성 향상

```kotlin
@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityUtils.belongsToStore(#id)) or (hasRole('EMPLOYEE') and #id == authentication.principal)")
    fun getEmployee(@PathVariable id: String): EmployeeResponse {
        return employeeService.getEmployee(id)
    }
}
```

### 2. Service Layer
- SecurityUtils를 활용한 프로그래밍 방식 권한 체크
- 복잡한 비즈니스 로직에 적합

```kotlin
@Service
class EmployeeService {
    fun getEmployee(employeeId: String): Employee {
        // SUPER_ADMIN은 모든 접근 가능
        if (SecurityUtils.isSuperAdmin()) {
            return employeeRepository.findById(employeeId)
        }

        // MANAGER는 소속 매장만
        if (SecurityUtils.isManager()) {
            val employee = employeeRepository.findById(employeeId)
            require(SecurityUtils.belongsToStore(employee.storeId)) {
                "접근 권한이 없습니다"
            }
            return employee
        }

        // EMPLOYEE는 본인만
        require(SecurityUtils.isCurrentUser(employeeId)) {
            "본인 정보만 조회 가능합니다"
        }

        return employeeRepository.findById(employeeId)
    }
}
```

## 권한별 API 접근 매트릭스

| API Endpoint | SUPER_ADMIN | MANAGER | EMPLOYEE |
|--------------|-------------|---------|----------|
| POST /api/auth/register | ✅ | ❌ | ❌ |
| GET /api/employees | ✅ | ✅ (소속 매장만) | ❌ |
| GET /api/employees/{id} | ✅ | ✅ (소속 매장만) | ✅ (본인만) |
| POST /api/employees | ✅ | ✅ (소속 매장만) | ❌ |
| PUT /api/employees/{id} | ✅ | ✅ (소속 매장만) | ✅ (본인만, 제한적) |
| DELETE /api/employees/{id} | ✅ | ❌ | ❌ |
| GET /api/schedules | ✅ | ✅ (소속 매장만) | ✅ (본인만) |
| POST /api/schedules | ✅ | ✅ (소속 매장만) | ❌ |
| POST /api/attendance/check-in | ✅ | ✅ | ✅ |
| POST /api/leave-requests | ✅ | ✅ | ✅ |
| PUT /api/leave-requests/{id}/approve | ✅ | ✅ (소속 매장만) | ❌ |
| GET /api/payrolls | ✅ | ✅ (소속 매장만) | ✅ (본인만) |

## 주의사항

1. **민감한 작업은 SUPER_ADMIN만**
   - 사용자 생성/삭제
   - 급여 정책 설정
   - 시스템 설정 변경

2. **MANAGER 권한 검증**
   - 항상 storeId를 확인하여 소속 매장만 접근하도록 제한
   - `SecurityUtils.belongsToStore()` 활용

3. **EMPLOYEE 권한 검증**
   - 본인 데이터만 접근 가능하도록 엄격히 제한
   - `SecurityUtils.isCurrentUser()` 활용

4. **예외 처리**
   - 권한 없음: `403 Forbidden`
   - 인증 실패: `401 Unauthorized`
   - 리소스 없음: `404 Not Found`

## 보안 체크리스트

- [ ] 모든 API 엔드포인트에 적절한 권한 체크 적용
- [ ] SUPER_ADMIN 전용 API는 `@PreAuthorize("hasRole('SUPER_ADMIN')")` 설정
- [ ] MANAGER는 소속 매장 데이터만 접근하도록 storeId 검증
- [ ] EMPLOYEE는 본인 데이터만 접근하도록 userId 검증
- [ ] 민감한 정보(급여, 개인정보)는 역할별로 필터링하여 반환
- [ ] SecurityUtils를 활용한 프로그래밍 방식 권한 체크 구현
- [ ] 권한 오류 시 적절한 HTTP 상태 코드 반환 (401, 403)
