-- 초기 데이터 생성 스크립트
-- Entity 스키마에 맞게 수정됨

-- 1. 매장 생성
INSERT INTO stores (store_id, name, location, created_at, updated_at)
VALUES
    ('store-001', '강남점', '서울시 강남구 테헤란로 123', NOW(), NOW()),
    ('store-002', '홍대점', '서울시 마포구 홍익로 456', NOW(), NOW()),
    ('store-003', '신촌점', '서울시 서대문구 신촌역로 789', NOW(), NOW());

-- 2. 사용자 생성 (비밀번호: password123)
-- BCrypt 해시: $2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS
INSERT INTO users (user_id, email, password, role, is_active, last_login_at, created_at, updated_at)
VALUES
    -- SUPER_ADMIN
    ('user-admin', 'admin@lms.com', '$2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS', 'SUPER_ADMIN', true, NULL, NOW(), NOW()),

    -- 강남점 MANAGER
    ('user-manager-001', 'manager.gangnam@lms.com', '$2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS', 'MANAGER', true, NULL, NOW(), NOW()),

    -- 강남점 EMPLOYEE
    ('user-emp-001', 'employee1.gangnam@lms.com', '$2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS', 'EMPLOYEE', true, NULL, NOW(), NOW()),
    ('user-emp-002', 'employee2.gangnam@lms.com', '$2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS', 'EMPLOYEE', true, NULL, NOW(), NOW()),

    -- 홍대점 MANAGER
    ('user-manager-002', 'manager.hongdae@lms.com', '$2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS', 'MANAGER', true, NULL, NOW(), NOW()),

    -- 홍대점 EMPLOYEE
    ('user-emp-003', 'employee1.hongdae@lms.com', '$2a$10$EfRjwpUmoHURFezK9f2Z2eLe1rCpDUBy5BGVrhjWqC4BYOhDf4FFS', 'EMPLOYEE', true, NULL, NOW(), NOW());

-- 3. 근로자 정보 생성
INSERT INTO employees (employee_id, user_id, name, employee_type, store_id, remaining_leave, is_active, created_at, updated_at)
VALUES
    -- 강남점 매니저
    ('emp-manager-001', 'user-manager-001', '박수진', 'REGULAR', 'store-001', 15.0, true, NOW(), NOW()),

    -- 강남점 직원
    ('emp-001', 'user-emp-001', '김민수', 'REGULAR', 'store-001', 13.5, true, NOW(), NOW()),
    ('emp-002', 'user-emp-002', '이지영', 'REGULAR', 'store-001', 14.0, true, NOW(), NOW()),

    -- 홍대점 매니저
    ('emp-manager-002', 'user-manager-002', '최동현', 'REGULAR', 'store-002', 15.0, true, NOW(), NOW()),

    -- 홍대점 직원
    ('emp-003', 'user-emp-003', '정서연', 'REGULAR', 'store-002', 15.0, true, NOW(), NOW());

-- 4. 급여 정책 생성
INSERT INTO payroll_policies (payroll_policy_id, policy_type, multiplier, effective_from, effective_to, description, created_at, updated_at)
VALUES
    ('policy-001', 'OVERTIME', 1.5, '2024-01-01', NULL, '초과근무 가산율', NOW(), NOW()),
    ('policy-002', 'NIGHT_SHIFT', 1.5, '2024-01-01', NULL, '야간근무 가산율', NOW(), NOW()),
    ('policy-003', 'WEEKEND', 1.5, '2024-01-01', NULL, '주말근무 가산율', NOW(), NOW()),
    ('policy-004', 'HOLIDAY', 2.0, '2024-01-01', NULL, '공휴일근무 가산율', NOW(), NOW());
