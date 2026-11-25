package com.vidacruz.triage.domain.model;

public enum NivelTriaje {
    CRITICO(1, "CRITICO", 5),
    URGENTE(2, "URGENTE", 10),
    URGENTE_2(3, "URGENTE_2", 30),
    NO_URGENTE(4, "NO_URGENTE", 60),
    NO_URGENTE_2(5, "NO_URGENTE_2", 120);

    private final int orden;
    private final String nombre;
    private final int slaMinutos;

    NivelTriaje(int orden, String nombre, int slaMinutos) {
        this.orden = orden;
        this.nombre = nombre;
        this.slaMinutos = slaMinutos;
    }

    public int getOrden() {
        return orden;
    }

    public String getNombre() {
        return nombre;
    }

    public int getSlaMinutos() {
        return slaMinutos;
    }

    /**
     * Indica si este nivel es más grave que otro.
     * Útil para implementar la regla "mayor gravedad gana" de RN-01.
     */
    public boolean esMasGraveQue(NivelTriaje otro) {
        return this.orden < otro.orden;
    }
}
