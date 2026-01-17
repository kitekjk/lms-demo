# 🔐 보안 설정 가이드

이 문서는 프로젝트의 보안 설정과 민감 정보 관리 방법을 설명합니다.

## 🚨 OAuth 토큰이 유출된 경우

Git에 OAuth 토큰이 실수로 커밋된 경우 즉시 조치가 필요합니다.

### 1️⃣ 기존 토큰 무효화 및 새 토큰 발급

```bash
# Claude Code 로그아웃 (기존 토큰 무효화)
claude logout

# 재로그인 (새 토큰 자동 발급)
claude login
```

### 2️⃣ 사용자 레벨 MCP 설정 (권장)

프로젝트별 `.mcp.json` 파일 대신 사용자 레벨 설정을 사용하세요:

```bash
# TaskMaster AI MCP 서버 추가
claude mcp add task-master-ai --scope user \
  --env TASK_MASTER_TOOLS="core" \
  -- npx -y task-master-ai@latest

# 설정 확인
claude mcp list
```

**장점:**
- ✅ OAuth 토큰이 사용자 레벨에서 안전하게 관리됨
- ✅ 프로젝트별로 `.mcp.json` 파일 관리 불필요
- ✅ 한 번 설정하면 모든 프로젝트에서 사용 가능
- ✅ Git에 민감 정보가 포함되지 않음

### 3️⃣ Git 히스토리에서 민감 정보 제거 (선택사항)

**⚠️ 주의: 이 작업은 Git 히스토리를 재작성하므로 팀원과 협의 필요**

#### 방법 A: git-filter-repo 사용 (권장)

```bash
# 1. git-filter-repo 설치
pip install git-filter-repo

# 2. 민감 파일 히스토리에서 제거
git filter-repo --path .mcp.json --invert-paths

# 3. 강제 푸시
git push --force
```

#### 방법 B: BFG Repo-Cleaner 사용

```bash
# 1. BFG 다운로드
# https://rtyley.github.io/bfg-repo-cleaner/

# 2. 저장소 미러 클론
git clone --mirror https://github.com/your-username/lms-demo.git lms-demo-backup.git

# 3. 민감 정보 파일 제거
java -jar bfg.jar --delete-files .mcp.json lms-demo-backup.git

# 4. Git GC 실행
cd lms-demo-backup.git
git reflog expire --expire=now --all && git gc --prune=now --aggressive

# 5. 강제 푸시
git push --force
```

#### 팀원 대응

강제 푸시 후 모든 팀원은 저장소를 재클론해야 합니다:

```bash
# 기존 저장소 백업
mv lms-demo lms-demo-backup

# 새로 클론
git clone https://github.com/your-username/lms-demo.git
cd lms-demo

# 환경 재설정
cp .env.example .env
# .env 파일 편집

# MCP 재설정
claude mcp add task-master-ai --scope user \
  --env TASK_MASTER_TOOLS="core" \
  -- npx -y task-master-ai@latest
```

## 🔑 환경 변수 관리

### 로컬 개발 환경

`.env` 파일을 사용하여 환경별 설정을 관리합니다:

```bash
# .env.example 복사
cp .env.example .env

# .env 파일 편집
vim .env
```

**절대 Git에 포함하지 말 것:**
- `.env` - 환경 변수
- `.mcp.json` - MCP OAuth 토큰 (더 이상 사용 안 함)
- `.taskmaster/config.json` - TaskMaster API 키

### 프로덕션 환경

프로덕션 환경에서는 환경 변수를 시스템 레벨에서 설정:

```bash
# Linux/macOS
export DATABASE_URL="postgresql://..."
export JWT_SECRET="..."

# Docker
docker run -e DATABASE_URL="..." -e JWT_SECRET="..." ...

# Kubernetes
# ConfigMap, Secret 사용
```

## 📋 보안 체크리스트

프로젝트 설정 시 확인할 사항:

- [ ] `.gitignore`에 `.env`, `.mcp.json`, `.taskmaster/config.json` 포함 확인
- [ ] `.env.example`에 실제 값이 아닌 템플릿만 포함
- [ ] Claude MCP를 사용자 레벨(`--scope user`)로 설정
- [ ] Git 히스토리에 민감 정보 없는지 확인: `git log --all --full-history --source -- .env .mcp.json`
- [ ] TaskMaster API 키를 사용자 레벨에서 관리 (`task-master models --setup`)

## 🔍 민감 정보 감지

GitHub는 자동으로 공개 저장소의 민감 정보를 스캔합니다:

1. GitHub Repository → Settings → Security → Secret scanning
2. 알림이 있으면 즉시 조치
3. 정기적으로 확인하여 유출 방지

## 📚 추가 자료

- [Claude Code MCP 문서](https://docs.anthropic.com/claude/docs/claude-code)
- [Git 민감 정보 제거 가이드](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [TaskMaster AI 문서](https://www.npmjs.com/package/task-master-ai)

---

**마지막 업데이트:** 2026-01-17
