#!/usr/bin/env bash
# Build jlink Docker image (minimal JRE + Project Leyden CDS)
# Pre-requisite: export JAVA_HOME to a JDK 25+ installation
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}/../.."

echo "=== Building bootJar ==="
./gradlew :server-admin:bootJar -x test -x spotlessCheck --no-daemon

echo "=== Building Docker image: archforge:jlink ==="
docker build -f docker/jlink/Dockerfile -t archforge:jlink .

echo "=== Done ==="
echo "Run with: cd docker && docker compose -f docker-compose.jlink.yml up -d"
