#!/bin/bash

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"
source "$REPO_ROOT/.githooks/lib/log.sh"

HOOK="CLAUDE:PreToolUse"

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | grep -o '"file_path":"[^"]*"' | sed 's/"file_path":"//;s/"//')

echo "🔍 worktree 체크 중... ($FILE_PATH)" >&2
if echo "$FILE_PATH" | grep -q '\.worktrees/'; then
  echo "✅ worktree 확인됨" >&2
  exit 0
fi

log_fail "$HOOK" "원본 저장소 직접 작업 시도 차단: $FILE_PATH"
log_error "$HOOK" "원본 저장소 직접 작업 차단" "file_path: $FILE_PATH"
echo "❌ 원본 저장소에서 직접 작업 금지! worktree를 먼저 생성하세요." >&2
echo "👉 git worktree add .worktrees/<작업명> -b <브랜치명>" >&2
exit 2
