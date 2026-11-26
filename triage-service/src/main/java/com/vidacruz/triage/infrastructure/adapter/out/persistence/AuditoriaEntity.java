package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("auditoria")
public class AuditoriaEntity {
    @Id
    private String id;

    @Column("paciente_id")
    private String pacienteId;

    @Column("estado_anterior")
    private String estadoAnterior;

    @Column("estado_nuevo")
    private String estadoNuevo;

    @Column("tipo_evento")
    private String tipoEvento;

    @Column("timestamp")
    private Instant timestamp;

    public AuditoriaEntity() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
