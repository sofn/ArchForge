#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${DIR}/../.." && pwd)"

docker stop archforge-postgres-dev archforge-redis-dev archforge-s3-dev 2>/dev/null || true
docker rm archforge-postgres-dev archforge-redis-dev archforge-s3-dev 2>/dev/null || true

docker compose -f "${REPO_ROOT}/docker/observability/docker-compose.observability.yml" down 2>/dev/null || true

echo "Dev and observability containers stopped."
