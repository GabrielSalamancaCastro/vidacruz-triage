package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
@Import({ReporteOperacionAdapter.class, ReporteOperacionR2dbcAdapterIT.FlywayConfig.class})
class ReporteOperacionR2dbcAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("vidacruz_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url",
                () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @TestConfiguration
    static class FlywayConfig {
        @Bean
        public FlywayRunner flywayRunner() {
            return new FlywayRunner();
        }
    }

    static class FlywayRunner {
        @PostConstruct
        public void migrate() {
            Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        }
    }

    @Autowired
    private ReporteOperacionAdapter adapter;

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void reporte_agrupa_por_hora_y_calcula_p95() {
        var horaBase = Instant.parse("2026-10-29T10:00:00Z");

        sembrarPaciente("p-1", "CRITICO", horaBase, "p-1", horaBase.plusSeconds(600));
        sembrarPaciente("p-2", "NO_URGENTE", horaBase, null, null);
        sembrarPaciente("p-3", "NO_URGENTE", horaBase, null, null);

        StepVerifier.create(adapter.generarReporte(horaBase, horaBase.plusSeconds(3600)))
                .assertNext(r -> {
                    assertThat(r.totalRegistrados()).isEqualTo(3);
                    assertThat(r.porNivel()).containsEntry("CRITICO", 1L);
                    assertThat(r.esperaPromedioMin()).isEqualTo(10.0);
                    assertThat(r.esperaP95Min()).isEqualTo(10.0);
                })
                .verifyComplete();
    }

    private void sembrarPaciente(String id, String nivel, Instant fechaRegistro,
                                 String pacienteConsultaId, Instant fechaConsulta) {
        databaseClient.sql("""
                INSERT INTO pacientes (id, nombre_completo, edad, frecuencia_cardiaca, presion_sistolica,
                    presion_diastolica, spo2, temperatura, dolor, gcs, queja_principal,
                    nivel_triaje, estado, escalado, fecha_registro)
                VALUES (:id, 'Test', 30, 80, 120, 80, 99, 36.5, 2, 15, 'Test',
                    :nivel, 'REGISTRADO', false, :fecha)
                """)
                .bind("id", id).bind("nivel", nivel)
                .bind("fecha", OffsetDateTime.ofInstant(fechaRegistro, ZoneOffset.UTC))
                .then().block();

        if (pacienteConsultaId != null) {
            databaseClient.sql("""
                    INSERT INTO auditoria (id, paciente_id, estado_anterior, estado_nuevo, tipo_evento, timestamp)
                    VALUES (:id, :pacienteId, 'REGISTRADO', 'EN_CONSULTA', 'paciente_en_consulta', :ts)
                    """)
                    .bind("id", java.util.UUID.randomUUID().toString())
                    .bind("pacienteId", pacienteConsultaId)
                    .bind("ts", OffsetDateTime.ofInstant(fechaConsulta, ZoneOffset.UTC))
                    .then().block();
        }
    }
}