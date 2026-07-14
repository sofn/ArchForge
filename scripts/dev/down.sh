#!/usr/bin/env bash
set -e

docker stop archforge-postgres-dev archforge-redis-dev archforge-s3-dev 2>/dev/null || true
docker rm archforge-postgres-dev archforge-redis-dev archforge-s3-dev 2>/dev/null || true

echo "Dev containers stopped and removed."
