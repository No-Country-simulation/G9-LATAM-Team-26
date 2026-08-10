package com.equipo26.financeai.exception;

import java.time.LocalDateTime;

/**
   Modelo estándar para las respuestas de error de la API.
 */
public record ErrorResponse(
        int status,
        String mensaje,
        LocalDateTime fecha
) {
}
