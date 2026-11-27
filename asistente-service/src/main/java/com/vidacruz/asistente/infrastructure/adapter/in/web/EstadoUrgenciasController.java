package com.vidacruz.asistente.infrastructure.adapter.in.web;

import com.vidacruz.asistente.application.port.in.ConsultarEstadoUrgenciasUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/asistente")
public class EstadoUrgenciasController {

    private final ConsultarEstadoUrgenciasUseCase useCase;

    public EstadoUrgenciasController(ConsultarEstadoUrgenciasUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/estado-urgencias")
    public Mono<Map<String, Long>> estado() {
        return useCase.consultarEstado();
    }
}
