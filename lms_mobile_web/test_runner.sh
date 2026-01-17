#!/bin/bash

# Flutter UI 테스트 자동화 스크립트
# 사용법: ./test_runner.sh [옵션]
# 옵션: all, unit, widget, integration, golden, analyze

set -e  # 에러 발생 시 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🚀 Flutter 테스트 자동화 시작..."

# 의존성 설치
echo "📦 의존성 설치 중..."
flutter pub get

# 코드 분석
if [ "$1" == "all" ] || [ "$1" == "analyze" ] || [ -z "$1" ]; then
    echo ""
    echo "🔍 코드 분석 중..."
    if flutter analyze; then
        echo -e "${GREEN}✅ 코드 분석 통과${NC}"
    else
        echo -e "${RED}❌ 코드 분석 실패${NC}"
        exit 1
    fi
fi

# 단위 테스트
if [ "$1" == "all" ] || [ "$1" == "unit" ] || [ -z "$1" ]; then
    echo ""
    echo "🧪 단위 테스트 실행 중..."
    if flutter test test/ --exclude-tags=golden,integration; then
        echo -e "${GREEN}✅ 단위 테스트 통과${NC}"
    else
        echo -e "${RED}❌ 단위 테스트 실패${NC}"
        exit 1
    fi
fi

# Golden 테스트
if [ "$1" == "all" ] || [ "$1" == "golden" ]; then
    echo ""
    echo "🖼️  Golden 테스트 실행 중..."
    if flutter test test/golden_test.dart; then
        echo -e "${GREEN}✅ Golden 테스트 통과${NC}"
    else
        echo -e "${YELLOW}⚠️  Golden 파일 변경 감지됨${NC}"
        echo "Golden 파일을 업데이트하려면: flutter test --update-goldens"
    fi
fi

# 통합 테스트
if [ "$1" == "all" ] || [ "$1" == "integration" ]; then
    echo ""
    echo "🔗 통합 테스트 실행 중..."
    if flutter test integration_test/; then
        echo -e "${GREEN}✅ 통합 테스트 통과${NC}"
    else
        echo -e "${RED}❌ 통합 테스트 실패${NC}"
        exit 1
    fi
fi

# 레이아웃 오버플로우 체크
if [ "$1" == "all" ] || [ -z "$1" ]; then
    echo ""
    echo "📐 레이아웃 오버플로우 체크 중..."
    flutter test --reporter=json > test_results.json 2>&1 || true

    if grep -q "RenderFlex overflowed" test_results.json; then
        echo -e "${RED}❌ 레이아웃 오버플로우 에러 발견!${NC}"
        grep "RenderFlex overflowed" test_results.json
        rm test_results.json
        exit 1
    else
        echo -e "${GREEN}✅ 레이아웃 오버플로우 없음${NC}"
        rm test_results.json
    fi
fi

echo ""
echo -e "${GREEN}✨ 모든 테스트 통과!${NC}"
