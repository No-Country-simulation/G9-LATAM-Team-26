package com.equipo26.financeai.service;

import com.equipo26.financeai.client.MlServiceClient;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialServiceImpl implements FinancialService {

    private static final double PORCENTAJE_AHORRO_SUGERIDO = 0.20;

    private final MlServiceClient mlServiceClient;

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        // El diagnóstico ya NO se calcula acá: lo produce el microservicio ML
        MlAnalysisResponse ml = mlServiceClient.analizar(datos);

        log.info("ML devolvió: perfil={}, {} transacciones clasificadas",
                ml.getPerfilFinanciero(), ml.getTransaccionesClasificadas().size());

        FinancialResponse respuesta = new FinancialResponse();
        respuesta.setPerfilFinanciero(ml.getPerfilFinanciero());
        respuesta.setProbabilidad(ml.getProbabilidad());
        respuesta.setResumenGastos(agruparPorCategoria(ml.getTransaccionesClasificadas()));
        respuesta.setRecomendaciones(generarRecomendaciones(ml.getPerfilFinanciero(), datos));

        return respuesta;
    }

    /**
     * Suma los valores de las transacciones agrupándolas por categoría.
     * Este es el valor que agrega el backend Java sobre la salida cruda del ML.
     */
    private Map<String, Double> agruparPorCategoria(List<TransaccionClasificadaDTO> transacciones) {
        if (transacciones == null || transacciones.isEmpty()) {
            return Collections.emptyMap();
        }
        return transacciones.stream()
                .collect(Collectors.groupingBy(
                        TransaccionClasificadaDTO::getCategoria,
                        TreeMap::new,
                        Collectors.summingDouble(t -> t.getValor().doubleValue())));
    }

    /**
     * Traduce el perfil del modelo a consejos accionables.
     * Los strings del switch deben coincidir con los que devuelve FastAPI.
     */
    private List<String> generarRecomendaciones(String perfil, FinancialRequest datos) {
        List<String> recomendaciones = new ArrayList<>();

        switch (perfil) {
            case "En riesgo" -> recomendaciones.add(
                    "Tu perfil financiero es de riesgo. Es recomendable reducir gastos y buscar asesoría financiera.");
            case "En observación" -> recomendaciones.add(
                    "Tu situación requiere atención. Prioriza liquidar deudas de mayor interés y controlar gastos.");
            case "Saludable" -> recomendaciones.add(
                    "Tu perfil financiero es saludable. Mantén tus hábitos actuales.");
            default -> {
                log.warn("Perfil financiero no reconocido recibido del ML: {}", perfil);
                recomendaciones.add("Revisa tus finanzas con detalle para mantener un balance saludable.");
            }
        }

        if (datos.getIngresoMensual() != null && datos.getIngresoMensual() > 0) {
            double ahorroSugerido = datos.getIngresoMensual() * PORCENTAJE_AHORRO_SUGERIDO;
            recomendaciones.add(String.format(
                    "Te recomendamos destinar al menos el 20%% de tu ingreso mensual ($%.2f) a tu fondo de ahorro.",
                    ahorroSugerido));
        }

        return recomendaciones;
    }

    @Override
    public FinancialResponse buscarPorId(Long id) {
        return new FinancialResponse();
    }
}