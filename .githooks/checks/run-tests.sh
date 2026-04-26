#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$REPO_ROOT/.githooks/lib/log.sh"

HOOK="CHECK:test"

echo_log "$HOOK" "🔍 테스트 및 커버리지 체크 중..."

cd "$REPO_ROOT" || exit 1

TMPFILE=$(mktemp)
./gradlew test jacocoTestReport jacocoTestCoverageVerification 2>&1 | tee "$TMPFILE"
GRADLE_EXIT=${PIPESTATUS[0]}

if [ $GRADLE_EXIT -ne 0 ]; then
    log_fail "$HOOK" "커버리지 80% 미달"
    log_error "$HOOK" "커버리지 80% 미달" "$(cat "$TMPFILE")"
    rm -f "$TMPFILE"
    echo_log "$HOOK" "❌ 커버리지 80% 미달! 테스트를 보완하고 다시 커밋해주세요."
    exit 1
fi

rm -f "$TMPFILE"
echo_log "$HOOK" "✅ 커버리지 체크 통과!"
exit 0
