#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker not found."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose not found."
  exit 1
fi

docker compose -f "${COMPOSE_FILE}" down
echo "[OK] RocketMQ services removed."
