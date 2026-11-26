package com.vidacruz.triage.infrastructure.adapter.in.web;

import com.vidacruz.triage.application.port.in.CambiarEstadoUseCase;
import com.vidacruz.triage.application.port.in.ObtenerPacienteUseCase;
import com.vidacruz.triage.infrastructure.adapter.in.web.dto.CambiarEstadoRequest;
import com.vidacruz.triage.infrastructure.adapter.in.web.dto.PacienteDetalleResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/triage/pacientes")
public class PacienteEstadoController {
    private final CambiarEstadoUseCase cambiarEstadoUseCase;
    private final ObtenerPacienteUseCase obtenerPacienteUseCase;

    public PacienteEstadoController(
            CambiarEstadoUseCase cambiarEstadoUseCase,
            ObtenerPacienteUseCase obtenerPacienteUseCase
    ) {
        this.cambiarEstadoUseCase = cambiarEstadoUseCase;
        this.obtenerPacienteUseCase = obtenerPacienteUseCase;
    }

    @PatchMapping("/{id}/estado")
    public Mono<PacienteDetalleResponse> cambiarEstado(
            @PathVariable String id,
            @Valid @RequestBody CambiarEstadoRequest request
    ) {
        return cambiarEstadoUseCase.cambiarEstado(id, request.estado())
                .map(p -> PacienteDetalleResponse.desde(p, p.fechaRegistro()));
    }

    @GetMapping("/{id}")
    public Mono<PacienteDetalleResponse> obtener(@PathVariable String id) {
        return obtenerPacienteUseCase.obtener(id)
                .map(p -> PacienteDetalleResponse.desde(p, p.fechaRegistro()));
    }
}
