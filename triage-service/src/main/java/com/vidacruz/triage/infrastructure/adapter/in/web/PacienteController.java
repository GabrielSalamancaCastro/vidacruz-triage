package com.vidacruz.triage.infrastructure.adapter.in.web;

import com.vidacruz.triage.application.port.in.RegistrarPacienteUseCase;
import com.vidacruz.triage.domain.model.SignosVitales;
import com.vidacruz.triage.infrastructure.adapter.in.web.dto.PacienteResponse;
import com.vidacruz.triage.infrastructure.adapter.in.web.dto.RegistrarPacienteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/triage/pacientes")
public class PacienteController {
    private final RegistrarPacienteUseCase registrarPacienteUseCase;

    public PacienteController(RegistrarPacienteUseCase registrarPacienteUseCase) {
        this.registrarPacienteUseCase = registrarPacienteUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<PacienteResponse>> registrar(
            @Valid @RequestBody RegistrarPacienteRequest request
    ) {

        var signos = new SignosVitales(
                request.frecuenciaCardiaca(),
                request.presionSistolica(),
                request.presionDiastolica(),
                request.spo2(),
                request.temperatura(),
                request.dolor(),
                request.gcs()
        );

        return registrarPacienteUseCase
                .registrar(request.nombreCompleto(), request.edad(), signos, request.quejaPrincipal())
                .map(PacienteResponse::desde)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}
