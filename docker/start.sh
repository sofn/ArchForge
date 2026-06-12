#!/usr/bin/env bash
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}"

MODE="${1:-jlink}"

case "$MODE" in
  jlink)
    echo "Starting in jlink mode (minimal JRE + Project Leyden CDS)..."
    docker compose -f docker-compose.jlink.yml up -d --build
    ;;
  fulljre)
    echo "Starting in full JRE mode (simplest, largest image)..."
    docker compose -f docker-compose.fulljre.yml up -d --build
    ;;
  native)
    echo "Starting in Native Image mode (BellSoft Liberica NIK 25)..."
    docker compose -f docker-compose.native.yml up -d --build
    ;;
  down)
    echo "Stopping all services..."
    docker compose -f docker-compose.jlink.yml down 2>/dev/null || true
    docker compose -f docker-compose.fulljre.yml down 2>/dev/null || true
    docker compose -f docker-compose.native.yml down 2>/dev/null || true
    ;;
  *)
    echo "Usage: $0 [jlink|fulljre|native|down]"
    echo ""
    echo "  jlink   - Minimal JRE via jlink + Project Leyden CDS (default, recommended)"
    echo "  fulljre - Full Azul Zulu JRE (simplest, fastest to build)"
    echo "  native  - GraalVM Native Image (smallest, fastest startup, needs 8GB+ RAM)"
    echo "  down    - Stop all services"
    exit 1
    ;;
esac
