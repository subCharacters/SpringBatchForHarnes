#!/bin/bash

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"
LOG_DIR="$REPO_ROOT/.logs"
LOG_FILE="$LOG_DIR/hooks.log"
ERROR_FILE="$LOG_DIR/error.log"
MAX_LOG_SIZE=$((10 * 1024 * 1024))  # 10MB

_LOG_INITIALIZED=0

_log_init() {
  [ "$_LOG_INITIALIZED" -eq 1 ] && return
  _LOG_INITIALIZED=1

  mkdir -p "$LOG_DIR"

  # 10MB 초과 시 자동 초기화
  if [ -f "$LOG_FILE" ]; then
    local size
    size=$(stat -c%s "$LOG_FILE" 2>/dev/null || echo 0)
    if [ "$size" -gt "$MAX_LOG_SIZE" ]; then
      : > "$LOG_FILE"
    fi
  fi

  # 3일 지난 파일 자동 삭제
  find "$LOG_DIR" -type f -mtime +3 -delete 2>/dev/null
}

_log_write() {
  local hook="$1"
  local level="$2"
  local msg="$3"
  local ts
  ts=$(date '+%Y-%m-%d %H:%M:%S')
  echo "[$ts] [$hook] $level $msg" >> "$LOG_FILE"
}

log_info() {
  local hook="$1" msg="$2"
  _log_init
  _log_write "$hook" "INFO" "$msg"
}

log_success() {
  local hook="$1" msg="$2"
  _log_init
  _log_write "$hook" "SUCCESS" "$msg"
}

log_fail() {
  local hook="$1" msg="$2"
  _log_init
  _log_write "$hook" "FAIL" "$msg"
}

log_error() {
  local hook="$1" msg="$2" detail="$3"
  local ts
  ts=$(date '+%Y-%m-%d %H:%M:%S')
  _log_init
  _log_write "$hook" "ERROR" "$msg"
  {
    echo "[$ts] [$hook] ERROR $msg"
    echo ""
    echo "--- DETAIL ---"
    echo "$detail"
  } > "$ERROR_FILE"
}

log_skip() {
  local hook="$1" msg="$2"
  _log_init
  _log_write "$hook" "SKIP" "$msg"
}
