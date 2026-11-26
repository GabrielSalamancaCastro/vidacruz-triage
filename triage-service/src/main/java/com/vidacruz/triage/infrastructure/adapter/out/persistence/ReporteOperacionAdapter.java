package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import com.vidacruz.triage.application.port.out.ReporteOperacionRepositoryPort;
import com.vidacruz.triage.domain.model.ReporteHora;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
public class ReporteOperacionAdapter implements ReporteOperacionRepositoryPort{

    private static final String SQL = """
            WITH primera_consulta AS (
                SELECT a.paciente_id, MIN(a.timestamp) AS primera_entrada_consulta
                FROM auditoria a
                WHERE a.estado_nuevo = 'EN_CONSULTA'
                GROUP BY a.paciente_id
            ),
            esperas AS (
                SELECT
                    p.id,
                    p.fecha_registro,
                    p.nivel_triaje,
                    EXTRACT(EPOCH FROM (pc.primera_entrada_consulta - p.fecha_registro)) / 60.0 AS espera_minutos
                FROM pacientes p
                LEFT JOIN primera_consulta pc ON pc.paciente_id = p.id
                WHERE p.fecha_registro >= :desde AND p.fecha_registro <= :hasta
            )
            SELECT
                date_trunc('hour', fecha_registro) AS hora,
                COUNT(*) AS total_registrados,
                COUNT(*) FILTER (WHERE nivel_triaje = 'CRITICO')      AS criticos,
                COUNT(*) FILTER (WHERE nivel_triaje = 'URGENTE')      AS urgentes,
                COUNT(*) FILTER (WHERE nivel_triaje = 'URGENTE_2')    AS urgentes_2,
                COUNT(*) FILTER (WHERE nivel_triaje = 'NO_URGENTE')   AS no_urgentes,
                COUNT(*) FILTER (WHERE nivel_triaje = 'NO_URGENTE_2') AS no_urgentes_2,
                COALESCE(AVG(espera_minutos) FILTER (WHERE espera_minutos IS NOT NULL), 0) AS espera_promedio_min,
                COALESCE(
                    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY espera_minutos)
                        FILTER (WHERE espera_minutos IS NOT NULL),
                    0
                ) AS espera_p95_min
            FROM esperas
            GROUP BY date_trunc('hour', fecha_registro)
            ORDER BY hora
            """;

    private final DatabaseClient databaseClient;

    public ReporteOperacionAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<ReporteHora> generarReporte(Instant desde, Instant hasta) {
        return databaseClient.sql(SQL)
                .bind("desde", OffsetDateTime.ofInstant(desde, ZoneOffset.UTC))
                .bind("hasta", OffsetDateTime.ofInstant(hasta, ZoneOffset.UTC))
                .map((row, metadata) -> new ReporteHora(
                        row.get("hora", OffsetDateTime.class).toInstant(),
                        row.get("total_registrados", Long.class),
                        Map.of(
                                "CRITICO", row.get("criticos", Long.class),
                                "URGENTE", row.get("urgentes", Long.class),
                                "URGENTE_2", row.get("urgentes_2", Long.class),
                                "NO_URGENTE", row.get("no_urgentes", Long.class),
                                "NO_URGENTE_2", row.get("no_urgentes_2", Long.class)
                        ),
                        row.get("espera_promedio_min", Double.class),
                        row.get("espera_p95_min", Double.class)
                ))
                .all();
    }
}
