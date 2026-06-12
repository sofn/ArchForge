#!/usr/bin/env bash
# Build native Docker image (GraalVM Native Image via BellSoft Liberica NIK 25)
# Requires: 8GB+ RAM, --network=host for dependency download
# Pre-requisite: export JAVA_HOME to a JDK 25+ installation
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}/../.."

echo "=== Building Docker image: archforge:native ==="
echo "Note: This may take 10+ minutes on first build"
docker build --network=host -f docker/native/Dockerfile -t archforge:native .

echo "=== Done ==="
echo "Run with: cd docker && docker compose -f docker-compose.native.yml up -d"
