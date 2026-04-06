#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"

# 参数：image namesrv_container broker_container namesrv_port broker_port broker_ha_port broker_fast_port proxy_port broker_java_opt dashboard_image dashboard_container dashboard_port enable_dashboard namesrv_java_opt
export ROCKETMQ_IMAGE="${1:-apache/rocketmq:5.3.2}"
export NAMESRV_CONTAINER="${2:-rmq-namesrv}"
export BROKER_CONTAINER="${3:-rmq-broker}"
export NAMESRV_PORT="${4:-9876}"
export BROKER_PORT="${5:-10911}"
export BROKER_HA_PORT="${6:-10909}"
export BROKER_FAST_PORT="${7:-10912}"
export PROXY_PORT="${8:-8081}"
export BROKER_JAVA_OPT="${9:--server -Xms384m -Xmx384m -Xmn192m}"
export DASHBOARD_IMAGE="${10:-apacherocketmq/rocketmq-dashboard:latest}"
export DASHBOARD_CONTAINER="${11:-rmq-dashboard}"
export DASHBOARD_PORT="${12:-8080}"
export ENABLE_DASHBOARD="${13:-false}"
export NAMESRV_JAVA_OPT="${14:--server -Xms128m -Xmx128m -Xmn64m}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker not found."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose not found."
  exit 1
fi

if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "[ERROR] compose file not found: ${COMPOSE_FILE}"
  exit 1
fi

if ! docker image inspect "${ROCKETMQ_IMAGE}" >/dev/null 2>&1; then
  echo "[INFO] Local image not found, trying to pull: ${ROCKETMQ_IMAGE}"
  docker pull "${ROCKETMQ_IMAGE}"
else
  echo "[INFO] Local image found: ${ROCKETMQ_IMAGE}, skip pull."
fi

if [[ "${ENABLE_DASHBOARD}" == "true" ]]; then
  if ! docker image inspect "${DASHBOARD_IMAGE}" >/dev/null 2>&1; then
    echo "[INFO] Local image not found, trying to pull: ${DASHBOARD_IMAGE}"
    docker pull "${DASHBOARD_IMAGE}"
  else
    echo "[INFO] Local image found: ${DASHBOARD_IMAGE}, skip pull."
  fi
fi

if [[ "${ENABLE_DASHBOARD}" == "true" ]]; then
  docker compose --profile dashboard -f "${COMPOSE_FILE}" up -d
else
  docker compose -f "${COMPOSE_FILE}" up -d namesrv broker
fi

echo "[OK] RocketMQ deployed."
echo "[INFO] NameServer: ${NAMESRV_PORT}, Broker: ${BROKER_PORT}"
if [[ "${ENABLE_DASHBOARD}" == "true" ]]; then
  echo "[INFO] Dashboard: ${DASHBOARD_PORT}"
else
  echo "[INFO] Dashboard: disabled (set ENABLE_DASHBOARD=true to enable)"
fi
echo "[INFO] Check containers: docker ps | grep rmq"
echo "[INFO] Check logs: docker logs --tail=80 ${NAMESRV_CONTAINER}"
echo "[INFO] Check logs: docker logs --tail=80 ${BROKER_CONTAINER}"
if [[ "${ENABLE_DASHBOARD}" == "true" ]]; then
  echo "[INFO] Check logs: docker logs --tail=80 ${DASHBOARD_CONTAINER}"
fi
