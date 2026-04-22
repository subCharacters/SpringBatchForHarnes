#!/bin/bash

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | grep -o '"file_path":"[^"]*"' | sed 's/"file_path":"//;s/"//')

if echo "$FILE_PATH" | grep -q '\.worktrees/'; then
  exit 0
fi

echo "❌ 원본 저장소에서 직접 작업 금지! worktree를 먼저 생성하세요." >&2
echo "👉 git worktree add .worktrees/<작업명> -b <브랜치명>" >&2
exit 2
