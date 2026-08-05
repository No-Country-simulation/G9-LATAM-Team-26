package com.equipo26.financeai.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FrecuenciaAhorro {
    NULA("Nula"),
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    private final String valor;

    FrecuenciaAhorro(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    // Este metodo permite que Spring acepte "Media" o "media" ignorando mayúsculas,
    // pero rechace cualquier otra palabra que no esté en el Enum.
    @JsonCreator
    public static FrecuenciaAhorro fromString(String text) {
        for (FrecuenciaAhorro b : FrecuenciaAhorro.values()) {
            if (b.valor.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Valor inesperado para frecuencia_ahorro: " + text);
    }
}