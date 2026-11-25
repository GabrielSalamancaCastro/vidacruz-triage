package com.vidacruz.triage.domain.exception;

public class DominioException extends RuntimeException{
    private final String campo;

    public DominioException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
