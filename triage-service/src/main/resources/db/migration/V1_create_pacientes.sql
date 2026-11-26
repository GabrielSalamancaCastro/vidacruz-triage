
CREATE TABLE pacientes (
                           id                  VARCHAR(36) PRIMARY KEY,
                           nombre_completo     VARCHAR(100) NOT NULL,
                           edad                INT NOT NULL,
                           frecuencia_cardiaca INT NOT NULL,
                           presion_sistolica   INT NOT NULL,
                           presion_diastolica  INT NOT NULL,
                           spo2                INT NOT NULL,
                           temperatura         NUMERIC(4,1) NOT NULL,
                           dolor               INT NOT NULL,
                           gcs                 INT NOT NULL,
                           queja_principal     VARCHAR(500) NOT NULL,
                           nivel_triaje        VARCHAR(20) NOT NULL,
                           estado              VARCHAR(20) NOT NULL,
                           escalado            BOOLEAN NOT NULL DEFAULT FALSE,
                           fecha_registro      TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Índices para las consultas de Fase 5 (scheduler SLA) y Fase 6 (reportes)
CREATE INDEX idx_pacientes_estado ON pacientes(estado);
CREATE INDEX idx_pacientes_fecha_registro ON pacientes(fecha_registro);
CREATE INDEX idx_pacientes_nivel_triaje ON pacientes(nivel_triaje);