package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.in.RegistrarPacienteUseCase;
import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.model.EventoDominio;
import com.vidacruz.triage.domain.model.EstadoPaciente;
import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.model.SignosVitales;
import com.vidacruz.triage.domain.service.ClasificadorTriaje;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.vidacruz.triage.application.port.in.RegistrarPacienteUseCase;

public class RegistrarPacienteService implements RegistrarPacienteUseCase {

    private final ClasificadorTriaje clasificador;
    private final PacienteRepositoryPort pacienteRepository;
    private final EventPublisherPort eventPublisher;

    public RegistrarPacienteService(
            ClasificadorTriaje clasificador,
            PacienteRepositoryPort pacienteRepository,
            EventPublisherPort eventPublisher
    ){
        this.clasificador = clasificador;
        this.pacienteRepository = pacienteRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Paciente> registrar(
            String nombreCompleto,
            int edad,
            SignosVitales signosVitales,
            String quejaPrincipal
    ) {
        return Mono.defer(() -> {
            // 1. Clasificar (RN-01). Es síncrono porque el dominio es puro.
            NivelTriaje nivel = clasificador.clasificar(signosVitales, quejaPrincipal);

            // 2. Construir el Paciente. El constructor valida nombre, edad, queja.
            Paciente paciente = new Paciente(
                    UUID.randomUUID().toString(),
                    nombreCompleto,
                    edad,
                    signosVitales,
                    quejaPrincipal,
                    nivel,
                    EstadoPaciente.REGISTRADO,
                    false,                 // escalado = false al registrar
                    Instant.now()
            );

            // 3. Persistir y, cuando termine, publicar el evento.
            //    chain: save → then(event) → thenReturn(paciente)
            return pacienteRepository.guardar(paciente)
                    .flatMap(guardado -> eventPublisher
                            .publicar(construirEventoRegistrado(guardado))
                            .thenReturn(guardado));
        });
    }

    private EventoDominio construirEventoRegistrado(Paciente paciente) {
        return new EventoDominio(
                "paciente_registrado",
                paciente.id(),
                paciente.fechaRegistro(),
                Map.of(
                        "nivel", paciente.nivelTriaje().getNombre(),
                        "slaMinutos", paciente.slaMinutos(),
                        "estado", paciente.estado().name(),
                        "fechaRegistro", paciente.fechaRegistro().toString()
                )
        );
    }
}
