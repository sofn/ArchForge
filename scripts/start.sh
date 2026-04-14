#!/usr/bin/env bash
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}"

MODE="${1:-native}"

case "$MODE" in
  native)
    echo "Starting in Native mode (GraalVM)..."
    docker compose -f docker-compose.native.yml up -d --build
    ;;
  jvm)
    echo "Starting in JVM mode..."
    docker compose -f docker-compose.yml up -d --build
    ;;
  *)
    echo "Usage: $0 [native|jvm]"
    echo "  native  - Spring Native (GraalVM) mode (default)"
    echo "  jvm     - Standard JVM mode"
    exit 1
    ;;
esac
