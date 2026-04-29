#!/bin/bash
set -euo pipefail

WILDFLY_HOME=/opt/jboss/wildfly

echo "[entrypoint] Configurando datasource si corresponde..."

if [ -n "${DB_HOST:-}" ] && [ -n "${DB_NAME:-}" ] && [ -n "${DB_USER:-}" ] && [ -n "${DB_PASSWORD:-}" ]; then
  DB_PORT=${DB_PORT:-5432}
  DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
  echo "[entrypoint] Añadiendo módulo y driver PostgreSQL y datasource ChatDS"
  ${WILDFLY_HOME}/bin/jboss-cli.sh --command="embed-server --std-out=echo;module add --name=org.postgresql --resources=${WILDFLY_HOME}/standalone/deployments/postgresql.jar --dependencies=javax.api,javax.transaction.api;jdbc-driver add --driver-name=postgresql --driver-module-name=org.postgresql --driver-class-name=org.postgresql.Driver;data-source add --name=ChatDS --jndi-name=java:/ChatDS --driver-name=postgresql --connection-url='${DB_URL}' --user-name='${DB_USER}' --password='${DB_PASSWORD}' --use-ccm=false;stop-embedded-server"
else
  echo "[entrypoint] Variables de BD no proporcionadas, omitiendo creación de datasource (usando JNDI existente si aplica)."
fi

echo "[entrypoint] Iniciando WildFly..."
exec ${WILDFLY_HOME}/bin/standalone.sh -b 0.0.0.0 -Djboss.http.port=${PORT:-8080}

