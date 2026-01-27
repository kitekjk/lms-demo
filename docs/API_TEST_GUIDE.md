# 백엔드 서버 실행 및 API 테스트 가이드

## 🚀 빠른 시작 (3분 완성)

Docker가 설치되어 있다면 아래 명령어만으로 바로 시작할 수 있습니다!

```bash
# 1. MySQL 시작
docker-compose up -d

# 2. 서버 실행 (local 프로파일이 기본값)
.\gradlew :interfaces:bootRun

# 3. 브라우저에서 Swagger UI 접속
http://localhost:8080/swagger-ui.html
```

축하합니다! 🎉 이제 Swagger UI를 통해 API를 확인할 수 있습니다.

> **참고**: 현재 초기 데이터 로드가 비활성화되어 있습니다. API를 테스트하려면 먼저 사용자 등록 및 로그인이 필요합니다.

**프로파일별 실행:**
- **local** (기본): 로컬 개발 - 테이블 자동 생성/삭제
- **dev**: 개발 서버 - 스키마 자동 업데이트
- **prod**: 프로덕션 - 스키마 검증만 수행

```bash
# 개발 서버로 실행
.\gradlew :interfaces:bootRun --args='--spring.profiles.active=dev'

# 프로덕션 모드로 실행
.\gradlew :interfaces:bootRun --args='--spring.profiles.active=prod'
```

---

