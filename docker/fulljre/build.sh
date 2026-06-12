#!/usr/bin/env bash
# Build full JRE Docker image (simplest, largest image)
# Pre-requisite: export JAVA_HOME to a JDK 25+ installation
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}/../.."

echo "=== Building bootJar ==="
./gradlew :server-admin:bootJar -x test -x spotlessCheck --no-daemon

echo "=== Building Docker image: archforge:fulljre ==="
docker build -f docker/fulljre/Dockerfile -t archforge:fulljre .

echo "=== Done ==="
echo "Run with: cd docker && docker compose -f docker-compose.fulljre.yml up -d"
