package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.in.EscalarPacienteUseCase;
import com.vidacruz.triage.application.port.out.AuditoriaRepositoryPort;
import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.model.Auditoria;
import com.vidacruz.triage.domain.model.EventoDominio;
import com.vidacruz.triage.domain.model.Paciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EscalarPacienteService implements EscalarPacienteUseCase{

    private static final Logger log = LoggerFactory.getLogger(EscalarPacienteService.class);

    private final PacienteRepositoryPort pacienteRepository;
    private final AuditoriaRepositoryPort auditoriaRepository;
    private final EventPublisherPort eventPublisher;

    public EscalarPacienteService(
            PacienteRepositoryPort pacienteRepository,
            AuditoriaRepositoryPort auditoriaRepository,
            EventPublisherPort eventPublisher
    ) {
        this.pacienteRepository = pacienteRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Paciente> escalarSiCorresponde(String pacienteId) {
        return pacienteRepository.buscarPorId(pacienteId)
                .flatMap(paciente -> {
                    // Verificar que el paciente esté en REGISTRADO y no escalado
                    if (!paciente.estaActivo() || paciente.escalado()) {
                        log.debug("Paciente {} no requiere escalamiento (estado={}, escalado={})",
                                pacienteId, paciente.estado(), paciente.escalado());
                        return Mono.empty();
                    }

                    // Verificar que haya superado el SLA
                    var ahora = Instant.now();
                    var fechaLimite = paciente.fechaRegistro().plusSeconds(paciente.slaMinutos() * 60L);
                    if (ahora.isBefore(fechaLimite)) {
                        log.debug("Paciente {} aún no supera su SLA (límite={})", pacienteId, fechaLimite);
                        return Mono.empty();
                    }

                    // UPDATE condicional atómico (idempotencia)
                    return pacienteRepository.marcarEscaladoSiCorresponde(pacienteId)
                            .flatMap(filasAfectadas -> {
                                if (filasAfectadas == 0) {
                                    // Otro proceso ya lo escaló, o el paciente cambió de estado
                                    log.debug("Paciente {} ya fue escalado por otro proceso", pacienteId);
                                    return Mono.empty();
                                }

                                // El UPDATE afectó 1 fila: este proceso es el que escala
                                var pacienteEscalado = new Paciente(
                                        paciente.id(),
                                        paciente.nombreCompleto(),
                                        paciente.edad(),
                                        paciente.signosVitales(),
                                        paciente.quejaPrincipal(),
                                        paciente.nivelTriaje(),
                                        paciente.estado(),
                                        true, // escalado = true
                                        paciente.fechaRegistro()
                                );

                                return guardarAuditoria(pacienteEscalado)
                                        .then(publicarEvento(pacienteEscalado))
                                        .thenReturn(pacienteEscalado);
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Paciente {} no encontrado o no escalado", pacienteId);
                    return Mono.empty();
                }));
    }

    private Mono<Auditoria> guardarAuditoria(Paciente paciente) {
        return auditoriaRepository.guardar(new Auditoria(
                UUID.randomUUID().toString(),
                paciente.id(),
                paciente.estado().name(),
                paciente.estado().name(), // mismo estado, solo cambia flag
                "paciente_escalado",
                Instant.now()
        ));
    }

    private Mono<Void> publicarEvento(Paciente paciente) {
        return eventPublisher.publicar(new EventoDominio(
                "paciente_escalado",
                paciente.id(),
                Instant.now(),
                Map.of(
                        "nivel", paciente.nivelTriaje().getNombre(),
                        "slaMinutos", paciente.slaMinutos(),
                        "escaladoEn", Instant.now().toString()
                )
        ));
    }
}
