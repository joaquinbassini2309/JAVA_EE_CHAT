# --- ETAPA DE COMPILACIÓN ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- ETAPA DE EJECUCIÓN (WILDFLY) ---
FROM quay.io/wildfly/wildfly:32.0.1.Final-jdk21

# Variables de entorno por defecto
ENV DB_URL=jdbc:postgresql://db:5432/chatdb
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV PORT=8080

USER root
# Descargar driver a /tmp para evitar despliegue automático
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar /tmp/
RUN chown jboss:jboss /tmp/postgresql-42.7.3.jar

USER jboss
COPY setup.cli /opt/jboss/wildfly/bin/

# Configurar WildFly (standalone.xml)
RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent && \
    /opt/jboss/wildfly/bin/standalone.sh -c standalone.xml & \
    sleep 10 && \
    /opt/jboss/wildfly/bin/jboss-cli.sh --connect --file=/opt/jboss/wildfly/bin/setup.cli && \
    /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command=:shutdown

# Desplegar como ROOT.war para evitar 404 y prefijos
COPY --from=build /app/CapaServicio/target/chat-empresarial.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

EXPOSE 8080

# Comando de arranque con puerto dinámico
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-Djboss.http.port=${PORT}"]