package com.vidacruz.triage.infrastructure.adapter.out.persistence;

import com.vidacruz.triage.domain.model.EstadoPaciente;
import com.vidacruz.triage.domain.model.NivelTriaje;
import com.vidacruz.triage.domain.model.Paciente;
import com.vidacruz.triage.domain.model.SignosVitales;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {
    public PacienteEntity aEntity(Paciente paciente) {
        var entity = new PacienteEntity();
        entity.setId(paciente.id());
        entity.setNombreCompleto(paciente.nombreCompleto());
        entity.setEdad(paciente.edad());
        entity.setFrecuenciaCardiaca(paciente.signosVitales().frecuenciaCardiaca());
        entity.setPresionSistolica(paciente.signosVitales().presionSistolica());
        entity.setPresionDiastolica(paciente.signosVitales().presionDiastolica());
        entity.setSpo2(paciente.signosVitales().spo2());
        entity.setTemperatura(paciente.signosVitales().temperatura());
        entity.setDolor(paciente.signosVitales().dolor());
        entity.setGcs(paciente.signosVitales().gcs());
        entity.setQuejaPrincipal(paciente.quejaPrincipal());
        entity.setNivelTriaje(paciente.nivelTriaje().name());
        entity.setEstado(paciente.estado().name());
        entity.setEscalado(paciente.escalado());
        entity.setFechaRegistro(paciente.fechaRegistro());
        return entity;
    }

    public Paciente aDominio(PacienteEntity entity) {
        var signos = new SignosVitales(
                entity.getFrecuenciaCardiaca(),
                entity.getPresionSistolica(),
                entity.getPresionDiastolica(),
                entity.getSpo2(),
                entity.getTemperatura(),
                entity.getDolor(),
                entity.getGcs()
        );
        return new Paciente(
                entity.getId(),
                entity.getNombreCompleto(),
                entity.getEdad(),
                signos,
                entity.getQuejaPrincipal(),
                NivelTriaje.valueOf(entity.getNivelTriaje()),
                EstadoPaciente.valueOf(entity.getEstado()),
                Boolean.TRUE.equals(entity.getEscalado()),
                entity.getFechaRegistro()
        );
    }
}
