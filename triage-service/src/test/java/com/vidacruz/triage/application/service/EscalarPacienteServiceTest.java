package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.out.AuditoriaRepositoryPort;
import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EscalarPacienteServiceTest {
    private PacienteRepositoryPort pacienteRepository;
    private AuditoriaRepositoryPort auditoriaRepository;
    private EventPublisherPort eventPublisher;
    private EscalarPacienteService service;

    private static final Paciente PACIENTE_REGISTRADO_SLA_SUPERADO = new Paciente(
            "p-001", "Juan Pérez", 45,
            new SignosVitales(130, 180, 110, 97, 36.8, 8, 15),
            "Dolor torácico", NivelTriaje.CRITICO,
            EstadoPaciente.REGISTRADO, false,
            Instant.now().minus(10, ChronoUnit.MINUTES) // SLA CRITICO = 5 min, ya superado
    );

    private static final Paciente PACIENTE_EN_CONSULTA = new Paciente(
            "p-002", "Ana López", 30,
            new SignosVitales(72, 120, 80, 99, 36.5, 2, 15),
            "Herida", NivelTriaje.NO_URGENTE,
            EstadoPaciente.EN_CONSULTA, false,
            Instant.now().minus(10, ChronoUnit.MINUTES)
    );

    @BeforeEach
    void setUp() {
        pacienteRepository = mock(PacienteRepositoryPort.class);
        auditoriaRepository = mock(AuditoriaRepositoryPort.class);
        eventPublisher = mock(EventPublisherPort.class);
        service = new EscalarPacienteService(pacienteRepository, auditoriaRepository, eventPublisher);

        when(auditoriaRepository.guardar(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(eventPublisher.publicar(any())).thenReturn(Mono.empty());
    }

    @Test
    void escala_paciente_registrado_con_sla_superado() {
        when(pacienteRepository.buscarPorId("p-001")).thenReturn(Mono.just(PACIENTE_REGISTRADO_SLA_SUPERADO));
        when(pacienteRepository.marcarEscaladoSiCorresponde("p-001")).thenReturn(Mono.just(1));

        StepVerifier.create(service.escalarSiCorresponde("p-001"))
                .assertNext(p -> {
                    assertThat(p.escalado()).isTrue();
                    assertThat(p.id()).isEqualTo("p-001");
                })
                .verifyComplete();

        verify(auditoriaRepository, times(1)).guardar(any());
        verify(eventPublisher, times(1)).publicar(any());
    }

    @Test
    void segundo_intento_no_duplica_evento_ni_auditoria() {
        when(pacienteRepository.buscarPorId("p-001")).thenReturn(Mono.just(PACIENTE_REGISTRADO_SLA_SUPERADO));
        // Simular que otro proceso ya lo escaló (UPDATE afecta 0 filas)
        when(pacienteRepository.marcarEscaladoSiCorresponde("p-001")).thenReturn(Mono.just(0));

        StepVerifier.create(service.escalarSiCorresponde("p-001"))
                .verifyComplete(); // Mono.empty()

        verify(auditoriaRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicar(any());
    }

    @Test
    void paciente_en_consulta_no_se_escala() {
        when(pacienteRepository.buscarPorId("p-002")).thenReturn(Mono.just(PACIENTE_EN_CONSULTA));

        StepVerifier.create(service.escalarSiCorresponde("p-002"))
                .verifyComplete();

        verify(pacienteRepository, never()).marcarEscaladoSiCorresponde(any());
        verify(eventPublisher, never()).publicar(any());
    }

    @Test
    void paciente_no_encontrado_no_escala() {
        when(pacienteRepository.buscarPorId("no-existe")).thenReturn(Mono.empty());

        StepVerifier.create(service.escalarSiCorresponde("no-existe"))
                .verifyComplete();

        verify(pacienteRepository, never()).marcarEscaladoSiCorresponde(any());
    }
}
