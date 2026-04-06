#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
ENABLE_DASHBOARD="${1:-false}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker not found."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose not found."
  exit 1
fi

if [[ "${ENABLE_DASHBOARD}" == "true" ]]; then
  docker compose --profile dashboard -f "${COMPOSE_FILE}" start
else
  docker compose -f "${COMPOSE_FILE}" start namesrv broker
fi
echo "[OK] RocketMQ services started."
