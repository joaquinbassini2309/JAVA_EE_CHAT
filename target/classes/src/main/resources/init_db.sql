-- ==============================================================================
-- Script de Inicialización de Base de Datos para Chat JAVA EE (PostgreSQL)
-- ==============================================================================

-- 1. Limpieza inicial
DROP TABLE IF EXISTS mensajes CASCADE;
DROP TABLE IF EXISTS participantes CASCADE;
DROP TABLE IF EXISTS conversaciones CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- ==============================================================================
-- 2. Creación de Estructura de Tablas (DDL)
-- ==============================================================================

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    foto_url VARCHAR(255),
    estado VARCHAR(20) DEFAULT 'OFFLINE',
    activo BOOLEAN DEFAULT TRUE, 
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conversaciones (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    tipo VARCHAR(20) DEFAULT 'PRIVADA',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE participantes (
    id SERIAL PRIMARY KEY,
    id_conversacion INT NOT NULL,
    id_usuario INT NOT NULL,
    rol VARCHAR(20) DEFAULT 'MIEMBRO',
    fecha_union TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participantes_conversacion FOREIGN KEY (id_conversacion) REFERENCES conversaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_participantes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT uk_participantes_conv_user UNIQUE (id_conversacion, id_usuario)
);

CREATE TABLE mensajes (
    id SERIAL PRIMARY KEY,
    id_conversacion INT NOT NULL,
    id_emisor INT NOT NULL,
    contenido TEXT NOT NULL,
    tipo_mensaje VARCHAR(20) DEFAULT 'TEXTO',
    url_adjunto VARCHAR(255),
    leido BOOLEAN DEFAULT FALSE,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensajes_conversacion FOREIGN KEY (id_conversacion) REFERENCES conversaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_mensajes_emisor FOREIGN KEY (id_emisor) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_mensajes_conversacion ON mensajes(id_conversacion);

-- ==============================================================================
-- 3. Inserción de Datos Simulados (DML)
-- ==============================================================================

-- a) Usuarios
-- Aprovechamos los DEFAULT para 'activo' y 'fecha_registro'
INSERT INTO usuarios (username, email, password_hash, estado) VALUES
('admin_master', 'admin@example.com', '$2a$10$wN10Cuvx2c72v7MhVp/Fdu1TItZk0QkXqJXZBw.Q99H0YxI4/nN6m', 'ONLINE'),
('juanperez', 'juan@example.com', '$2a$10$wN10Cuvx2c72v7MhVp/Fdu1TItZk0QkXqJXZBw.Q99H0YxI4/nN6m', 'OFFLINE'),
('maria_dev', 'maria@example.com', '$2a$10$wN10Cuvx2c72v7MhVp/Fdu1TItZk0QkXqJXZBw.Q99H0YxI4/nN6m', 'ONLINE');

-- b) Conversaciones
INSERT INTO conversaciones (nombre, tipo) VALUES
('Soporte General', 'GRUPO'),
('Juan - Admin', 'PRIVADA');

-- c) Participantes
INSERT INTO participantes (id_conversacion, id_usuario, rol) VALUES
(1, 1, 'ADMIN'),
(1, 2, 'MIEMBRO'),
(1, 3, 'MIEMBRO'),
(2, 2, 'MIEMBRO'),
(2, 1, 'MIEMBRO');

-- d) Mensajes
INSERT INTO mensajes (id_conversacion, id_emisor, contenido, leido) VALUES
(1, 1, '¡Bienvenidos al grupo de soporte general!', FALSE),
(1, 2, '¡Gracias admin, listo para ayudar!', FALSE),
(2, 2, 'Hola admin, te enviaba este mensaje confidencial en privado.', TRUE),
(2, 1, 'Recibido Juan. Todo en perfecto orden.', FALSE);