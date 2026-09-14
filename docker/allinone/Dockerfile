# All-in-one image: nginx + server-admin + server-web + next.js under s6-overlay.
#
# Artifacts are pre-staged into context/ by `archforge build --allinone`:
#   context/server-admin.jar   archforge-server-admin bootJar
#   context/server-web.jar     archforge-server-web bootJar
#   context/admin/             ArchForgeAdmin dist (static SPA)
#   context/web/               ArchForgeWeb .next/standalone
#   context/web-static/        ArchForgeWeb .next/static
#   context/web-public/        ArchForgeWeb public
#
# Ports: 80 (web, nginx → next.js) · 8088 (admin, nginx → SPA + /api → :8080)
# Processes: s6-overlay supervises nginx, server-admin, server-web, nextjs.

FROM node:22-bookworm-slim AS node

FROM azul/zulu-openjdk:25
ARG S6_OVERLAY_VERSION=3.2.0.2

RUN apt-get update \
    && apt-get install -y --no-install-recommends nginx curl xz-utils ca-certificates \
    && rm -rf /var/lib/apt/lists/* /etc/nginx/sites-enabled/default

# Node runtime for the Next.js standalone server (glibc build — alpine musl won't run here).
COPY --from=node /usr/local/bin/node /usr/local/bin/node

ADD https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-noarch.tar.xz /tmp/
ADD https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-x86_64.tar.xz /tmp/
RUN tar -C / -Jxpf /tmp/s6-overlay-noarch.tar.xz \
    && tar -C / -Jxpf /tmp/s6-overlay-x86_64.tar.xz \
    && rm /tmp/s6-overlay-*.tar.xz

COPY context/server-admin.jar /app/server-admin.jar
COPY context/server-web.jar /app/server-web.jar
COPY context/admin/ /srv/admin/
COPY context/web/ /srv/web/
COPY context/web-static/ /srv/web/apps/web/.next/static/
COPY context/web-public/ /srv/web/apps/web/public/

COPY s6-rc.d/ /etc/s6-overlay/s6-rc.d/
COPY nginx.conf /etc/nginx/conf.d/allinone.conf

EXPOSE 80 8088

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health >/dev/null \
    && curl -fsS http://localhost:8081/actuator/health >/dev/null \
    && curl -fsS http://localhost:80/ >/dev/null \
    || exit 1

ENTRYPOINT ["/init"]
