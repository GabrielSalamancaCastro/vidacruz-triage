package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import com.vidacruz.triage.domain.model.EstadoPaciente;
import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.model.SignosVitales;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
@Import({PacientePersistenceAdapter.class, PacienteMapper.class, PacientePersistenceAdapterIT.FlywayConfig.class})
class PacientePersistenceAdapterIT {

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
    private PacientePersistenceAdapter adapter;

    @Test
    void guarda_y_recupera_un_paciente() {
        var signos = new SignosVitales(130, 180, 110, 97, 36.8, 8, 15);
        var paciente = new Paciente(
                UUID.randomUUID().toString(),
                "Juan Pérez", 45, signos, "Dolor torácico",
                NivelTriaje.CRITICO, EstadoPaciente.REGISTRADO, false, Instant.now()
        );

        StepVerifier.create(adapter.guardar(paciente))
                .assertNext(g -> {
                    assertThat(g.id()).isEqualTo(paciente.id());
                    assertThat(g.nivelTriaje()).isEqualTo(NivelTriaje.CRITICO);
                })
                .verifyComplete();
    }
}
