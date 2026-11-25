package com.vidacruz.triage.domain.model;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum QuejaAdministrativa {
    DOCUMENTOS("documentos"),
    RECOLECCION_RESULTADOS("recolección de resultados"),
    RENOVACION_RECETARIO("renovación de recetario"),
    VACUNACION("vacunación"),
    SEGUIMIENTO("seguimiento");

    private final String texto;

    QuejaAdministrativa(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    private static final Set<String> TEXTOS_NORMALIZADOS =
            Stream.of(values())
                    .map(QuejaAdministrativa::getTexto)
                    .map(QuejaAdministrativa::normalizar)
                    .collect(Collectors.toUnmodifiableSet());

    public static boolean esAdministrativa(String quejaPrincipal) {
        if (quejaPrincipal == null) {
            return false;
        }
        return TEXTOS_NORMALIZADOS.contains(normalizar(quejaPrincipal));
    }

    private static String normalizar(String texto) {
        return texto.trim().toLowerCase(Locale.ROOT);
    }
}
