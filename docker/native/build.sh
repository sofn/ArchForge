#!/usr/bin/env bash
set -e
dir="$(cd "$(dirname "$0")" && pwd)"
cd "${dir}/../.."

echo "=== Building Native Image (BellSoft Liberica NIK 25) ==="
echo "Note: This may take 10+ minutes on first build"
docker build -f docker/native/Dockerfile -t appforge:native .

echo "=== Done ==="
echo "Run with: cd docker && docker compose -f docker-compose.native.yml up -d"
