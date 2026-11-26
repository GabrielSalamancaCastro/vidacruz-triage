package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import com.vidacruz.triage.application.port.out.AuditoriaRepositoryPort;
import com.vidacruz.triage.domain.model.Auditoria;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AuditoriaPersistenceAdapter implements AuditoriaRepositoryPort {
    private final AuditoriaRepository repository;
    private final AuditoriaMapper mapper;

    public AuditoriaPersistenceAdapter(AuditoriaRepository repository, AuditoriaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Auditoria> guardar(Auditoria auditoria) {
        return repository.save(mapper.aEntity(auditoria))
                .map(mapper::aDominio);
    }

    @Override
    public Flux<Auditoria> buscarPorPaciente(String pacienteId) {
        return repository.findByPacienteId(pacienteId)
                .map(mapper::aDominio);
    }
}
