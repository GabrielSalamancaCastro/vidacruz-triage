package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.domain.model.Paciente;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PacientePersistenceAdapter implements PacienteRepositoryPort {
    private final PacienteRepository repository;
    private final PacienteMapper mapper;

    public PacientePersistenceAdapter(PacienteRepository repository, PacienteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Paciente> guardar(Paciente paciente) {
        return repository.save(mapper.aEntity(paciente))
                .map(mapper::aDominio);
    }
}
