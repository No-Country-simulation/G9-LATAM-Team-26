package com.equipo26.financeai.service;

import com.equipo26.financeai.client.MlServiceClient;
import com.equipo26.financeai.dto.*;
import com.equipo26.financeai.entity.AnalisisFinanciero;
import com.equipo26.financeai.exception.FinancialNotFoundException;
import com.equipo26.financeai.repository.AnalisisFinancieroRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialServiceImpl implements FinancialService {

    private static final BigDecimal PORCENTAJE_AHORRO_SUGERIDO = new BigDecimal("0.20");
    private static final BigDecimal UMBRAL_MAXIMO_OCIO = new BigDecimal("0.15"); // Máximo 15% del ingreso en ocio

    private final MlServiceClient mlServiceClient;
    private final AnalisisFinancieroRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        // El diagnóstico lo produce el microservicio ML
        MlAnalysisResponse ml = mlServiceClient.analizar(datos);

        // Guardamos el perfil en una variable para poder modificarlo si se rompe la regla
        String perfilFinal = ml.getPerfilFinanciero();

        // CHICOS AQUI LA NUEVA REGLA DE NEGOCIO: VALIDACIÓN DE GASTOS VS INGRESOS
        // 1. Sumamos todas las transacciones que vienen en el request
        BigDecimal totalGastos = datos.getTransacciones().stream()
                .map(t -> t.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Si los gastos son mayores al ingreso, sobrescribimos lo que dijo la IA
        if (totalGastos.compareTo(datos.getIngresoMensual()) > 0) {
            perfilFinal = "Crítico";
            log.warn("Regla de negocio activada: Los gastos ({}) superan los ingresos ({}). Perfil forzado a Crítico.", totalGastos, datos.getIngresoMensual());
        }

        log.info("Perfil final a guardar={}, {} transacciones clasificadas",
                perfilFinal, ml.getTransaccionesClasificadas().size());

        // Aca la data extra (agrupaciones y recomendaciones)
        Map<String, Double> resumenGastos = agruparPorCategoria(ml.getTransaccionesClasificadas());

        // Le pasamos el perfilFinal (que ya está evaluado) para que genere las recomendaciones correctas
        List<String> recomendaciones = generarRecomendaciones(perfilFinal, datos, resumenGastos);

        AnalisisFinanciero entidad = new AnalisisFinanciero();

        // Se actualizan los datos (usamos perfilFinal en lugar del ml directo)
        entidad.setPerfilFinanciero(perfilFinal);
        entidad.setProbabilidad(ml.getProbabilidad());

        try {
            // Transformar el Map y la List a Strings en formato JSON puro para guardarlos en H2
            entidad.setResumenGastos(objectMapper.writeValueAsString(resumenGastos));
            entidad.setRecomendaciones(objectMapper.writeValueAsString(recomendaciones));
        } catch (JsonProcessingException e) {
            log.error("Error convirtiendo estructuras a JSON para la BD", e);
            throw new RuntimeException("Error interno al procesar el análisis");
        }

        // Si la entidad ya tenía su ID cargado, JPA hará automáticamente un UPDATE. Si no, hará un INSERT.
        AnalisisFinanciero guardado = repository.save(entidad);

        // Armar la respuesta final incluyendo el ID
        FinancialResponse respuesta = new FinancialResponse();
        respuesta.setId(guardado.getId());
        respuesta.setPerfilFinanciero(guardado.getPerfilFinanciero());
        respuesta.setProbabilidad(guardado.getProbabilidad());
        respuesta.setResumenGastos(resumenGastos);
        respuesta.setRecomendaciones(recomendaciones);

        return respuesta;
    }

    /**
     * Suma los valores de las transacciones agrupándolas por categoría.
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
     * Traduce el perfil del modelo a consejos accionables, combinando reglas de negocio.
     */
    private List<String> generarRecomendaciones(String perfil, FinancialRequest datos, Map<String, Double> resumenGastos) {
        List<String> recomendaciones = new ArrayList<>();

        switch (perfil) {
            case "Crítico" -> recomendaciones.add(
                    "🚨 ¡Tu situación financiera es crítica!!! Tus gastos son más elevados que tu ingreso.");
            case "En riesgo" -> recomendaciones.add(
                    "🚨 Tu perfil financiero es de riesgo. Es recomendable reducir gastos y buscar asesoría financiera.");
            case "En observación" -> recomendaciones.add(
                    "⚠️ Tu situación requiere atención. Prioriza liquidar deudas de mayor interés y controlar gastos.");
            case "Saludable" -> recomendaciones.add(
                    "✅ Tu perfil financiero es saludable. Mantén tus hábitos actuales.");
            default -> {
                log.warn("Perfil financiero no reconocido recibido del ML: {}", perfil);
                recomendaciones.add("Revisa tus finanzas con detalle para mantener un balance saludable.");
            }
        }

        BigDecimal ingresoMensual = datos.getIngresoMensual();

        if (ingresoMensual != null && ingresoMensual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ahorroSugerido = ingresoMensual.multiply(PORCENTAJE_AHORRO_SUGERIDO);
            recomendaciones.add(String.format(
                    "💡 Te recomendamos destinar al menos el 20%% de tu ingreso mensual ($%.2f) a tu fondo de ahorro.",
                    ahorroSugerido.doubleValue()));

            Double gastoOcio = resumenGastos.getOrDefault("Entretenimiento y Ocio", 0.0);
            BigDecimal maximoOcio = ingresoMensual.multiply(UMBRAL_MAXIMO_OCIO);

            if (BigDecimal.valueOf(gastoOcio).compareTo(maximoOcio) > 0) {
                recomendaciones.add(String.format(
                        "🎭 Tus gastos en 'Entretenimiento y Ocio' ($%.2f) superan el 15%% de tus ingresos. Te sugerimos reducirlos para no afectar tu salud financiera.",
                        gastoOcio));
            }
        }

        return recomendaciones;
    }

    @Override
    public FinancialResponse buscarPorId(Long id) {
        AnalisisFinanciero encontrado = repository.findById(id)
                .orElseThrow(() -> new FinancialNotFoundException(id));

        FinancialResponse respuesta = new FinancialResponse();
        respuesta.setId(encontrado.getId());
        respuesta.setPerfilFinanciero(encontrado.getPerfilFinanciero());
        respuesta.setProbabilidad(encontrado.getProbabilidad());

        try {
            if (encontrado.getResumenGastos() != null) {
                Map<String, Double> gastos = objectMapper.readValue(
                        encontrado.getResumenGastos(), new TypeReference<Map<String, Double>>() {});
                respuesta.setResumenGastos(gastos);
            }
            if (encontrado.getRecomendaciones() != null) {
                List<String> recs = objectMapper.readValue(
                        encontrado.getRecomendaciones(), new TypeReference<List<String>>() {});
                respuesta.setRecomendaciones(recs);
            }
        } catch (JsonProcessingException e) {
            log.error("Error reconstruyendo el JSON desde la BD", e);
        }

        return respuesta;
    }

    @Override
    public FinancialResponse editar(Long id, FinancialRequest datos) {
        //Buscar el registro existente, en caso contrario, lanzar excepción
        AnalisisFinanciero existente = repository.findById(id)
                .orElseThrow(() -> new FinancialNotFoundException(id));

        // Volver a mandar los datos actualizados al microservicio ML
        FinancialRequest requestParaMl = new FinancialRequest();
        requestParaMl.setIngresoMensual(datos.getIngresoMensual());
        requestParaMl.setFrecuenciaAhorro(datos.getFrecuenciaAhorro());
        requestParaMl.setNivelEndeudamiento(datos.getNivelEndeudamiento());
        requestParaMl.setTransacciones(datos.getTransacciones());

        MlAnalysisResponse ml = mlServiceClient.analizar(requestParaMl);
        log.info("ML devolvio (update): perfil={}, {} transacciones clasificadas",
                ml.getPerfilFinanciero(), ml.getTransaccionesClasificadas().size());

        // Recalcular resumen y recomendaciones con la misma lógica de analizar
        Map<String, Double> resumenGastos = agruparPorCategoria(ml.getTransaccionesClasificadas());
        List<String> recomendaciones = generarRecomendaciones(ml.getPerfilFinanciero(), requestParaMl, resumenGastos);

        //Actualizar los campos del registro Existente
        existente.setPerfilFinanciero(ml.getPerfilFinanciero());
        existente.setProbabilidad(ml.getProbabilidad());
        existente.setIngresoMensual(datos.getIngresoMensual());
        existente.setNivelEndeudamiento(datos.getNivelEndeudamiento());
        existente.setFrecuenciaAhorro(datos.getFrecuenciaAhorro());

        try {
            existente.setResumenGastos(objectMapper.writeValueAsString(resumenGastos));
            existente.setRecomendaciones(objectMapper.writeValueAsString(recomendaciones));
        }catch (JsonProcessingException e){
            log.error("Error convirtiendo estructura a JSON para la BD", e);
            throw new RuntimeException("Error interno al procesar la actualización");
        }
        // save() con un id existente
        AnalisisFinanciero actualizado = repository.save(existente);

        //Armar la respuesta
        FinancialResponse respuesta = new FinancialResponse();
        respuesta.setId(actualizado.getId());
        respuesta.setPerfilFinanciero(actualizado.getPerfilFinanciero());
        respuesta.setProbabilidad(actualizado.getProbabilidad());
        respuesta.setResumenGastos(resumenGastos);
        respuesta.setRecomendaciones(recomendaciones);

        return respuesta;
    }
}