## 📋 목차
1. [사전 준비](#사전-준비)
2. [데이터베이스 설정](#데이터베이스-설정)
3. [서버 실행](#서버-실행)
4. [Swagger UI로 테스트](#swagger-ui로-테스트)
5. [cURL로 테스트](#curl로-테스트)
6. [Postman으로 테스트](#postman으로-테스트)
7. [통합 테스트 실행](#통합-테스트-실행)

---

## 🔧 사전 준비

### 1. 필수 소프트웨어
- **JDK 21** 이상
- **MySQL 8.0** 이상
- **Gradle** (프로젝트에 포함된 Gradle Wrapper 사용 가능)

### 2. 환경 변수 설정
`.env` 파일이 없다면 생성:
```bash
cp .env.example .env
```

`.env` 파일 내용 확인 및 수정:
```properties
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lms_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password_here

# JWT
JWT_SECRET_KEY=my-secret-key-for-development-only-must-be-changed-in-production-environment
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
```

---

## 🗄️ 데이터베이스 설정

### 1. MySQL 설치 및 실행

#### 옵션 1: Docker 사용 (권장) 🐳

프로젝트 루트에 `docker-compose.yml` 파일이 있습니다.

**MySQL 컨테이너 시작:**
```bash
docker-compose up -d
```

**컨테이너 상태 확인:**
```bash
docker-compose ps
```

**MySQL 접속 확인:**
```bash
docker exec -it lms-demo-mysql mysql -uroot -pchangeme
```

**컨테이너 중지:**
```bash
docker-compose down
```

**데이터를 포함하여 완전 삭제:**
```bash
docker-compose down -v
```

#### 옵션 2: 로컬 MySQL 설치

MySQL 8.0 이상을 설치하고 아래 명령으로 데이터베이스 생성:
```sql
CREATE DATABASE lms_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**접속 정보:**
- Host: `localhost`
- Port: `3306`
- Database: `lms_demo`
- Username: `root`
- Password: `changeme` (`.env` 파일에서 변경 가능)

### 2. 스키마 생성 방법

프로젝트는 **프로파일별로 다른 스키마 관리 전략**을 사용합니다.

#### Spring Profile 설정

**local 프로파일 (기본)** - `application-local.yml`
- `ddl-auto: create-drop` - 서버 시작 시 테이블 생성, 종료 시 삭제
- `sql.init.mode: always` - data.sql 자동 실행
- **용도**: 로컬 개발 및 테스트

**dev 프로파일** - `application-dev.yml`
- `ddl-auto: update` - 스키마 변경사항 자동 반영
- `sql.init.mode: never` - 초기 데이터 로드하지 않음
- **용도**: 개발 서버

**prod 프로파일** - `application-prod.yml`
- `ddl-auto: validate` - 스키마 검증만 수행 (안전)
- `sql.init.mode: never` - 초기 데이터 로드하지 않음
- **용도**: 프로덕션 서버

#### 프로파일 변경 방법

**기본 프로파일 변경:**
`application.yml`에서 수정
```yaml
spring:
  profiles:
    active: local  # local, dev, prod 중 선택
```

**실행 시 프로파일 지정:**
```bash
# Gradle
.\gradlew :interfaces:bootRun --args='--spring.profiles.active=dev'

# IntelliJ IDEA
Run Configuration > VM Options: -Dspring.profiles.active=dev

# JAR 실행
java -jar -Dspring.profiles.active=prod app.jar
```

### 3. 초기 데이터 자동 로드

서버 실행 시 `data.sql` 파일이 자동으로 실행되어 테스트용 데이터가 생성됩니다.

#### 생성되는 초기 데이터:

**매장 (3개)**
- 강남점, 홍대점, 신촌점

**사용자 및 근로자 (6명)**
| 이메일 | 비밀번호 | 역할 | 이름 | 매장 |
|--------|---------|------|------|------|
| admin@lms.com | password123 | SUPER_ADMIN | - | - |
| manager.gangnam@lms.com | password123 | MANAGER | 박수진 | 강남점 |
| employee1.gangnam@lms.com | password123 | EMPLOYEE | 김민수 | 강남점 |
| employee2.gangnam@lms.com | password123 | EMPLOYEE | 이지영 | 강남점 |
| manager.hongdae@lms.com | password123 | MANAGER | 최동현 | 홍대점 |
| employee1.hongdae@lms.com | password123 | EMPLOYEE | 정서연 | 홍대점 |

**기타 데이터**
- 급여 정책: 각 매장별 1개씩
- 근무 일정: 이번 주 일정
- 출퇴근 기록: 어제 기록
- 휴가 신청: 승인/대기/거절 각 1건씩

💡 **빠른 테스트 팁**: `admin@lms.com` / `password123` 으로 로그인하면 모든 API를 테스트할 수 있습니다!

---

## 🚀 서버 실행

### 방법 1: Gradle을 사용한 실행 (권장)
```bash
# Windows
.\gradlew :interfaces:bootRun

# Linux/macOS
./gradlew :interfaces:bootRun
```

### 방법 2: IntelliJ IDEA에서 실행
1. `interfaces/src/main/kotlin/com/lms/interfaces/LmsDemoApplication.kt` 파일 열기
2. `main` 함수 옆의 ▶️ 버튼 클릭
3. "Run 'LmsDemoApplication'" 선택

### 서버 시작 확인
서버가 정상적으로 시작되면 다음 로그가 출력됩니다:
```
Started LmsDemoApplication in X.XXX seconds
```

기본 포트: **8080**

---

## 📚 Swagger UI로 테스트

### 1. Swagger UI 접속
서버 실행 후 브라우저에서 접속:
```
http://localhost:8080/swagger-ui.html
```

### 2. Swagger UI 구성
8개의 API 도메인으로 구성:
- **인증** - 로그인, 회원가입, 토큰 갱신
- **매장 관리** - 매장 CRUD
- **근로자 관리** - 근로자 등록, 조회, 수정
- **근무 일정** - 일정 등록, 조회, 수정, 삭제
- **출퇴근 관리** - 출근, 퇴근, 기록 조회 및 수정
- **휴가 관리** - 휴가 신청, 승인, 거부, 조회
- **급여 정책** - 급여 정책 등록, 조회, 수정
- **급여 조회** - 급여 계산, 조회, 배치 실행

### 3. 인증 테스트 (필수 단계)

#### Step 1: 회원가입
1. **인증** 섹션 펼치기
2. `POST /api/auth/register` 클릭
3. "Try it out" 버튼 클릭
4. Request body 입력:
```json
{
  "email": "admin@lms.com",
  "password": "password123",
  "name": "관리자",
  "role": "SUPER_ADMIN"
}
```
5. "Execute" 클릭

#### Step 2: 로그인
1. `POST /api/auth/login` 클릭
2. "Try it out" 버튼 클릭
3. Request body 입력:
```json
{
  "email": "admin@lms.com",
  "password": "password123"
}
```
4. "Execute" 클릭
5. Response에서 `accessToken` 복사

#### Step 3: JWT 토큰 설정
1. 페이지 상단의 **Authorize** 🔓 버튼 클릭
2. Value 필드에 토큰 입력 (Bearer 접두어 제외)
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
3. "Authorize" 클릭
4. "Close" 클릭

이제 모든 인증이 필요한 API를 테스트할 수 있습니다! 🎉

### 4. 매장 생성 테스트
1. **매장 관리** 섹션의 `POST /api/stores` 클릭
2. "Try it out" 버튼 클릭
3. Request body:
```json
{
  "name": "강남점",
  "location": "서울시 강남구 테헤란로 123"
}
```
4. "Execute" 클릭
5. Response에서 `storeId` 복사 (예: `store-uuid-123`)

### 5. 근로자 등록 테스트
1. **근로자 관리** 섹션의 `POST /api/employees` 클릭
2. "Try it out" 버튼 클릭
3. Request body (위에서 복사한 storeId 사용):
```json
{
  "email": "employee@lms.com",
  "password": "password123",
  "storeId": "store-uuid-123",
  "name": "김직원",
  "phoneNumber": "010-1234-5678",
  "hourlyWage": 12000,
  "hireDate": "2024-01-01T00:00:00Z"
}
```
4. "Execute" 클릭

---

## 🌐 cURL로 테스트

### 1. 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@lms.com",
    "password": "password123"
  }'
```

Response에서 `accessToken` 복사:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "tokenType": "Bearer"
}
```

### 2. 매장 조회 (인증 필요)
```bash
# 토큰을 환경 변수로 설정
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# API 호출
curl -X GET http://localhost:8080/api/stores \
  -H "Authorization: Bearer $TOKEN"
```

### 3. 근무 일정 생성
```bash
curl -X POST http://localhost:8080/api/work-schedules \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid-123",
    "storeId": "store-uuid-123",
    "scheduledDate": "2024-02-01",
    "startTime": "09:00:00",
    "endTime": "18:00:00"
  }'
```

### 4. 출근 체크
```bash
curl -X POST http://localhost:8080/api/attendances/check-in \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid-123",
    "checkInTime": "2024-02-01T09:00:00Z"
  }'
```

---

## 📮 Postman으로 테스트

### 1. Postman Collection 가져오기
Swagger UI에서 OpenAPI 스펙 다운로드:
```
http://localhost:8080/api-docs
```

Postman에서:
1. "Import" 버튼 클릭
2. "Link" 탭 선택
3. URL 입력: `http://localhost:8080/api-docs`
4. "Continue" → "Import" 클릭

### 2. 환경 변수 설정
Postman에서 새 Environment 생성:
- `baseUrl`: `http://localhost:8080`
- `token`: (로그인 후 받은 accessToken)

### 3. Authorization 설정
1. Collection 또는 개별 Request 설정
2. "Authorization" 탭 선택
3. Type: "Bearer Token"
4. Token: `{{token}}`

---

## 🧪 통합 테스트 실행

### 전체 테스트 실행
```bash
# 전체 프로젝트 테스트
./gradlew test

# 모듈별 테스트
./gradlew :domain:test
./gradlew :application:test
./gradlew :infrastructure:test
./gradlew :interfaces:test
```

### 통합 테스트만 실행
```bash
./gradlew :interfaces:test --tests "*IntegrationTest"
```

### 테스트 커버리지 리포트
```bash
./gradlew test jacocoTestReport

# 리포트 확인
# build/reports/jacoco/test/html/index.html
```

---

## 🔍 API 엔드포인트 전체 목록

### 인증 (4개)
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃

### 매장 관리 (5개)
- `POST /api/stores` - 매장 생성
- `GET /api/stores` - 매장 목록 조회
- `GET /api/stores/{id}` - 매장 상세 조회
- `PUT /api/stores/{id}` - 매장 정보 수정
- `DELETE /api/stores/{id}` - 매장 삭제

### 근로자 관리 (5개)
- `POST /api/employees` - 근로자 등록
- `GET /api/employees` - 근로자 목록 조회
- `GET /api/employees/{id}` - 근로자 상세 조회
- `PUT /api/employees/{id}` - 근로자 정보 수정
- `DELETE /api/employees/{id}` - 근로자 삭제

### 근무 일정 (6개)
- `POST /api/work-schedules` - 일정 생성
- `GET /api/work-schedules` - 일정 목록 조회
- `GET /api/work-schedules/{id}` - 일정 상세 조회
- `PUT /api/work-schedules/{id}` - 일정 수정
- `DELETE /api/work-schedules/{id}` - 일정 삭제
- `GET /api/work-schedules/employee/{employeeId}` - 근로자별 일정 조회

### 출퇴근 관리 (5개)
- `POST /api/attendances/check-in` - 출근
- `POST /api/attendances/check-out` - 퇴근
- `GET /api/attendances` - 출퇴근 기록 조회
- `GET /api/attendances/{id}` - 출퇴근 상세 조회
- `PUT /api/attendances/{id}` - 출퇴근 기록 수정

### 휴가 관리 (7개)
- `POST /api/leave-requests` - 휴가 신청
- `GET /api/leave-requests` - 휴가 목록 조회
- `GET /api/leave-requests/{id}` - 휴가 상세 조회
- `PUT /api/leave-requests/{id}` - 휴가 신청 수정
- `DELETE /api/leave-requests/{id}` - 휴가 신청 취소
- `POST /api/leave-requests/{id}/approve` - 휴가 승인
- `POST /api/leave-requests/{id}/reject` - 휴가 거부

### 급여 정책 (6개)
- `POST /api/payroll-policies` - 급여 정책 등록
- `GET /api/payroll-policies` - 급여 정책 목록 조회
- `GET /api/payroll-policies/{id}` - 급여 정책 상세 조회
- `PUT /api/payroll-policies/{id}` - 급여 정책 수정
- `DELETE /api/payroll-policies/{id}` - 급여 정책 삭제
- `GET /api/payroll-policies/store/{storeId}` - 매장별 급여 정책 조회

### 급여 조회 (6개)
- `GET /api/payrolls` - 급여 목록 조회
- `GET /api/payrolls/{id}` - 급여 상세 조회
- `GET /api/payrolls/employee/{employeeId}` - 근로자별 급여 조회
- `GET /api/payrolls/calculate/{employeeId}` - 급여 계산
- `POST /api/payrolls/batch` - 일괄 급여 계산
- `POST /api/payrolls/batch/execute` - 배치 급여 산정 실행

---

## 🎯 시나리오 기반 테스트

### 시나리오 1: 신규 매장 및 근로자 등록
```bash
# 1. SUPER_ADMIN으로 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@lms.com", "password": "password123"}'

# 2. 매장 생성
curl -X POST http://localhost:8080/api/stores \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "강남점", "location": "서울시 강남구"}'

# 3. 근로자 등록
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "employee@lms.com",
    "password": "password123",
    "storeId": "store-uuid",
    "name": "김직원",
    "phoneNumber": "010-1234-5678",
    "hourlyWage": 12000,
    "hireDate": "2024-01-01T00:00:00Z"
  }'
```

### 시나리오 2: 근무 일정 등록 및 출퇴근
```bash
# 1. 근무 일정 생성
curl -X POST http://localhost:8080/api/work-schedules \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid",
    "storeId": "store-uuid",
    "scheduledDate": "2024-02-01",
    "startTime": "09:00:00",
    "endTime": "18:00:00"
  }'

# 2. 출근 체크
curl -X POST http://localhost:8080/api/attendances/check-in \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid",
    "checkInTime": "2024-02-01T09:00:00Z"
  }'

# 3. 퇴근 체크
curl -X POST http://localhost:8080/api/attendances/check-out \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid",
    "checkOutTime": "2024-02-01T18:30:00Z"
  }'
```

### 시나리오 3: 휴가 신청 및 승인
```bash
# 1. 휴가 신청
curl -X POST http://localhost:8080/api/leave-requests \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "employee-uuid",
    "leaveType": "ANNUAL",
    "startDate": "2024-03-01",
    "endDate": "2024-03-03",
    "reason": "개인 사유"
  }'

# 2. 휴가 승인 (MANAGER/SUPER_ADMIN)
curl -X POST http://localhost:8080/api/leave-requests/{id}/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

### 시나리오 4: 급여 계산
```bash
# 1. 급여 정책 등록
curl -X POST http://localhost:8080/api/payroll-policies \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "store-uuid",
    "baseHourlyWage": 10000,
    "overtimeRate": 1.5,
    "nightShiftRate": 1.5,
    "weekendRate": 1.5,
    "holidayRate": 2.0
  }'

# 2. 근로자 급여 계산
curl -X GET "http://localhost:8080/api/payrolls/calculate/employee-uuid?year=2024&month=2" \
  -H "Authorization: Bearer $TOKEN"

# 3. 급여 일괄 계산
curl -X POST http://localhost:8080/api/payrolls/batch \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "store-uuid",
    "year": 2024,
    "month": 2
  }'
```

---

## 🐛 트러블슈팅

### 1. 서버가 시작되지 않음
**증상**: `Port 8080 is already in use`
```bash
# Windows에서 포트 8080 사용 중인 프로세스 확인 및 종료
netstat -ano | findstr :8080
taskkill /PID [PID번호] /F

# Linux/macOS
lsof -ti:8080 | xargs kill -9
```

### 2. 데이터베이스 연결 실패
**증상**: `Communications link failure`

확인 사항:
- MySQL 서버 실행 중인지 확인
- `.env` 파일의 DB 연결 정보 확인
- 방화벽 설정 확인

### 3. 401 Unauthorized 에러
**원인**: JWT 토큰이 만료되었거나 잘못됨

해결 방법:
- 다시 로그인하여 새로운 토큰 발급
- Swagger UI에서 Authorize 버튼으로 토큰 재설정

### 4. 403 Forbidden 에러
**원인**: 권한 부족

해결 방법:
- 해당 API에 필요한 권한(Role) 확인
- SUPER_ADMIN, MANAGER, EMPLOYEE 중 적절한 권한으로 로그인

---

## 📊 로그 확인

### 애플리케이션 로그
서버 실행 중 콘솔에서 확인:
- SQL 쿼리: `org.hibernate.SQL: DEBUG`
- API 요청/응답: `com.lms: DEBUG`
- Spring Security: `org.springframework.security: DEBUG`

### 로그 레벨 변경
`application.yml` 파일 수정:
```yaml
logging:
  level:
    root: INFO
    com.lms: DEBUG  # TRACE, DEBUG, INFO, WARN, ERROR
```

---

## 🎉 다음 단계

백엔드 API 테스트를 마치셨다면:
1. **프론트엔드 개발**: Flutter 모바일 앱 또는 웹 어드민 구현
2. **배포**: Docker 컨테이너화 및 클라우드 배포
3. **모니터링**: 로그 수집 및 메트릭 모니터링 설정

---

**작성일**: 2026-01-17
**작성자**: Claude Code + TaskMaster AI
