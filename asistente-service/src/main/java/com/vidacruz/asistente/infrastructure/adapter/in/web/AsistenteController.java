package com.vidacruz.asistente.infrastructure.adapter.in.web;

import com.vidacruz.asistente.application.port.in.ConsultarProtocoloUseCase;
import com.vidacruz.asistente.infrastructure.adapter.in.web.dto.ConsultaRequest;
import com.vidacruz.asistente.infrastructure.adapter.in.web.dto.ConsultaResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/asistente")
public class AsistenteController {

    private final ConsultarProtocoloUseCase useCase;

    public AsistenteController(ConsultarProtocoloUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/consulta")
    public Mono<ConsultaResponse> consultar(@Valid @RequestBody ConsultaRequest request) {
        return useCase.consultar(request.pregunta())
                .map(ConsultaResponse::desde);
    }
}
