package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.model.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObtenerPacienteServiceTest {
    @Test
    void devuelve_paciente_existente() {
        var repo = mock(PacienteRepositoryPort.class);
        var paciente = new Paciente(
                "p-001", "Juan Pérez", 45,
                new SignosVitales(130, 180, 110, 97, 36.8, 8, 15),
                "Dolor torácico", NivelTriaje.CRITICO,
                EstadoPaciente.REGISTRADO, false, Instant.now()
        );
        when(repo.buscarPorId("p-001")).thenReturn(Mono.just(paciente));

        var service = new ObtenerPacienteService(repo);

        StepVerifier.create(service.obtener("p-001"))
                .assertNext(p -> assertThat(p.id()).isEqualTo("p-001"))
                .verifyComplete();
    }

    @Test
    void paciente_no_encontrado_lanza_excepcion() {
        var repo = mock(PacienteRepositoryPort.class);
        when(repo.buscarPorId("no-existe")).thenReturn(Mono.empty());

        var service = new ObtenerPacienteService(repo);

        StepVerifier.create(service.obtener("no-existe"))
                .expectError(DominioException.class)
                .verify();
    }
}
