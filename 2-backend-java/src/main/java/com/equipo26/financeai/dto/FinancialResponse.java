package com.equipo26.financeai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * DTO que mapea el JSON de salida que la API le devolverá al frontend.
 * Contiene el diagnóstico de la IA y las recomendaciones del Backend.
 */
@Data
@Schema(description = "Respuesta con el análisis financiero del usuario")
@JsonPropertyOrder({ "id", "perfil_financiero", "probabilidad", "resumen_gastos", "recomendaciones" })
public class FinancialResponse {

    @Schema(description = "ID del perfil generado automáticamente", example = "1")
    private Long id;

    // Diagnóstico de la IA (Ej: "En observacion", "Saludable")
    @Schema(description = "Perfil financiero asignado al usuario", example = "Saludable")
    @JsonProperty("perfil_financiero")
    private String perfilFinanciero;

    // Porcentaje de certeza de la IA (Ej: 0.85)
    @Schema(description = "Probabilidad estimada del análisis financiero", example = "0.85", minimum = "0", maximum = "1")
    @JsonProperty("probabilidad")
    private Double probabilidad;

    // Resumen de gastos agrupados por categoría (Ej: "Alimentacion": 420)
    @Schema(description = "Resumen de los gastos clasificados del usuario", example = "{\"Alimentación\":420.00,\"Transporte\":150.00}")
    @JsonProperty("resumen_gastos")
    private Map<String, Double> resumenGastos;

    // Los consejos financieros van aca
    @Schema(description = "Lista de recomendaciones financieras generadas", example = "[\"Tu nivel de endeudamiento está dentro de un rango saludable.\", \"Te recomendamos destinar al menos el 20% de tu ingreso mensual ($3000.00) a tu fondo de ahorro.\"]"
    )
    @JsonProperty("recomendaciones")
    private List<String> recomendaciones;
}