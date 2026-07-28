package com.equipo26.financeai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * DTO que mapea el JSON de salida que la API le devolverá al frontend.
 * Contiene el diagnóstico de la IA y las recomendaciones del Backend.
 */
@Data
@JsonPropertyOrder({ "perfil_financiero", "probabilidad", "resumen_gastos", "recomendaciones" })
public class FinancialResponse {

    // Diagnóstico de la IA (Ej: "En observacion", "Saludable")
    @JsonProperty("perfil_financiero")
    private String perfilFinanciero;

    // Porcentaje de certeza de la IA (Ej: 0.85)
    @JsonProperty("probabilidad")
    private Double probabilidad;

    // Resumen de gastos agrupados por categoría (Ej: "Alimentacion": 420)
    @JsonProperty("resumen_gastos")
    private Map<String, Double> resumenGastos;

    // Los consejos financieros van aca
    @JsonProperty("recomendaciones")
    private List<String> recomendaciones;
}