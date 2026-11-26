CREATE TABLE auditoria (
                           id              VARCHAR(36) PRIMARY KEY,
                           paciente_id     VARCHAR(36) NOT NULL,
                           estado_anterior VARCHAR(20) NOT NULL,
                           estado_nuevo    VARCHAR(20) NOT NULL,
                           tipo_evento     VARCHAR(50) NOT NULL,
                           timestamp       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_auditoria_paciente ON auditoria(paciente_id);
CREATE INDEX idx_auditoria_timestamp ON auditoria(timestamp);