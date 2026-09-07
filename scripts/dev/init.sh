#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${DIR}/../.." && pwd)"

# shellcheck source=/dev/null
[ -f "${DIR}/.env" ] && source "${DIR}/.env"

DB_USERNAME="${DB_USERNAME:-archforge}"
DB_PASSWORD="${DB_PASSWORD:-archforge}"
DB_NAME_USER="${DB_NAME_USER:-archforge_user}"
DB_NAME_TASK="${DB_NAME_TASK:-archforge_task}"
RUSTFS_ROOT_USER="${RUSTFS_ROOT_USER:-minioadmin}"
RUSTFS_ROOT_PASSWORD="${RUSTFS_ROOT_PASSWORD:-minioadmin}"
S3_BUCKET="${S3_BUCKET:-archforge}"

POSTGRES_CONTAINER="archforge-postgres-dev"
REDIS_CONTAINER="archforge-redis-dev"
S3_CONTAINER="archforge-s3-dev"

start_postgres() {
    if docker ps -a --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
        echo "PostgreSQL container already exists, starting..."
        docker start "${POSTGRES_CONTAINER}" >/dev/null
    else
        echo "Creating PostgreSQL container..."
        docker run -d --name "${POSTGRES_CONTAINER}" \
            -e POSTGRES_USER="${DB_USERNAME}" \
            -e POSTGRES_PASSWORD="${DB_PASSWORD}" \
            -e POSTGRES_DB="${DB_NAME_USER}" \
            -p 5432:5432 \
            --health-cmd "pg_isready -U ${DB_USERNAME}" \
            --health-interval 5s \
            --health-timeout 2s \
            --health-retries 10 \
            postgres:17-alpine
    fi

    echo "Waiting for PostgreSQL to be ready..."
    for i in {1..60}; do
        if docker exec "${POSTGRES_CONTAINER}" pg_isready -U "${DB_USERNAME}" >/dev/null 2>&1; then
            break
        fi
        sleep 1
    done
    docker exec "${POSTGRES_CONTAINER}" pg_isready -U "${DB_USERNAME}"
}

start_redis() {
    if docker ps -a --format '{{.Names}}' | grep -q "^${REDIS_CONTAINER}$"; then
        echo "Redis container already exists, starting..."
        docker start "${REDIS_CONTAINER}" >/dev/null
    else
        echo "Creating Redis container..."
        docker run -d --name "${REDIS_CONTAINER}" \
            -p 6379:6379 \
            --health-cmd "redis-cli ping" \
            --health-interval 5s \
            --health-timeout 2s \
            --health-retries 10 \
            redis:7-alpine
    fi

    echo "Waiting for Redis to be ready..."
    for i in {1..60}; do
        if docker exec "${REDIS_CONTAINER}" redis-cli ping >/dev/null 2>&1; then
            break
        fi
        sleep 1
    done
    docker exec "${REDIS_CONTAINER}" redis-cli ping
}

start_s3() {
    if docker ps -a --format '{{.Names}}' | grep -q "^${S3_CONTAINER}$"; then
        echo "S3 container already exists, starting..."
        docker start "${S3_CONTAINER}" >/dev/null
    else
        echo "Creating RustFS S3 container..."
        docker run -d --name "${S3_CONTAINER}" \
            -e RUSTFS_ROOT_USER="${RUSTFS_ROOT_USER}" \
            -e RUSTFS_ROOT_PASSWORD="${RUSTFS_ROOT_PASSWORD}" \
            -p 9000:9000 \
            rustfs/rustfs:latest server /data
    fi

    echo "Waiting for RustFS to be ready..."
    for i in {1..60}; do
        if curl -sf "http://localhost:9000/health" >/dev/null 2>&1; then
            break
        fi
        sleep 1
    done
    curl -sf "http://localhost:9000/health" >/dev/null
}

init_databases() {
    echo "Ensuring databases exist..."
    for db in "${DB_NAME_USER}" "${DB_NAME_TASK}"; do
        exists=$(docker exec "${POSTGRES_CONTAINER}" psql -U "${DB_USERNAME}" -d postgres -Atc "SELECT 1 FROM pg_database WHERE datname = '${db}';" || true)
        if [ "${exists}" != "1" ]; then
            docker exec "${POSTGRES_CONTAINER}" psql -U "${DB_USERNAME}" -d postgres -c "CREATE DATABASE ${db};"
        fi
    done
}

import_seed() {
    echo "Importing seed data into ${DB_NAME_USER}..."
    for sql in "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql/data-admin-user.sql" \
               "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql/data-admin-dept.sql" \
               "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql/data-admin-config.sql" \
               "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql/data-admin-scheduler.sql" \
               "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql/data-admin-blog.sql" \
               "${REPO_ROOT}/archforge-domain/archforge-admin-user/src/main/resources/sql/reset-sequences.sql"; do
        filename=$(basename "${sql}")
        docker cp "${sql}" "${POSTGRES_CONTAINER}:/tmp/${filename}"
        docker exec "${POSTGRES_CONTAINER}" psql -U "${DB_USERNAME}" -d "${DB_NAME_USER}" -v ON_ERROR_STOP=0 -f "/tmp/${filename}"
    done

    echo "Creating S3 bucket if needed..."
    curl -X PUT "http://localhost:9000/${S3_BUCKET}" \
        -H "Authorization: AWS ${RUSTFS_ROOT_USER}:${RUSTFS_ROOT_PASSWORD}" \
        >/dev/null 2>&1 || true
}

start_postgres
start_redis
start_s3
init_databases

echo ""
echo "Dev environment is ready."
echo "Run: cd ${REPO_ROOT} && JAVA_HOME=/path/to/jdk25 ./gradlew :archforge-server-admin:bootRun"
echo "After the app has started (tables created by Hibernate), seed data with:"
echo "  ${DIR}/seed.sh"
