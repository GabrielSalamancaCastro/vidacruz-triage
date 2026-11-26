package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import com.vidacruz.triage.domain.model.Auditoria;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaMapper {
    public AuditoriaEntity aEntity(Auditoria auditoria) {
        var entity = new AuditoriaEntity();
        entity.setId(auditoria.id());
        entity.setPacienteId(auditoria.pacienteId());
        entity.setEstadoAnterior(auditoria.estadoAnterior());
        entity.setEstadoNuevo(auditoria.estadoNuevo());
        entity.setTipoEvento(auditoria.tipoEvento());
        entity.setTimestamp(auditoria.timestamp());
        return entity;
    }

    public Auditoria aDominio(AuditoriaEntity entity) {
        return new Auditoria(
                entity.getId(),
                entity.getPacienteId(),
                entity.getEstadoAnterior(),
                entity.getEstadoNuevo(),
                entity.getTipoEvento(),
                entity.getTimestamp()
        );
    }
}
