#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$REPO_ROOT/.githooks/lib/log.sh"

HOOK="SPOTBUGS"
REPORT_XML="$REPO_ROOT/build/reports/spotbugs/main.xml"
REPORT_HTML="$REPO_ROOT/build/reports/spotbugs/main.html"

echo_log "$HOOK" "🔍 SpotBugs 정적 분석 중..."

cd "$REPO_ROOT" || exit 1

# 이전 리포트 삭제 — 스탈레 결과 방지
rm -f "$REPORT_XML"

TMPFILE=$(mktemp)
./gradlew spotbugsMain 2>&1 | tee "$TMPFILE"

# 리포트 미생성 = Gradle 자체 실패 (컴파일 에러 등)
if [ ! -f "$REPORT_XML" ]; then
    log_fail "$HOOK" "Gradle 빌드 실패 (SpotBugs 리포트 미생성)"
    log_error "$HOOK" "Gradle 빌드 실패" "$(cat "$TMPFILE")"
    rm -f "$TMPFILE"
    echo_log "$HOOK" "❌ Gradle 빌드 실패! 컴파일 에러를 확인하세요."
    exit 1
fi
rm -f "$TMPFILE"

# 버그 수 집계 (priority: 1=HIGH, 2=MEDIUM, 3=LOW)
HIGH_COUNT=$(grep -c 'priority="1"' "$REPORT_XML" 2>/dev/null || true)
MEDIUM_COUNT=$(grep -c 'priority="2"' "$REPORT_XML" 2>/dev/null || true)
LOW_COUNT=$(grep -c 'priority="3"' "$REPORT_XML" 2>/dev/null || true)
HIGH_COUNT=${HIGH_COUNT:-0}
MEDIUM_COUNT=${MEDIUM_COUNT:-0}
LOW_COUNT=${LOW_COUNT:-0}
HIGH_MEDIUM=$((HIGH_COUNT + MEDIUM_COUNT))

# BugInstance XML 파싱 (awk 전용)
# 인자: $1=XML 파일  $2=필터("le2"=HIGH+MEDIUM, "eq3"=LOW만)
# 각 버그를 "[LEVEL] TYPE (CATEGORY) / 메시지 / 위치" 형식으로 출력
_parse_bugs() {
    awk -v filter="$2" '
    function attr(s, name,    p, r) {
        p = index(s, name "=\"")
        if (!p) return ""
        r = substr(s, p + length(name) + 2)
        return substr(r, 1, index(r, "\"") - 1)
    }
    /^  <BugInstance / {
        prio = attr($0, "priority") + 0
        want = (filter == "le2") ? (prio <= 2) : (prio == 3)
        if (want) {
            btype = attr($0, "type")
            cat   = attr($0, "category")
            level = (prio == 1) ? "HIGH" : (prio == 2) ? "MEDIUM" : "LOW"
            msg = ""; src = ""; in_bug = 1
        } else {
            in_bug = 0
        }
        next
    }
    !in_bug { next }
    /^    <LongMessage>/ {
        msg = $0
        sub(/^[ \t]*<LongMessage>/, "", msg)
        sub(/<\/LongMessage>.*/, "", msg)
        next
    }
    /^    <SourceLine / && src == "" {
        sp = attr($0, "sourcepath")
        st = attr($0, "start")
        if (sp) src = sp ":" st
        next
    }
    /^  <\/BugInstance>/ {
        in_bug = 0
        print "   [" level "] " btype " (" cat ")"
        print "          " msg
        if (src) print "          위치: " src
        next
    }
    ' "$1"
}

# HIGH/MEDIUM 버그 발견 시 상세 출력 후 차단
if [ "$HIGH_MEDIUM" -gt 0 ]; then
    log_fail "$HOOK" "SpotBugs HIGH/MEDIUM 버그 발견: HIGH=${HIGH_COUNT}, MEDIUM=${MEDIUM_COUNT}"
    echo_log "$HOOK" "❌ SpotBugs HIGH/MEDIUM 버그 ${HIGH_MEDIUM}건 발견! (HIGH=${HIGH_COUNT}, MEDIUM=${MEDIUM_COUNT})"
    echo_log "$HOOK" ""
    echo_log "$HOOK" "📋 버그 상세 목록:"
    while IFS= read -r line; do
        echo_log "$HOOK" "$line"
    done < <(_parse_bugs "$REPORT_XML" "le2")
    echo_log "$HOOK" ""
    echo_log "$HOOK" "   리포트 확인: $REPORT_HTML"
    log_error "$HOOK" "SpotBugs HIGH/MEDIUM 버그 차단" "HIGH=${HIGH_COUNT}, MEDIUM=${MEDIUM_COUNT}, 리포트: $REPORT_HTML"
    exit 1
fi

# LOW 경고만 있는 경우 로그 출력 후 통과
if [ "$LOW_COUNT" -gt 0 ]; then
    log_info "$HOOK" "SpotBugs LOW 경고 ${LOW_COUNT}건 발견 (통과)"
    echo_log "$HOOK" "⚠️  SpotBugs LOW 경고 ${LOW_COUNT}건 (통과됩니다)"
    echo_log "$HOOK" ""
    echo_log "$HOOK" "📋 LOW 경고 목록:"
    while IFS= read -r line; do
        echo_log "$HOOK" "$line"
    done < <(_parse_bugs "$REPORT_XML" "eq3")
    echo_log "$HOOK" ""
    echo_log "$HOOK" "   리포트 확인: $REPORT_HTML"
fi

echo_log "$HOOK" "✅ SpotBugs 검사 통과!"
exit 0
