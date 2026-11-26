package com.vidacruz.triage.infrastructure.config;

import com.vidacruz.triage.application.port.in.EscalarPacienteUseCase;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SlaScheduler {
    private static final Logger log = LoggerFactory.getLogger(SlaScheduler.class);

    private final PacienteRepositoryPort pacienteRepository;
    private final EscalarPacienteUseCase escalarPacienteUseCase;

    public SlaScheduler(
            PacienteRepositoryPort pacienteRepository,
            EscalarPacienteUseCase escalarPacienteUseCase
    ) {
        this.pacienteRepository = pacienteRepository;
        this.escalarPacienteUseCase = escalarPacienteUseCase;
    }

    @Scheduled(fixedDelayString = "${triage.sla.check-interval-ms:30000}")
    public void evaluarPacientesActivos() {
        log.debug("SLA Scheduler: evaluando pacientes activos...");

        pacienteRepository.buscarRegistradosSinEscalar()
                .flatMap(paciente -> escalarPacienteUseCase.escalarSiCorresponde(paciente.id()))
                .doOnNext(escalado -> log.info(
                        "Paciente escalado: id={}, nivel={}",
                        escalado.id(), escalado.nivelTriaje().getNombre()))
                .doOnError(err -> log.error("Error en SLA Scheduler", err))
                .subscribe(); // suscripción reactiva, no bloquea el scheduler
    }
}
