#!/bin/bash
set -euo pipefail

WILDFLY_HOME=/opt/wildfly
TEMPLATE="${WILDFLY_HOME}/standalone/configuration/chat-ds.xml.template"
TARGET="${WILDFLY_HOME}/standalone/deployments/chat-ds.xml"

echo "[entrypoint] Configurando datasource si corresponde..."
rm -f "$TARGET"

if [ -n "${DB_HOST:-}" ] && [ -n "${DB_NAME:-}" ] && [ -n "${DB_USER:-}" ] && [ -n "${DB_PASSWORD:-}" ]; then
  DB_PORT=${DB_PORT:-5432}
  echo "[entrypoint] Generando datasource ChatDS desde plantilla (PostgreSQL)"
  export DB_PORT
  envsubst < "$TEMPLATE" > "$TARGET"
  echo "[entrypoint] Datasource creado correctamente"
  cat "$TARGET"
else
  echo "[WARNING] Variables de BD (DB_HOST, DB_NAME, DB_USER, DB_PASSWORD) no proporcionadas"
  echo "[WARNING] Asegúrate de configurarlas en Render.com o la aplicación no funcionará"
  echo "[entrypoint] Esperando 5 segundos antes de iniciar WildFly..."
  sleep 5
fi

echo "[entrypoint] Iniciando WildFly..."
exec ${WILDFLY_HOME}/bin/standalone.sh -b 0.0.0.0 -Djboss.http.port=${PORT:-8080}

