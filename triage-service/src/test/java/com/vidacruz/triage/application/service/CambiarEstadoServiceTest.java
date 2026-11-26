package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.out.AuditoriaRepositoryPort;
import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.exception.TransicionInvalidaException;
import com.vidacruz.triage.domain.model.*;
import com.vidacruz.triage.domain.service.TransicionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CambiarEstadoServiceTest {
    private PacienteRepositoryPort pacienteRepository;
    private AuditoriaRepositoryPort auditoriaRepository;
    private EventPublisherPort eventPublisher;
    private CambiarEstadoService service;

    private static final Paciente PACIENTE_REGISTRADO = new Paciente(
            "p-001", "Juan Pérez", 45,
            new SignosVitales(130, 180, 110, 97, 36.8, 8, 15),
            "Dolor torácico", NivelTriaje.CRITICO,
            EstadoPaciente.REGISTRADO, false, Instant.parse("2026-10-29T15:00:00Z")
    );

    @BeforeEach
    void setUp() {
        pacienteRepository = mock(PacienteRepositoryPort.class);
        auditoriaRepository = mock(AuditoriaRepositoryPort.class);
        eventPublisher = mock(EventPublisherPort.class);
        service = new CambiarEstadoService(
                pacienteRepository, auditoriaRepository, eventPublisher, new TransicionValidator());

        when(pacienteRepository.guardar(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(auditoriaRepository.guardar(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(eventPublisher.publicar(any())).thenReturn(Mono.empty());
    }

    @Test
    void cambia_de_registrado_a_en_consulta_y_guarda_auditoria() {
        when(pacienteRepository.buscarPorId("p-001")).thenReturn(Mono.just(PACIENTE_REGISTRADO));

        StepVerifier.create(service.cambiarEstado("p-001", "EN_CONSULTA"))
                .assertNext(p -> assertThat(p.estado()).isEqualTo(EstadoPaciente.EN_CONSULTA))
                .verifyComplete();

        verify(pacienteRepository, times(1)).guardar(any());
        verify(auditoriaRepository, times(1)).guardar(any());
        verify(eventPublisher, times(1)).publicar(any());
    }

    @Test
    void transicion_invalida_lanza_excepcion() {
        var enConsulta = new Paciente(
                "p-001", "Juan Pérez", 45,
                new SignosVitales(130, 180, 110, 97, 36.8, 8, 15),
                "Dolor torácico", NivelTriaje.CRITICO,
                EstadoPaciente.EN_CONSULTA, false, Instant.now()
        );
        when(pacienteRepository.buscarPorId("p-001")).thenReturn(Mono.just(enConsulta));

        StepVerifier.create(service.cambiarEstado("p-001", "REGISTRADO"))
                .expectError(TransicionInvalidaException.class)
                .verify();

        verify(auditoriaRepository, never()).guardar(any());
    }

    @Test
    void paciente_no_encontrado_lanza_excepcion() {
        when(pacienteRepository.buscarPorId("no-existe")).thenReturn(Mono.empty());

        StepVerifier.create(service.cambiarEstado("no-existe", "EN_CONSULTA"))
                .expectError(DominioException.class)
                .verify();
    }

    @Test
    void estado_invalido_lanza_excepcion() {
        StepVerifier.create(service.cambiarEstado("p-001", "ESTADO_INEXISTENTE"))
                .expectError(DominioException.class)
                .verify();
    }
}
