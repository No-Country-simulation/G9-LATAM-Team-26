package com.equipo26.financeai.service;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialServiceImpl implements FinancialService {

    // Constante para el nivel de endeudamiento en cuestion
    private static final int UMBRAL_ENDEUDAMIENTO_CRITICO = 70;
    private static final int UMBRAL_ENDEUDAMIENTO_ALTO = 40;
    private static final double PORCENTAJE_AHORRO_SUGERIDO = 0.20;

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        FinancialResponse respuesta = new FinancialResponse();

        int nivelEndeudamiento = (datos != null && datos.getNivelEndeudamiento() != null)
                ? datos.getNivelEndeudamiento()
                : 0;

        List<String> recomendaciones = new ArrayList<>();

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
        if (datos != null && datos.getIngresoMensual() != null && datos.getIngresoMensual() > 0) {
            double ahorroSugerido = datos.getIngresoMensual() * PORCENTAJE_AHORRO_SUGERIDO;
            recomendaciones.add("💡 Te recomendamos destinar al menos el 20% de tu ingreso mensual ($" + ahorroSugerido + ") a tu fondo de ahorro.");
        }

        // Guardar las recomendaciones en la respuesta
        respuesta.setRecomendaciones(recomendaciones);

        return respuesta;
    }

    @Override
    public FinancialResponse buscarPorId(Long id) {
        return new FinancialResponse();
    }
}