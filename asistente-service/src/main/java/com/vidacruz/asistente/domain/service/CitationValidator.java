package com.vidacruz.asistente.domain.service;

import com.vidacruz.asistente.domain.model.Fuente;
import com.vidacruz.asistente.domain.model.Respuesta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CitationValidator {

    /**
     * Valida la respuesta del LLM contra el contexto recuperado.
     *
     * @param respuesta la respuesta generada por el LLM
     * @param contexto  las fuentes recuperadas del vector store
     * @return la respuesta original si es válida; {@link Respuesta#sinProtocolo()} si no
     */
    public Respuesta validar(Respuesta respuesta, List<Fuente> contexto) {
        if (respuesta == null || !respuesta.tieneFuentes()) {
            return Respuesta.sinProtocolo();
        }

        Set<String> protocolosValidos = contexto.stream()
                .map(Fuente::protocolo)
                .collect(Collectors.toSet());

        boolean todasValidas = respuesta.fuentes().stream()
                .allMatch(f -> protocolosValidos.contains(f.protocolo()));

        if (!todasValidas) {
            return Respuesta.sinProtocolo();
        }

        return respuesta;
    }
}
