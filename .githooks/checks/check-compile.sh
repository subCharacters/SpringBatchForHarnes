#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$REPO_ROOT/.githooks/lib/log.sh"

HOOK="CHECK:compile"

echo_log "$HOOK" "🔍 컴파일 체크 중..."

cd "$REPO_ROOT" || exit 1

TMPFILE=$(mktemp)
./gradlew compileJava compileTestJava 2>&1 | tee "$TMPFILE"
GRADLE_EXIT=${PIPESTATUS[0]}

if [ $GRADLE_EXIT -ne 0 ]; then
    log_fail "$HOOK" "컴파일 실패"
    log_error "$HOOK" "컴파일 실패" "$(cat "$TMPFILE")"
    rm -f "$TMPFILE"
    echo_log "$HOOK" "❌ 컴파일 실패! 코드를 확인하세요."
    exit 1
fi

rm -f "$TMPFILE"
echo_log "$HOOK" "✅ 컴파일 통과!"
exit 0
