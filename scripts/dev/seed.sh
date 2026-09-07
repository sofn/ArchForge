#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${DIR}/../.." && pwd)"

# shellcheck source=/dev/null
[ -f "${DIR}/.env" ] && source "${DIR}/.env"

DB_USERNAME="${DB_USERNAME:-archforge}"
DB_PASSWORD="${DB_PASSWORD:-archforge}"
DB_NAME_USER="${DB_NAME_USER:-archforge_user}"

POSTGRES_CONTAINER="archforge-postgres-dev"

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

echo "Seed import complete."
