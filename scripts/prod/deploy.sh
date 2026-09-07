#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${DIR}/../.." && pwd)"
FRONTEND_DIR="${REPO_ROOT}/../ArchForgeAdmin"

# shellcheck source=/dev/null
[ -f "${DIR}/.env" ] && source "${DIR}/.env"

if [ ! -d "${FRONTEND_DIR}" ]; then
    echo "Frontend repo not found at ${FRONTEND_DIR}, cloning..."
    git clone https://github.com/sofn/ArchForgeAdmin.git "${FRONTEND_DIR}"
fi

cd "${REPO_ROOT}"

echo "=== Building backend bootJar ==="
./gradlew :archforge-server-admin:bootJar -x test -x spotlessCheck --no-daemon

echo "=== Building backend image: archforge:fulljre ==="
docker build -f docker/fulljre/Dockerfile -t archforge:fulljre .

echo "=== Building frontend image: archforge-frontend:latest ==="
docker build -f "${REPO_ROOT}/docker/Dockerfile.frontend" -t archforge-frontend:latest "${FRONTEND_DIR}"

cd "${REPO_ROOT}/docker"

echo "=== Starting infrastructure ==="
docker compose -f docker-compose.prod.yml up -d postgres redis

echo "=== Ensuring databases exist ==="
for i in $(seq 1 30); do
    if docker compose -f docker-compose.prod.yml exec -T postgres pg_isready -U "${DB_USERNAME:-archforge}" >/dev/null 2>&1; then
        break
    fi
    sleep 1
done
docker compose -f docker-compose.prod.yml exec -T postgres psql -U "${DB_USERNAME:-archforge}" -d postgres -f /docker-entrypoint-initdb.d/init-db.sql

echo "=== Starting backend ==="
docker compose -f docker-compose.prod.yml up -d backend

echo "=== Waiting for backend to start ==="
for i in $(seq 1 120); do
    if docker compose -f docker-compose.prod.yml logs backend 2>/dev/null | grep -q 'Started .* in '; then
        break
    fi
    sleep 1
done

echo "=== Importing seed data ==="
docker cp "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql" "$(docker compose -f docker-compose.prod.yml ps -q postgres):/tmp/seed"
for sql in data-admin-user.sql data-admin-dept.sql data-admin-config.sql data-admin-scheduler.sql; do
    docker compose -f docker-compose.prod.yml exec -T postgres \
        psql -v ON_ERROR_STOP=0 -U "${DB_USERNAME:-archforge}" -d archforge_user -f "/tmp/seed/sql/${sql}"
done
docker compose -f docker-compose.prod.yml exec -T postgres \
    psql -U "${DB_USERNAME:-archforge}" -d archforge_user -f /tmp/seed/sql/reset-sequences.sql

echo "=== Starting frontend ==="
docker compose -f docker-compose.prod.yml up -d frontend

echo ""
echo "Production deployed. Frontend: http://localhost"
