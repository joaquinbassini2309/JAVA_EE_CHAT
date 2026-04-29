FROM maven:3.9.3-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copiar todo el repo para construir el módulo CapaServicio (y sus dependencias)
COPY . /workspace

# Construir el módulo de servicios (genera target/chat-empresarial.war)
RUN mvn -B -pl CapaServicio -am -DskipTests package

# Descargar el driver PostgreSQL en el directorio del proyecto para copiarlo al runtime
RUN mvn -B -f CapaServicio/pom.xml dependency:copy -Dartifact=org.postgresql:postgresql:42.7.2 -DoutputDirectory=/workspace/CapaServicio/target

FROM quay.io/wildfly/wildfly:32.0.1.Final

ENV WILDFLY_HOME=/opt/jboss/wildfly

# Copiar el .war generado
COPY --from=builder /workspace/CapaServicio/target/chat-empresarial.war ${WILDFLY_HOME}/standalone/deployments/

# Copiar el driver PostgreSQL desde el stage de build
COPY --from=builder /workspace/CapaServicio/target/postgresql-42.7.2.jar ${WILDFLY_HOME}/standalone/deployments/postgresql.jar

# Copiar script de configuración y hacerlo ejecutable
COPY configure-and-run.sh ${WILDFLY_HOME}/bin/configure-and-run.sh
RUN chmod +x ${WILDFLY_HOME}/bin/configure-and-run.sh

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/configure-and-run.sh"]

