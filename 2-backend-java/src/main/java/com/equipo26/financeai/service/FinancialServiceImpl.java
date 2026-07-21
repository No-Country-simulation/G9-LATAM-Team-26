package com.equipo26.financeai.service;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialServiceImpl implements FinancialService {

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        FinancialResponse respuesta = new FinancialResponse();

        int nivelEndeudamiento = (datos != null && datos.getNivel_endeudamiento() != null)
                ? datos.getNivel_endeudamiento()
                : 0;

        List<String> recomendaciones = new ArrayList<>();

        // 1. Asignar perfil financiero y recomendaciones según endeudamiento
        if (nivelEndeudamiento > 40) {
            respuesta.setPerfil_financiero("En observacion");
            recomendaciones.add("⚠️ Tu nivel de endeudamiento (" + nivelEndeudamiento + "%) es elevado. Prioriza liquidar deudas de mayor interés.");
        } else {
            respuesta.setPerfil_financiero("Saludable");
            recomendaciones.add("✅ Tu nivel de endeudamiento está dentro de un rango saludable.");
        }

        // 2. Recomendación de ahorro basada en ingresos
        if (datos != null && datos.getIngreso_mensual() != null && datos.getIngreso_mensual() > 0) {
            double ahorroSugerido = datos.getIngreso_mensual() * 0.20;
            recomendaciones.add("💡 Te recomendamos destinar al menos el 20% de tu ingreso mensual ($" + ahorroSugerido + ") a tu fondo de ahorro.");
        }

        // 3. Guardar las recomendaciones en la respuesta
        respuesta.setRecomendaciones(recomendaciones);

        return respuesta;
    }

    @Override
    public FinancialResponse buscarPorId(Long id) {
        return new FinancialResponse();
    }
}