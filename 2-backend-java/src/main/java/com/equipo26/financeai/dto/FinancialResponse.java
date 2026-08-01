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
@JsonPropertyOrder({ "perfil_financiero", "probabilidad", "resumen_gastos", "recomendaciones" })
public class FinancialResponse {

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
    @Schema(description = "Lista de recomendaciones financieras generadas", example = "✅ Tu nivel de endeudamiento está dentro de un rango saludable.")
    @JsonProperty("recomendaciones")
    private List<String> recomendaciones;
}