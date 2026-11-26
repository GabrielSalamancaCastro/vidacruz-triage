package com.vidacruz.triage.infrastructure.adapter.in.web;

import com.vidacruz.triage.application.port.in.ReporteOperacionUseCase;
import com.vidacruz.triage.domain.model.ReporteHora;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Instant;

@RestController
@RequestMapping("/triage/reportes")
public class ReporteOperacionController {

    private final ReporteOperacionUseCase useCase;

    public ReporteOperacionController(ReporteOperacionUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/operacion")
    public Flux<ReporteHora> reporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta
    ) {
        return useCase.generarReporte(desde, hasta);
    }
}
