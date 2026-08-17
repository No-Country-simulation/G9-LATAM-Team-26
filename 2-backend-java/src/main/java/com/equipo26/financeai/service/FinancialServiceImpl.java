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
    private static final BigDecimal UMBRAL_MAXIMO_OCIO = new BigDecimal("0.15"); // Máximo 15% del ingreso en categorías discrecionales
    private static final int UMBRAL_ENDEUDAMIENTO_ALTO = 40;

    // Categorías consideradas "discrecionales": es razonable sugerir reducirlas.
    // No incluye Vivienda, Servicios, Salud, etc. porque esas naturalmente pueden
    // superar el 15% del ingreso sin que eso sea un problema financiero.
    private static final List<String> CATEGORIAS_DISCRECIONALES =
            List.of("Entretenimiento", "Suscripciones", "Ropa", "Mascotas");

    private final MlServiceClient mlServiceClient;
    private final AnalisisFinancieroRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        // El diagnóstico lo produce el microservicio ML
        MlAnalysisResponse ml = mlServiceClient.analizar(datos);

        // Guardamos el perfil en una variable para poder modificarlo si se rompe la regla
        String perfilFinal = determinarPerfilFinal(ml.getPerfilFinanciero(), datos);

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
            entidad.setFactoresClave(objectMapper.writeValueAsString(ml.getFactoresClave()));
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
        respuesta.setFactoresClave(ml.getFactoresClave());

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
     * Aplica la regla de negocio que fuerza el perfil a "Crítico" cuando la suma
     * de las transacciones del período supera el ingreso mensual, sin importar
     * lo que haya devuelto el modelo ML.
     */
    private String determinarPerfilFinal(String perfilMl, FinancialRequest datos) {
        if (datos.getIngresoMensual() == null) {
            return perfilMl;
        }

        BigDecimal totalGastos = datos.getTransacciones().stream()
                .map(t -> t.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalGastos.compareTo(datos.getIngresoMensual()) > 0) {
            log.warn("Regla de negocio activada: Los gastos ({}) superan los ingresos ({}). Perfil forzado a Crítico.", totalGastos, datos.getIngresoMensual());
            return "Crítico";
        }

        return perfilMl;
    }

    /**
     * Traduce el perfil del modelo y los datos financieros a consejos accionables,
     * combinando reglas de negocio.
     */
    private List<String> generarRecomendaciones(String perfil, FinancialRequest datos,
                                                  Map<String, Double> resumenGastos) {
        List<String> recomendaciones = new ArrayList<>();

        recomendaciones.add(recomendacionPerfil(perfil));

        BigDecimal ingresoMensual = datos.getIngresoMensual();
        if (ingresoMensual != null && ingresoMensual.compareTo(BigDecimal.ZERO) > 0) {
            recomendaciones.add(recomendacionAhorro(ingresoMensual));
            recomendaciones.addAll(recomendacionesCategoriasDiscrecionales(ingresoMensual, resumenGastos));
        }

        recomendacionEndeudamiento(datos.getNivelEndeudamiento()).ifPresent(recomendaciones::add);
        recomendacionFrecuenciaAhorro(datos.getFrecuenciaAhorro()).ifPresent(recomendaciones::add);

        return recomendaciones;
    }

    private String recomendacionPerfil(String perfil) {
        return switch (perfil) {
            case "Crítico" -> "🚨 ¡Tu situación financiera es crítica!!! Tus gastos son más elevados que tu ingreso.";
            case "En riesgo" -> "🚨 Tu perfil financiero es de riesgo. Es recomendable reducir gastos y buscar asesoría financiera.";
            case "En observación" -> "⚠️ Tu situación requiere atención. Prioriza liquidar deudas de mayor interés y controlar gastos.";
            case "Saludable" -> "✅ Tu perfil financiero es saludable. Mantén tus hábitos actuales.";
            default -> {
                log.warn("Perfil financiero no reconocido recibido del ML: {}", perfil);
                yield "Revisa tus finanzas con detalle para mantener un balance saludable.";
            }
        };
    }

    private String recomendacionAhorro(BigDecimal ingresoMensual) {
        BigDecimal ahorroSugerido = ingresoMensual.multiply(PORCENTAJE_AHORRO_SUGERIDO);
        return String.format(
                "💡 Te recomendamos destinar al menos el 20%% de tu ingreso mensual ($%.2f) a tu fondo de ahorro.",
                ahorroSugerido.doubleValue());
    }

    private List<String> recomendacionesCategoriasDiscrecionales(BigDecimal ingresoMensual, Map<String, Double> resumenGastos) {
        BigDecimal maximoDiscrecional = ingresoMensual.multiply(UMBRAL_MAXIMO_OCIO);
        List<String> mensajes = new ArrayList<>();

        for (String categoria : CATEGORIAS_DISCRECIONALES) {
            Double gasto = resumenGastos.getOrDefault(categoria, 0.0);
            if (BigDecimal.valueOf(gasto).compareTo(maximoDiscrecional) > 0) {
                mensajes.add(String.format(
                        "🎭 Tus gastos en '%s' ($%.2f) superan el 15%% de tus ingresos. Te sugerimos reducirlos para no afectar tu salud financiera.",
                        categoria, gasto));
            }
        }

        return mensajes;
    }

    private Optional<String> recomendacionEndeudamiento(Integer nivelEndeudamiento) {
        if (nivelEndeudamiento != null && nivelEndeudamiento > UMBRAL_ENDEUDAMIENTO_ALTO) {
            return Optional.of(String.format(
                    "📉 Tu nivel de endeudamiento (%d%%) es alto. Prioriza liquidar las deudas con mayor tasa de interés antes de asumir nuevos compromisos.",
                    nivelEndeudamiento));
        }
        return Optional.empty();
    }

    private Optional<String> recomendacionFrecuenciaAhorro(FrecuenciaAhorro frecuenciaAhorro) {
        if (frecuenciaAhorro == FrecuenciaAhorro.NULA || frecuenciaAhorro == FrecuenciaAhorro.BAJA) {
            return Optional.of(
                    "🐷 Aumentar tu frecuencia de ahorro, aunque sea con montos pequeños, mejoraría tu perfil financiero.");
        }
        return Optional.empty();
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
            if (encontrado.getFactoresClave() != null) {
                List<String> factores = objectMapper.readValue(
                        encontrado.getFactoresClave(), new TypeReference<List<String>>() {});
                respuesta.setFactoresClave(factores);
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

        // Misma regla de negocio que analizar(): si los gastos superan el ingreso, el perfil es Crítico
        String perfilFinal = determinarPerfilFinal(ml.getPerfilFinanciero(), requestParaMl);

        // Recalcular resumen y recomendaciones con la misma lógica de analizar
        Map<String, Double> resumenGastos = agruparPorCategoria(ml.getTransaccionesClasificadas());
        List<String> recomendaciones = generarRecomendaciones(perfilFinal, requestParaMl, resumenGastos);

        //Actualizar los campos del registro Existente
        existente.setPerfilFinanciero(perfilFinal);
        existente.setProbabilidad(ml.getProbabilidad());
        existente.setIngresoMensual(datos.getIngresoMensual());
        existente.setNivelEndeudamiento(datos.getNivelEndeudamiento());
        existente.setFrecuenciaAhorro(datos.getFrecuenciaAhorro());

        try {
            existente.setResumenGastos(objectMapper.writeValueAsString(resumenGastos));
            existente.setRecomendaciones(objectMapper.writeValueAsString(recomendaciones));
            existente.setFactoresClave(objectMapper.writeValueAsString(ml.getFactoresClave()));
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
        respuesta.setFactoresClave(ml.getFactoresClave());

        return respuesta;
    }

    @Override
    public TransaccionClasificadaDTO[] clasificarTransacciones(ClasificarTransaccionesRequest datos) {
        // Passthrough simple: no calcula perfil ni persiste, solo clasifica.
        return mlServiceClient.clasificarTransacciones(datos);
    }
}