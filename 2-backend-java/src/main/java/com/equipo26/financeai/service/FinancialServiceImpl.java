package com.equipo26.financeai.service;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialServiceImpl implements FinancialService {

    private static final int UMBRAL_ENDEUDAMIENTO_ALTO = 40;
    private static final double PORCENTAJE_AHORRO_SUGERIDO = 0.20;

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        FinancialResponse respuesta = new FinancialResponse();

        int nivelEndeudamiento = (datos != null && datos.getNivel_endeudamiento() != null)
                ? datos.getNivel_endeudamiento()
                : 0;

        List<String> recomendaciones = new ArrayList<>();

        // Asignar perfil financiero y recomendaciones según endeudamiento

        if (nivelEndeudamiento > UMBRAL_ENDEUDAMIENTO_ALTO) {
            respuesta.setPerfil_financiero("En observacion");
            recomendaciones.add("⚠️ Tu nivel de endeudamiento (" + nivelEndeudamiento + "%) es elevado. Prioriza liquidar deudas de mayor interés.");
        } else {
            respuesta.setPerfil_financiero("Saludable");
            recomendaciones.add("✅ Tu nivel de endeudamiento está dentro de un rango saludable.");
        }

        // Recomendación de ahorro basada en ingresos
        if (datos != null && datos.getIngreso_mensual() != null && datos.getIngreso_mensual() > 0) {
            double ahorroSugerido = datos.getIngreso_mensual() * PORCENTAJE_AHORRO_SUGERIDO;
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