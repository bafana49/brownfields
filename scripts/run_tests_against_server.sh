#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

SERVER_SCRIPT="$1"
TEST_PATTERN="$2"

bash "$SERVER_SCRIPT" &
SERVER_PID=$!
trap 'kill "$SERVER_PID" >/dev/null 2>&1 || true' EXIT
sleep 5

if [ -z "$TEST_PATTERN" ]; then
  MVN_CMD=(mvn test)
else
  MVN_CMD=(mvn -Dtest="$TEST_PATTERN" test)
fi

if ! "${MVN_CMD[@]}"; then
  echo "Tests failed against server: $SERVER_SCRIPT"
  exit 1
fi

kill "$SERVER_PID" >/dev/null 2>&1 || true
trap - EXIT
