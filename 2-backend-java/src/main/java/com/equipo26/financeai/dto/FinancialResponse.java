package com.equipo26.financeai.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * DTO que mapea el JSON de salida que la API le devolverá al frontend.
 * Contiene el diagnóstico de la IA y las recomendaciones del Backend.
 */
@Data
public class FinancialResponse {

    // Diagnóstico de la IA (Ej: "En observacion", "Saludable")
    private String perfil_financiero;

    // Porcentaje de certeza de la IA (Ej: 0.85)
    private Double probabilidad;

    // Resumen de gastos agrupados por categoría (Ej: "Alimentacion": 420)
    private Map<String, Double> resumen_gastos;

    // Los consejos financieros van aca
    private List<String> recomendaciones;
}