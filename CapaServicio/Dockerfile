FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copiar todo el repo para construir el módulo CapaServicio (y sus dependencias)
COPY . /workspace

# Construir el módulo de servicios (genera target/chat-empresarial.war)
RUN mvn -B -pl CapaServicio -am -DskipTests package

# Descargar el driver PostgreSQL en el directorio del proyecto para copiarlo al runtime
RUN mvn -B -f CapaServicio/pom.xml dependency:copy -Dartifact=org.postgresql:postgresql:42.7.2 -DoutputDirectory=/workspace/CapaServicio/target

# Descargar y descomprimir WildFly 32 localmente para no depender de una imagen externa de WildFly
RUN mkdir -p /opt && \
	curl -fsSL -o /tmp/wildfly.tar.gz https://github.com/wildfly/wildfly/releases/download/32.0.1.Final/wildfly-32.0.1.Final.tar.gz && \
	tar -xzf /tmp/wildfly.tar.gz -C /opt && \
	mv /opt/wildfly-32.0.1.Final /opt/wildfly

FROM eclipse-temurin:21-jre-jammy

ENV WILDFLY_HOME=/opt/wildfly

# WildFly y el script de arranque requieren bash
RUN apt-get update && apt-get install -y --no-install-recommends bash && rm -rf /var/lib/apt/lists/*

# Copiar WildFly desde el stage de build
COPY --from=builder /opt/wildfly /opt/wildfly

# Copiar el .war generado
COPY --from=builder /workspace/CapaServicio/target/chat-empresarial.war ${WILDFLY_HOME}/standalone/deployments/

# Copiar el driver PostgreSQL desde el stage de build
COPY --from=builder /workspace/CapaServicio/target/postgresql-42.7.2.jar ${WILDFLY_HOME}/standalone/deployments/postgresql.jar

# Copiar script de configuración y hacerlo ejecutable (el contexto de build es la raíz del repo)
COPY CapaServicio/configure-and-run.sh ${WILDFLY_HOME}/bin/configure-and-run.sh
RUN chmod +x ${WILDFLY_HOME}/bin/configure-and-run.sh

EXPOSE 8080

CMD ["/opt/wildfly/bin/configure-and-run.sh"]

