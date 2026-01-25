-- Migración 2: Agregar campos GPS y picture
-- Version: 2
-- Descripción: Agrega campos de GPS para checkin/checkout y campo picture

-- Agregar campos de GPS si no existen
ALTER TABLE registros ADD COLUMN IF NOT EXISTS latitude_checkin DOUBLE DEFAULT NULL;
ALTER TABLE registros ADD COLUMN IF NOT EXISTS longitude_checkin DOUBLE DEFAULT NULL;
ALTER TABLE registros ADD COLUMN IF NOT EXISTS precision_metros_checkin DOUBLE DEFAULT NULL;

ALTER TABLE registros ADD COLUMN IF NOT EXISTS latitude_checkout DOUBLE DEFAULT NULL;
ALTER TABLE registros ADD COLUMN IF NOT EXISTS longitude_checkout DOUBLE DEFAULT NULL;
ALTER TABLE registros ADD COLUMN IF NOT EXISTS precision_metros_checkout DOUBLE DEFAULT NULL;

-- Agregar campo picture para URL de imagen
ALTER TABLE registros ADD COLUMN IF NOT EXISTS picture VARCHAR(500) DEFAULT NULL;

-- Remover restricción única anterior si existe y crearla nueva
ALTER TABLE registros DROP INDEX IF EXISTS unique_usuario_fecha;
ALTER TABLE registros ADD UNIQUE KEY unique_usuario_fecha (usuario_id, fecha);
