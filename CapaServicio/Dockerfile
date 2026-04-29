FROM maven:3.9.3-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copiar todo el repo para construir el módulo CapaServicio (y sus dependencias)
COPY . /workspace

# Construir el módulo de servicios (genera target/chat-empresarial.war)
RUN mvn -B -pl CapaServicio -am -DskipTests package

FROM quay.io/wildfly/wildfly:29.0.1.Final

ENV WILDFLY_HOME=/opt/jboss/wildfly

# Copiar el .war generado
COPY --from=builder /workspace/CapaServicio/target/chat-empresarial.war ${WILDFLY_HOME}/standalone/deployments/

# Descargar el driver de Postgres en la imagen para poder crear datasource en tiempo de arranque
ARG POSTGRESQL_VERSION=42.7.2
RUN curl -L -o ${WILDFLY_HOME}/standalone/deployments/postgresql.jar \
    https://repo1.maven.org/maven2/org/postgresql/postgresql/${POSTGRESQL_VERSION}/postgresql-${POSTGRESQL_VERSION}.jar

# Copiar script de configuración y hacerlo ejecutable
COPY configure-and-run.sh ${WILDFLY_HOME}/bin/configure-and-run.sh
RUN chmod +x ${WILDFLY_HOME}/bin/configure-and-run.sh

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/configure-and-run.sh"]

