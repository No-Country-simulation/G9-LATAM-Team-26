package com.equipo26.financeai.service;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import com.equipo26.financeai.dto.TransactionDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialServiceImpl implements FinancialService {

    // Constante para el nivel de endeudamiento en cuestion
    private static final int UMBRAL_ENDEUDAMIENTO_CRITICO = 70;
    private static final int UMBRAL_ENDEUDAMIENTO_ALTO = 35; // Tu ajuste de equipo conservado
    private static final double PORCENTAJE_AHORRO_SUGERIDO = 0.20;
    private static final double UMBRAL_MAXIMO_OCIO = 0.15; // Máximo 15% del ingreso en ocio

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        FinancialResponse respuesta = new FinancialResponse();
        List<String> recomendaciones = new ArrayList<>();
        Map<String, Double> resumenGastos = new HashMap<>();

        // Extraccion de valores seguros
        int nivelEndeudamiento = (datos != null && datos.getNivelEndeudamiento() != null)
                ? datos.getNivelEndeudamiento() : 0;

        double ingresoMensual = (datos != null && datos.getIngresoMensual() != null)
                ? datos.getIngresoMensual() : 0.0;

        // Acumular montos por categoría
        double gastoOcio = 0.0;

        if (datos != null && datos.getTransacciones() != null) {
            for (TransactionDTO transaccion : datos.getTransacciones()) {
                // Esto es una simulacion de la clasificación que hará la IA en el futuro
                String categoria = clasificarCategoriaMock(transaccion.getDescripcion());
                double valorGasto = transaccion.getValor().doubleValue();

                // Sumamos el valor a la categoría correspondiente
                resumenGastos.put(categoria, resumenGastos.getOrDefault(categoria, 0.0) + valorGasto);

                // Separamos el gasto en ocio para la regla financiera
                if (categoria.equals("Entretenimiento y Ocio")) {
                    gastoOcio += valorGasto;
                }
            }
        }
        respuesta.setResumenGastos(resumenGastos);

        // Regla para porcentajes de ocio y entretenimiento
        if (ingresoMensual > 0 && gastoOcio > (ingresoMensual * UMBRAL_MAXIMO_OCIO)) {
            recomendaciones.add("🎭 Tus gastos en 'Entretenimiento y Ocio' ($" + gastoOcio + ") superan el 15% de tus ingresos. Te sugerimos reducirlos para no afectar tu salud financiera.");
        }

        // Asignar perfil financiero y recomendaciones según endeudamiento
        if (nivelEndeudamiento > UMBRAL_ENDEUDAMIENTO_CRITICO) {
            respuesta.setPerfilFinanciero("En riesgo");
            recomendaciones.add("🚨 Tu nivel de endeudamiento es crítico. Es recomendable reducir gastos y buscar asesoría financiera.");
        } else if (nivelEndeudamiento > UMBRAL_ENDEUDAMIENTO_ALTO) {
            respuesta.setPerfilFinanciero("En observación");
            recomendaciones.add("⚠️ Tu nivel de endeudamiento (" + nivelEndeudamiento + "%) es elevado. Prioriza liquidar deudas de mayor interés.");
        } else {
            respuesta.setPerfilFinanciero("Saludable");
            recomendaciones.add("✅ Tu nivel de endeudamiento está dentro de un rango saludable.");
        }

        // Recomendación de ahorro basada en ingresos
        if (ingresoMensual > 0) {
            double ahorroSugerido = ingresoMensual * PORCENTAJE_AHORRO_SUGERIDO;
            recomendaciones.add("💡 Te recomendamos destinar al menos el 20% de tu ingreso mensual ($" + ahorroSugerido + ") a tu fondo de ahorro.");
        }

        respuesta.setRecomendaciones(recomendaciones);
        return respuesta;
    }

    @Override
    public FinancialResponse buscarPorId(Long id) {
        return new FinancialResponse();
    }

     /**
     * Mock temporal: Este metodo simula lo que hará la IA (FastAPI)
     * Reemplazar cuando se haga la integración HTTP.
     */
    private String clasificarCategoriaMock(String descripcion) {
        String descLower = descripcion.toLowerCase();
        if (descLower.contains("cine") || descLower.contains("netflix") || descLower.contains("spotify") || descLower.contains("juego") || descLower.contains("concierto")) {
            return "Entretenimiento y Ocio";
        } else if (descLower.contains("supermercado") || descLower.contains("comida") || descLower.contains("restaurante")) {
            return "Alimentación";
        } else if (descLower.contains("uber") || descLower.contains("gasolina") || descLower.contains("transporte")) {
            return "Transporte";
        } else {
            return "Otros Gastos";
        }
    }
}