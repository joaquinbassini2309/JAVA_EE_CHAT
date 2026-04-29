#!/bin/bash
set -euo pipefail

WILDFLY_HOME=/opt/wildfly
TEMPLATE="${WILDFLY_HOME}/standalone/configuration/chat-ds.xml.template"
TARGET="${WILDFLY_HOME}/standalone/deployments/chat-ds.xml"

echo "[entrypoint] Configurando datasource si corresponde..."
rm -f "$TARGET"

if [ -n "${DB_HOST:-}" ] && [ -n "${DB_NAME:-}" ] && [ -n "${DB_USER:-}" ] && [ -n "${DB_PASSWORD:-}" ]; then
  DB_PORT=${DB_PORT:-5432}
  echo "[entrypoint] Generando datasource ChatDS desde plantilla"
  export DB_PORT
  envsubst < "$TEMPLATE" > "$TARGET"
else
  echo "[entrypoint] Variables de BD no proporcionadas, omitiendo creación de datasource (usando JNDI existente si aplica)."
fi

echo "[entrypoint] Iniciando WildFly..."
exec ${WILDFLY_HOME}/bin/standalone.sh -b 0.0.0.0 -Djboss.http.port=${PORT:-8080}

