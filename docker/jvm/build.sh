#!/usr/bin/env bash
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}/../.."

echo "=== Building bootJar ==="
JAVA_HOME=/home/sofn/jdks/zulu25 ./gradlew :server-admin:bootJar -x test -x spotlessCheck --no-daemon

echo "=== Building Docker image (JVM + Project Leyden CDS) ==="
docker build -f docker/jvm/Dockerfile -t archsmith:jvm .

echo "=== Done ==="
echo "Run with: cd docker && docker compose up -d"
