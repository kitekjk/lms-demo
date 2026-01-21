# 로컬 MySQL 설정 가이드

Docker를 사용하지 않고 로컬에 설치된 MySQL을 사용하는 경우의 설정 가이드입니다.

## 필요 조건

- MySQL 8.0 이상
- MySQL 클라이언트 (MySQL Workbench, DBeaver, 또는 CLI)

## 설정 방법

### 1. MySQL 접속

```bash
# CLI로 접속
mysql -u root -p

# 또는 MySQL Workbench, DBeaver 등 사용
```

### 2. 데이터베이스 생성

```sql
-- 데이터베이스 생성 (UTF8MB4 문자셋)
CREATE DATABASE lms_demo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 생성 확인
SHOW DATABASES LIKE 'lms_demo';
```

### 3. 사용자 계정 설정

LMS 전용 계정을 생성합니다. (root 계정 대신 전용 계정 사용 권장)

```sql
-- 전용 사용자 생성
CREATE USER 'lms'@'localhost' IDENTIFIED BY 'lms1234';

-- 권한 부여
GRANT ALL PRIVILEGES ON lms_demo.* TO 'lms'@'localhost';

-- 권한 적용
FLUSH PRIVILEGES;

-- 확인
SHOW GRANTS FOR 'lms'@'localhost';
```

이 설정은 `application-local.yml`의 기본 설정과 일치합니다.

### 4. 연결 테스트

```sql
-- 데이터베이스 선택
USE lms_demo;

-- 테이블 목록 확인 (처음에는 비어있음)
SHOW TABLES;
```

### 5. 서버 실행

```bash
# 프로젝트 루트에서
./gradlew :interfaces:bootRun
```

서버가 시작되면 자동으로:
1. 테이블이 생성됩니다 (`ddl-auto: create-drop`)
2. 초기 데이터가 로드됩니다 (`data.sql`)

---

## 설정 요약

### 기본 설정 (변경 없이 사용)

| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 3306 |
| Database | lms_demo |
| Username | lms |
| Password | lms1234 |

### 커스텀 설정

환경 변수 또는 `application-local.yml`에서 변경 가능:

```yaml
spring:
  datasource:
    url: jdbc:mysql://YOUR_HOST:YOUR_PORT/YOUR_DATABASE
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```

---

## 포트 변경이 필요한 경우

로컬 MySQL이 다른 포트(예: 3307)에서 실행 중인 경우:

```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/lms_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

또는 `.env` 파일:

```env
DB_URL=jdbc:mysql://localhost:3307/lms_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

## 트러블슈팅

### "Access denied for user" 오류

```sql
-- 사용자 권한 확인
SELECT user, host FROM mysql.user;

-- lms 계정이 없으면 생성
CREATE USER 'lms'@'localhost' IDENTIFIED BY 'lms1234';
GRANT ALL PRIVILEGES ON lms_demo.* TO 'lms'@'localhost';
FLUSH PRIVILEGES;

-- 비밀번호 재설정 (계정이 이미 있는 경우)
ALTER USER 'lms'@'localhost' IDENTIFIED BY 'lms1234';
FLUSH PRIVILEGES;
```

### "Unknown database 'lms_demo'" 오류

```sql
-- 데이터베이스 생성
CREATE DATABASE lms_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### "Public Key Retrieval is not allowed" 오류

JDBC URL에 `allowPublicKeyRetrieval=true` 파라미터가 있는지 확인:

```
jdbc:mysql://localhost:3306/lms_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

### 한글 깨짐 문제

```sql
-- 데이터베이스 문자셋 확인
SHOW CREATE DATABASE lms_demo;

-- UTF8MB4가 아닌 경우 재생성
DROP DATABASE lms_demo;
CREATE DATABASE lms_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### MySQL 서비스 시작/중지

**Windows:**
```bash
# 서비스 시작
net start MySQL80

# 서비스 중지
net stop MySQL80
```

**macOS (Homebrew):**
```bash
# 시작
brew services start mysql

# 중지
brew services stop mysql
```

**Linux:**
```bash
# 시작
sudo systemctl start mysql

# 중지
sudo systemctl stop mysql
```

---

## 데이터 초기화

테이블과 데이터를 완전히 초기화하려면:

```sql
-- 데이터베이스 삭제 후 재생성
DROP DATABASE lms_demo;
CREATE DATABASE lms_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

서버를 재시작하면 테이블과 초기 데이터가 다시 생성됩니다.

---

## Docker vs 로컬 MySQL 비교

| 항목 | Docker Compose | 로컬 MySQL |
|------|----------------|------------|
| 설치 복잡도 | 낮음 (docker-compose up) | 중간 (MySQL 설치 필요) |
| 환경 일관성 | 높음 | 환경마다 다를 수 있음 |
| 포트 충돌 | docker-compose.yml에서 변경 | 설정 파일에서 변경 |
| 데이터 초기화 | `docker-compose down -v` | DROP DATABASE |
| 리소스 사용 | Docker 오버헤드 | 직접 실행으로 가벼움 |

**권장사항:**
- 빠른 시작: Docker Compose 사용
- 이미 MySQL 설치됨: 로컬 MySQL 사용
- 팀 개발: Docker Compose (환경 일관성)
