package com.equipo26.financeai.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
   Modelo estándar para las respuestas de error de la API.
 */
@Schema(name = "ErrorResponse", description = "Estructura estandar utilizada para representar errores de la API")
public record ErrorResponse(
        @Schema(description = "Código HTTP correspondiente al error.", examples = "400")
        int status,
        @Schema(description = "Mensaje descriptivo del error.", examples = "El formato de los datos enviados no es válido")
        String mensaje,
        @Schema(description = "Fecha y hora en la que ocurrio el error.", examples = "2026-08-09T23:15:39")
        LocalDateTime fecha
) {
}
