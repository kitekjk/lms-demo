#!/bin/bash

# 로컬 UI 테스트 - 실시간 모니터링
# 코드 변경사항을 감지하고 자동으로 테스트 실행

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clear
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}     🎯 Flutter UI 테스트 - 로컬 모니터링 모드     ${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 모드 선택
echo "테스트 모드를 선택하세요:"
echo "  1) Watch 모드 - 파일 변경 시 자동 재실행"
echo "  2) 한번만 실행"
echo "  3) 레이아웃 오버플로우만 체크"
echo "  4) Golden 테스트 (UI 스냅샷 비교)"
echo ""
read -p "선택 (1-4): " mode

case $mode in
  1)
    echo -e "\n${YELLOW}📡 Watch 모드 시작...${NC}"
    echo "파일이 변경되면 자동으로 테스트가 재실행됩니다."
    echo "종료하려면 Ctrl+C를 누르세요."
    echo ""

    # Watch 모드로 테스트 실행
    flutter test --watch
    ;;

  2)
    echo -e "\n${BLUE}🧪 테스트 실행 중...${NC}\n"
    flutter test

    if [ $? -eq 0 ]; then
      echo -e "\n${GREEN}✅ 모든 테스트 통과!${NC}"
    else
      echo -e "\n${RED}❌ 테스트 실패${NC}"
      exit 1
    fi
    ;;

  3)
    echo -e "\n${BLUE}📐 레이아웃 오버플로우 체크 중...${NC}\n"

    # 테스트 실행하고 결과를 파일에 저장
    flutter test --reporter=json > test_results.json 2>&1 || true

    # 오버플로우 에러 검색
    if grep -q "RenderFlex overflowed" test_results.json; then
      echo -e "${RED}❌ 레이아웃 오버플로우 발견!${NC}\n"
      echo "발견된 오버플로우:"
      grep -A 3 "RenderFlex overflowed" test_results.json | sed 's/^/  /'
      rm test_results.json
      exit 1
    else
      echo -e "${GREEN}✅ 레이아웃 오버플로우 없음${NC}"
      rm test_results.json
    fi
    ;;

  4)
    echo -e "\n${BLUE}🖼️  Golden 테스트 옵션:${NC}"
    echo "  1) Golden 파일과 비교 (변경사항 감지)"
    echo "  2) Golden 파일 업데이트 (새 스냅샷 생성)"
    echo ""
    read -p "선택 (1-2): " golden_mode

    if [ "$golden_mode" == "1" ]; then
      echo -e "\n${BLUE}🔍 Golden 테스트 실행 중...${NC}\n"
      flutter test test/golden_test.dart

      if [ $? -eq 0 ]; then
        echo -e "\n${GREEN}✅ UI 변경사항 없음${NC}"
      else
        echo -e "\n${YELLOW}⚠️  UI 변경 감지됨!${NC}"
        echo "변경사항을 승인하려면:"
        echo "  ./local_ui_test.sh 선택 4 → 선택 2"
      fi
    else
      echo -e "\n${YELLOW}📸 Golden 파일 업데이트 중...${NC}\n"
      flutter test --update-goldens test/golden_test.dart
      echo -e "\n${GREEN}✅ Golden 파일 업데이트 완료${NC}"
      echo "변경사항:"
      git diff test/goldens/ | head -20
    fi
    ;;

  *)
    echo -e "${RED}잘못된 선택입니다.${NC}"
    exit 1
    ;;
esac

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
