# GUÍA: Dónde colocar persistence.xml

El archivo `persistence.xml` debe estar en la carpeta de recursos, dentro de `META-INF`.

## Estructura de directorios Maven

```
tu-proyecto/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/                    ← Código fuente aquí
│   │   │   └── chat/
│   │   │       ├── clases/
│   │   │       ├── Manejadores/
│   │   │       ├── Sistema/
│   │   │       └── ...
│   │   └── resources/               ← ARCHIVOS DE CONFIGURACIÓN AQUÍ
│   │       └── META-INF/
│   │           └── persistence.xml  ← AQUÍ VA EL ARCHIVO
│   └── test/
│       └── java/
└── target/
```

## Pasos para crear la estructura:

1. En tu IDE (IntelliJ IDEA):
   - Clic derecho en proyecto → New → Directory
   - Crear: `src/main/resources`
   - Clic derecho en `resources` → New → Directory
   - Crear: `META-INF`

2. Clic derecho en `META-INF` → New → File
   - Nombre: `persistence.xml`
   - Pega el contenido (ver archivo adjunto)

## Alternativa: Línea de comandos

```bash
cd C:\Users\Usuario\IdeaProjects\JAVA_EE_CHAT
mkdir -p src\main\resources\META-INF
```

Luego copia el archivo persistence.xml a esa carpeta.

## Resultado final

Cuando compilas, Maven automáticamente:
1. Copia `src/main/resources` → `target/classes`
2. El archivo estará en: `target/classes/META-INF/persistence.xml`
3. En el WAR estará en: `chat-empresarial.war/WEB-INF/classes/META-INF/persistence.xml`

WildFly lo encontrará automáticamente.
