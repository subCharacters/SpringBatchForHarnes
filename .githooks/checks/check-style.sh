#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$REPO_ROOT/.githooks/lib/log.sh"

HOOK="CHECK:style"

echo_log "$HOOK" "🔍 Checkstyle 검사 중..."

cd "$REPO_ROOT" || exit 1

TMPFILE=$(mktemp)
./gradlew checkstyleMain checkstyleTest 2>&1 | tee "$TMPFILE"
EXIT_CODE=${PIPESTATUS[0]}

if [ $EXIT_CODE -ne 0 ]; then
    log_fail "$HOOK" "Checkstyle 검사 실패"
    log_error "$HOOK" "Checkstyle 검사 실패" "$(cat "$TMPFILE")"
    rm -f "$TMPFILE"
    echo_log "$HOOK" "❌ Checkstyle 위반 발생! build/reports/checkstyle/ 에서 상세 내용을 확인하세요."
    exit 1
fi

WARNINGS=$(grep -E "\[WARN\]" "$TMPFILE" || true)

if [ -n "$WARNINGS" ]; then
    log_fail "$HOOK" "Checkstyle 위반 발견"
    echo_log "$HOOK" "📋 Checkstyle 위반 목록:"
    while IFS= read -r line; do
        echo_log "$HOOK" "   $line"
    done <<< "$WARNINGS"
    rm -f "$TMPFILE"
    echo_log "$HOOK" "❌ Checkstyle 위반이 있습니다. 위반 항목을 수정 후 다시 커밋하세요."
    exit 1
fi

rm -f "$TMPFILE"
echo_log "$HOOK" "✅ Checkstyle 검사 통과!"
exit 0
