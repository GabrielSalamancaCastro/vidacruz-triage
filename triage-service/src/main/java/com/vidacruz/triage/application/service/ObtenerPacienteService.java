package com.vidacruz.triage.application.service;

import com.vidacruz.triage.application.port.in.ObtenerPacienteUseCase;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.exception.DominioException;
import com.vidacruz.triage.domain.model.Paciente;
import reactor.core.publisher.Mono;

public class ObtenerPacienteService implements ObtenerPacienteUseCase {

    private final PacienteRepositoryPort pacienteRepository;

    public ObtenerPacienteService(PacienteRepositoryPort pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Mono<Paciente> obtener(String pacienteId) {
        return pacienteRepository.buscarPorId(pacienteId)
                .switchIfEmpty(Mono.error(new DominioException("id",
                        "Paciente no encontrado: " + pacienteId)));
    }
}
