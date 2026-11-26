package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.in.CambiarEstadoUseCase;
import com.vidacruz.triage.application.port.out.AuditoriaRepositoryPort;
import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.model.Auditoria;
import com.vidacruz.triage.domain.model.EventoDominio;
import com.vidacruz.triage.domain.model.EstadoPaciente;
import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.service.TransicionValidator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class CambiarEstadoService implements CambiarEstadoUseCase {

    private final PacienteRepositoryPort pacienteRepository;
    private final AuditoriaRepositoryPort auditoriaRepository;
    private final EventPublisherPort eventPublisher;
    private final TransicionValidator transicionValidator;

    public CambiarEstadoService(
            PacienteRepositoryPort pacienteRepository,
            AuditoriaRepositoryPort auditoriaRepository,
            EventPublisherPort eventPublisher,
            TransicionValidator transicionValidator
    ) {
        this.pacienteRepository = pacienteRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.eventPublisher = eventPublisher;
        this.transicionValidator = transicionValidator;
    }

    @Override
    public Mono<Paciente> cambiarEstado(String pacienteId, String nuevoEstado) {
        return Mono.defer(() -> {
            EstadoPaciente nuevo = parsearEstado(nuevoEstado);

            return pacienteRepository.buscarPorId(pacienteId)
                    .switchIfEmpty(Mono.error(new DominioException("id",
                            "Paciente no encontrado: " + pacienteId)))
                    .flatMap(paciente -> {
                        transicionValidator.validar(paciente.estado(), nuevo);

                        var actualizado = new Paciente(
                                paciente.id(),
                                paciente.nombreCompleto(),
                                paciente.edad(),
                                paciente.signosVitales(),
                                paciente.quejaPrincipal(),
                                paciente.nivelTriaje(),
                                nuevo,
                                paciente.escalado(),
                                paciente.fechaRegistro()
                        );

                        return pacienteRepository.guardar(actualizado)
                                .flatMap(guardado -> guardarAuditoria(paciente, guardado)
                                        .then(publicarEvento(guardado))
                                        .thenReturn(guardado));
                    });
        });
    }

    private Mono<Auditoria> guardarAuditoria(Paciente anterior, Paciente nuevo) {
        return auditoriaRepository.guardar(new Auditoria(
                UUID.randomUUID().toString(),
                nuevo.id(),
                anterior.estado().name(),
                nuevo.estado().name(),
                tipoEvento(nuevo.estado()),
                Instant.now()
        ));
    }

    private Mono<Void> publicarEvento(Paciente paciente) {
        return eventPublisher.publicar(new EventoDominio(
                tipoEvento(paciente.estado()),
                paciente.id(),
                Instant.now(),
                Map.of("estado", paciente.estado().name(), "nivel", paciente.nivelTriaje().getNombre())
        ));
    }

    private String tipoEvento(EstadoPaciente estado) {
        return switch (estado) {
            case EN_CONSULTA -> "paciente_en_consulta";
            case ALTA -> "paciente_alta";
            case REGISTRADO -> "paciente_registrado";
        };
    }

    private EstadoPaciente parsearEstado(String estado) {
        try {
            return EstadoPaciente.valueOf(estado);
        } catch (IllegalArgumentException e) {
            throw new DominioException("estado", "Estado inválido: " + estado);
        }
    }
}
