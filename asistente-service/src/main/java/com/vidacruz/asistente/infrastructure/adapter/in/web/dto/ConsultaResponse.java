package com.vidacruz.asistente.infrastructure.adapter.in.web.dto;

import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;

import java.util.List;

public record ConsultaResponse(
        String respuesta,
        List<Fuente> fuentes
) {
    public static ConsultaResponse desde(Respuesta respuesta) {
        return new ConsultaResponse(respuesta.respuesta(), respuesta.fuentes());
    }
}
