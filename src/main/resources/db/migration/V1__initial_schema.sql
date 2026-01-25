-- Migración Inicial: Crear tablas base
-- Version: 1
-- Descripción: Crea las tablas usuarios y registros con estructura inicial

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacion VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS registros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME,
    latitude_checkin DOUBLE,
    longitude_checkin DOUBLE,
    precision_metros_checkin DOUBLE,
    latitude_checkout DOUBLE,
    longitude_checkout DOUBLE,
    precision_metros_checkout DOUBLE,
    reporte TEXT,
    picture VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    UNIQUE KEY unique_usuario_fecha (usuario_id, fecha)
);

-- Crear índices para optimizar búsquedas
CREATE INDEX idx_usuario_fecha ON registros(usuario_id, fecha);
CREATE INDEX idx_fecha ON registros(fecha);
