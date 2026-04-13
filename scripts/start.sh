#!/usr/bin/env bash
set -e
dir="$(cd "$(dirname "$0")" && pwd)"

# Build and start via docker-compose
cd "${dir}"
docker compose up -d --build