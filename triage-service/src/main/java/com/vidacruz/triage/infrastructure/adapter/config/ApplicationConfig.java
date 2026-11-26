package com.vidacruz.triage.infrastructure.adapter.config;

import com.vidacruz.triage.application.port.out.AuditoriaRepositoryPort;
import com.vidacruz.triage.application.port.out.EventPublisherPort;
import com.vidacruz.triage.application.port.out.PacienteRepositoryPort;
import com.vidacruz.triage.application.service.CambiarEstadoService;
import com.vidacruz.triage.application.service.ObtenerPacienteService;
import com.vidacruz.triage.application.service.RegistrarPacienteService;
import com.vidacruz.triage.domain.service.ClasificadorTriaje;
import com.vidacruz.triage.domain.service.SlaCalculator;
import com.vidacruz.triage.domain.service.TransicionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public ClasificadorTriaje clasificadorTriaje() {
        return new ClasificadorTriaje();
    }

    @Bean
    public SlaCalculator slaCalculator() {
        return new SlaCalculator();
    }

    @Bean
    public RegistrarPacienteService registrarPacienteService(
            ClasificadorTriaje clasificador,
            PacienteRepositoryPort pacienteRepository,
            EventPublisherPort eventPublisher
    ) {
        return new RegistrarPacienteService(clasificador, pacienteRepository, eventPublisher);
    }

    @Bean
    public TransicionValidator transicionValidator() {
        return new TransicionValidator();
    }

    @Bean
    public CambiarEstadoService cambiarEstadoService(
            PacienteRepositoryPort pacienteRepository,
            AuditoriaRepositoryPort auditoriaRepository,
            EventPublisherPort eventPublisher,
            TransicionValidator transicionValidator) {
        return new CambiarEstadoService(pacienteRepository, auditoriaRepository, eventPublisher, transicionValidator);
    }

    @Bean
    public ObtenerPacienteService obtenerPacienteService(PacienteRepositoryPort pacienteRepository) {
        return new ObtenerPacienteService(pacienteRepository);
    }
}
