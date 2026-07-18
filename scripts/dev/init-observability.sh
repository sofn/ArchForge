#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${DIR}/../.." && pwd)"

cd "${REPO_ROOT}/docker/observability"

docker compose up -d

echo ""
echo "Observability stack is ready."
echo "  Grafana      http://localhost:3000  (admin / admin)"
echo "  Prometheus   http://localhost:9090"
echo "  Jaeger UI    http://localhost:16686"
echo "  Alertmanager http://localhost:9093"
echo ""
echo "For local bootRun, export:"
echo "  OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318/v1/traces"
echo "  SAMPLING_PROBABILITY=1.0"
