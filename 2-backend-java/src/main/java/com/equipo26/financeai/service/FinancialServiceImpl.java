package com.equipo26.financeai.service;

import com.equipo26.financeai.client.MlServiceClient;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;
import com.equipo26.financeai.entity.AnalisisFinanciero;
import com.equipo26.financeai.repository.AnalisisFinancieroRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final AnalisisFinancieroRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FinancialResponse analizar(FinancialRequest datos) {
        // El diagnóstico lo produce el microservicio ML
        MlAnalysisResponse ml = mlServiceClient.analizar(datos);

        log.info("ML devolvió: perfil={}, {} transacciones clasificadas",
                ml.getPerfilFinanciero(), ml.getTransaccionesClasificadas().size());

        // Aca la data extra (agrupaciones y recomendaciones)
        Map<String, Double> resumenGastos = agruparPorCategoria(ml.getTransaccionesClasificadas());
        List<String> recomendaciones = generarRecomendaciones(ml.getPerfilFinanciero(), datos);

        // Crear la entidad y guardar en la base de datos
        AnalisisFinanciero entidad = new AnalisisFinanciero();
        entidad.setPerfilFinanciero(ml.getPerfilFinanciero());
        entidad.setProbabilidad(ml.getProbabilidad());

        try {
            // Transformar el Map y la List a Strings en formato JSON puro para guardarlos en H2
            entidad.setResumenGastos(objectMapper.writeValueAsString(resumenGastos));
            entidad.setRecomendaciones(objectMapper.writeValueAsString(recomendaciones));
        } catch (JsonProcessingException e) {
            log.error("Error convirtiendo estructuras a JSON para la BD", e);
            throw new RuntimeException("Error interno al procesar el análisis");
        }

        AnalisisFinanciero guardado = repository.save(entidad); // Se guarda en H2

        // Armar la respuesta final incluyendo el nuevo ID autogenerado
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
        //Buscar el registro real en H2. Si no existe, lanzamos un error.
        AnalisisFinanciero encontrado = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el análisis con ID: " + id));

        // Crear la caja de respuesta
        FinancialResponse respuesta = new FinancialResponse();

        // Llenar la caja con los datos de la base de datos
        respuesta.setId(encontrado.getId());
        respuesta.setPerfilFinanciero(encontrado.getPerfilFinanciero());
        respuesta.setProbabilidad(encontrado.getProbabilidad());

        try {
            // Hacer el proceso inverso: Leemos el texto de la base de datos y lo reconstruimos como Map y List
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

        // Devolver la caja llena
        return respuesta;
    }
}