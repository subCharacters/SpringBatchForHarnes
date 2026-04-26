#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$REPO_ROOT/.githooks/lib/log.sh"

HOOK="APPLY:format"
JAR="$REPO_ROOT/libs/google-java-format-1.35.0-all-deps.jar"
JVM_ARGS=(
    "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED"
    "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"
    "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED"
    "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED"
    "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED"
    "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
)

echo_log "$HOOK" "🔍 Google Java Format 검사 중..."

mapfile -t JAVA_FILES < <(find "$REPO_ROOT/src" -name "*.java" | sort)

if [ ${#JAVA_FILES[@]} -eq 0 ]; then
    echo_log "$HOOK" "⏭️  Java 파일 없음 - 스킵"
    exit 0
fi

CHANGED=$(java "${JVM_ARGS[@]}" -jar "$JAR" --dry-run "${JAVA_FILES[@]}" 2>/dev/null)

if [ -z "$CHANGED" ]; then
    echo_log "$HOOK" "✅ 포맷 검사 통과!"
    exit 0
fi

java "${JVM_ARGS[@]}" -jar "$JAR" --replace "${JAVA_FILES[@]}" 2>/dev/null

echo_log "$HOOK" "📝 포맷 변경된 파일:"
while IFS= read -r file; do
    echo_log "$HOOK" "   - $file"
done <<< "$CHANGED"

MSG="포맷이 변경됐습니다. 변경된 파일을 확인 후 다시 add하고 커밋하세요"
log_fail "$HOOK" "$MSG"
echo_log "$HOOK" "❌ $MSG"
exit 1
