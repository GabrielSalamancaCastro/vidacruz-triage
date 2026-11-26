package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.model.EventoDominio;
import com.vidacruz.triage.domain.model.EstadoPaciente;
import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.model.SignosVitales;
import com.vidacruz.triage.domain.service.ClasificadorTriaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrarPacienteServiceTest {
    private ClasificadorTriaje clasificador;
    private PacienteRepositoryPort pacienteRepository;
    private EventPublisherPort eventPublisher;
    private RegistrarPacienteService service;

    @BeforeEach
    void setUp() {
        clasificador = new ClasificadorTriaje();
        pacienteRepository = mock(PacienteRepositoryPort.class);
        eventPublisher = mock(EventPublisherPort.class);
        service = new RegistrarPacienteService(clasificador, pacienteRepository, eventPublisher);

        when(pacienteRepository.guardar(any(Paciente.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(eventPublisher.publicar(any(EventoDominio.class)))
                .thenReturn(Mono.empty());
    }

    private static SignosVitales signosE1() {
        // FC=130 → CRITICO
        return new SignosVitales(130, 180, 110, 97, 36.8, 8, 15);
    }

    @Test
    void registra_paciente_con_id_nivel_estado_y_fecha() {
        var resultado = service.registrar("Juan Pérez", 45, signosE1(), "Dolor torácico");

        StepVerifier.create(resultado)
                .assertNext(p -> {
                    assertThat(p.id()).isNotBlank();
                    assertThat(p.nombreCompleto()).isEqualTo("Juan Pérez");
                    assertThat(p.edad()).isEqualTo(45);
                    assertThat(p.nivelTriaje()).isEqualTo(NivelTriaje.CRITICO);
                    assertThat(p.estado()).isEqualTo(EstadoPaciente.REGISTRADO);
                    assertThat(p.escalado()).isFalse();
                    assertThat(p.fechaRegistro()).isNotNull();
                    assertThat(p.slaMinutos()).isEqualTo(5);
                })
                .verifyComplete();
    }

    @Test
    void llama_a_guardar_una_vez() {
        service.registrar("Juan Pérez", 45, signosE1(), "Dolor torácico")
                .block();

        verify(pacienteRepository, times(1)).guardar(any(Paciente.class));
    }

    @Test
    void publica_evento_paciente_registrado_una_vez() {
        service.registrar("Juan Pérez", 45, signosE1(), "Dolor torácico")
                .block();

        ArgumentCaptor<EventoDominio> captor = ArgumentCaptor.forClass(EventoDominio.class);
        verify(eventPublisher, times(1)).publicar(captor.capture());

        EventoDominio evento = captor.getValue();
        assertThat(evento.eventType()).isEqualTo("paciente_registrado");
        assertThat(evento.pacienteId()).isNotBlank();
        assertThat(evento.payload())
                .containsEntry("nivel", "CRITICO")
                .containsEntry("slaMinutos", 5)
                .containsEntry("estado", "REGISTRADO");
    }

    @Test
    void si_falla_guardar_no_publica_evento() {
        when(pacienteRepository.guardar(any(Paciente.class)))
                .thenReturn(Mono.error(new RuntimeException("DB caída")));

        StepVerifier.create(service.registrar("Juan Pérez", 45, signosE1(), "Dolor torácico"))
                .expectErrorMatches(e -> e.getMessage().equals("DB caída"))
                .verify();

        verify(eventPublisher, never()).publicar(any());
    }

    @Test
    void si_falla_publicar_propaga_error() {
        when(eventPublisher.publicar(any(EventoDominio.class)))
                .thenReturn(Mono.error(new RuntimeException("Kafka caído")));

        StepVerifier.create(service.registrar("Juan Pérez", 45, signosE1(), "Dolor torácico"))
                .expectErrorMatches(e -> e.getMessage().equals("Kafka caído"))
                .verify();
    }

    @Test
    void clasifica_correctamente_un_paciente_no_urgente() {
        var sv = new SignosVitales(72, 120, 80, 99, 36.5, 2, 15);

        StepVerifier.create(service.registrar("Ana López", 30, sv, "Herida por cortar"))
                .assertNext(p -> assertThat(p.nivelTriaje()).isEqualTo(NivelTriaje.NO_URGENTE))
                .verifyComplete();
    }
}
