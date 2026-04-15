#!/usr/bin/env bash
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}"

MODE="${1:-jvm}"

case "$MODE" in
  jvm)
    echo "Starting in JVM mode (Project Leyden / CDS optimized)..."
    docker compose -f docker-compose.yml up -d --build
    ;;
  native)
    echo "Starting in Native Image mode (BellSoft Liberica NIK 25)..."
    docker compose -f docker-compose.native.yml up -d --build
    ;;
  down)
    echo "Stopping all services..."
    docker compose -f docker-compose.yml down 2>/dev/null
    docker compose -f docker-compose.native.yml down 2>/dev/null
    ;;
  *)
    echo "Usage: $0 [jvm|native|down]"
    echo "  jvm     - JVM mode with Leyden CDS optimization (default)"
    echo "  native  - Native Image mode (Liberica NIK 25)"
    echo "  down    - Stop all services"
    exit 1
    ;;
esac
