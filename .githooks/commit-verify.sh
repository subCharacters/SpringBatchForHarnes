#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/lib/log.sh"

HOOK="COMMIT-VERIFY"

CHECKS=(
    "$SCRIPT_DIR/checks/apply-format.sh"
    "$SCRIPT_DIR/checks/check-style.sh"
    "$SCRIPT_DIR/checks/check-compile.sh"
    "$SCRIPT_DIR/checks/analyze-bugs.sh"
    "$SCRIPT_DIR/checks/run-tests.sh"
)

echo_log "$HOOK" "🚀 코드 품질 검사 시작..."

for check in "${CHECKS[@]}"; do
    bash "$check"
    if [ $? -ne 0 ]; then
        log_fail "$HOOK" "검사 실패: $(basename "$check")"
        echo_log "$HOOK" "❌ 검사 중단: $(basename "$check") 실패"
        exit 1
    fi
done

log_success "$HOOK" "모든 코드 품질 검사 통과"
echo_log "$HOOK" "✅ 모든 코드 품질 검사 통과!"
exit 0
