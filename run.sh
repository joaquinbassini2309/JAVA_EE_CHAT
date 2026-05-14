# Detener el script si hay cualquier error
set -e

# Rutas
PROJECT_ROOT="/home/guzman-perera/Documentos/UTEC/JAVA EE/JAVA_EE_CHAT"
WILDFLY_BIN="/home/guzman-perera/Documentos/UTEC/JAVA EE/wildfly-32.0.1.Final/bin/standalone.sh"
WILDFLY_DEPLOY="/home/guzman-perera/Documentos/UTEC/JAVA EE/wildfly-32.0.1.Final/standalone/deployments"
DOCKER_NAME="postgres-chat"

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}--- PREPARANDO ENTORNO ---${NC}"

# 0. Limpiar puertos (por si quedaron procesos colgados)
echo -e "${YELLOW}Limpiando puertos 8080 y 9990...${NC}"
fuser -k 8080/tcp > /dev/null 2>&1 || true
fuser -k 9990/tcp > /dev/null 2>&1 || true
sleep 1

# 1. Compilar Frontend (Vite)
echo -e "${GREEN}Limpiando y compilando Frontend...${NC}"
rm -rf "$PROJECT_ROOT/CapaPresentacion/Web/dist"
# Borrar solo lo que genera Vite, NO borrar WEB-INF
find "$PROJECT_ROOT/CapaServicio/src/main/webapp/" -mindepth 1 -maxdepth 1 ! -name 'WEB-INF' -exec rm -rf {} +
cd "$PROJECT_ROOT/CapaPresentacion/Web" && npm run build
cp -r dist/* ../../CapaServicio/src/main/webapp/

# 2. Compilar con Maven
echo -e "${GREEN}Limpiando y compilando Backend...${NC}"
cd "$PROJECT_ROOT" && mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}[OK] Compilación exitosa.${NC}"
    # Limpiar y copiar el .war nuevo
    echo -e "${GREEN}Limpiando y copiando el .war a WildFly...${NC}"
    rm -rf "$WILDFLY_DEPLOY"/*
    # Forzar borrado de carpetas temporales de WildFly si existen
    rm -rf "$(dirname "$WILDFLY_DEPLOY")/tmp"
    rm -rf "$(dirname "$WILDFLY_DEPLOY")/data"
    cp "$PROJECT_ROOT/CapaServicio/target/chat-empresarial.war" "$WILDFLY_DEPLOY/"
else
    echo -e "${RED}[!] Error en la compilación. Se intentará arrancar igual...${NC}"
fi

echo ""
echo -e "${YELLOW}--- INICIANDO SERVICIOS ---${NC}"

# 2. Iniciar Docker
echo -e "${GREEN}Reiniciando contenedor Docker:${NC} $DOCKER_NAME"
docker stop $DOCKER_NAME > /dev/null 2>&1
docker start $DOCKER_NAME

echo ""

# 3. Iniciar WildFly
echo -e "${GREEN}Ejecutando:${NC} WildFly Server..."
"$WILDFLY_BIN" -b 0.0.0.0 &
WF_PID=$!

echo ""
echo -e "${CYAN}=======================================================================${NC}"
echo -e "${GREEN}  >>> TODO EN MARCHA${NC}"
echo -e "${YELLOW}  >>> PARA CERRAR TODO, PRESIONA [ENTER]${NC}"
echo -e "${CYAN}=======================================================================${NC}"

# Pausa hasta que quieras cerrar
read -p ""

echo -e "\n${RED}--- CERRANDO SERVICIOS ---${NC}"

# 4. Apagar WildFly
echo -e "${GREEN}Matando proceso Java (WildFly)...${NC}"
kill -9 $WF_PID > /dev/null 2>&1
ps aux | grep "[j]ava" | grep "wildfly" | awk '{print $2}' | xargs -r kill -9

# 5. Apagar Docker
echo -e "${GREEN}Ejecutando:${NC} docker stop $DOCKER_NAME"
docker stop $DOCKER_NAME

echo ""
echo -e "${GREEN}Entorno cerrado. ¡Hasta la próxima, Guzman!${NC}"

