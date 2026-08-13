package com.equipo26.financeai.service;

import com.equipo26.financeai.client.MlServiceClient;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de FinancialServiceImpl.
 *
 * IMPORTANTE: estas pruebas NO llaman al microservicio de FastAPI real.
 * El "cerebro" de ML (MlServiceClient) se simula con Mockito (@Mock), así que
 * lo que estamos probando aquí es SOLO la lógica que le pertenece al backend
 * de Java: agrupar gastos por categoría y redactar las recomendaciones a
 * partir de lo que el ML le devuelve. Si estas pruebas fallan, el problema
 * está en FinancialServiceImpl, no en el modelo de IA.
 */
@ExtendWith(MockitoExtension.class)
class FinancialServiceImplTest {

    @Mock
    private MlServiceClient mlServiceClient;

    @InjectMocks
    private FinancialServiceImpl financialService;

    /**
     * Helper: arma una respuesta simulada del ML con un perfil específico.
     * Siempre incluye transaccionesClasificadas (aunque sea vacía) porque
     * FinancialServiceImpl llama a .size() sobre esa lista — si se deja en
     * null, revienta con NullPointerException antes de llegar a la lógica
     * que realmente queremos probar.
     */
    private MlAnalysisResponse crearMlResponseMock(String perfil, Double probabilidad,
                                                     List<TransaccionClasificadaDTO> transacciones) {
        MlAnalysisResponse mockMl = new MlAnalysisResponse();
        mockMl.setPerfilFinanciero(perfil);
        mockMl.setProbabilidad(probabilidad);
        mockMl.setTransaccionesClasificadas(transacciones);
        return mockMl;
    }

    private TransaccionClasificadaDTO crearTransaccion(String categoria, double valor) {
        TransaccionClasificadaDTO t = new TransaccionClasificadaDTO();
        t.setDescripcion("Transacción de prueba");
        t.setValor(BigDecimal.valueOf(valor));
        t.setCategoria(categoria);
        return t;
    }

    // -----------------------------------------------------------------
    // CASO 1: perfil "En riesgo"
    // Qué probamos: que cuando el ML devuelve "En riesgo", el backend
    // traduce eso al mensaje correcto de recomendación (el switch de
    // generarRecomendaciones() debe entrar por el case "En riesgo").
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Perfil 'En riesgo' debe generar el mensaje de alerta correspondiente")
    void testPerfilEndeudamientoCritico() {
        // Given: un usuario con 75% de endeudamiento, y el ML ya decidió que está "En riesgo"
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(75);
        request.setIngresoMensual(4500.0);

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("En riesgo", 0.91, List.of()));

        // When: se ejecuta el análisis
        FinancialResponse response = financialService.analizar(request);

        // Then: el perfil se propaga tal cual, y la primera recomendación
        // corresponde al case "En riesgo" del switch (no al default)
        assertEquals("En riesgo", response.getPerfilFinanciero());
        assertTrue(response.getRecomendaciones().get(0).contains("perfil financiero es de riesgo"),
                "Debe usar el mensaje específico de riesgo, no el mensaje genérico");
    }

    // -----------------------------------------------------------------
    // CASO 2: perfil "En observación"
    // Qué probamos: el segundo branch del switch, para asegurar que no
    // solo funciona el primer/último case (un error común es que el
    // switch solo se pruebe con 1 de 3 ramas).
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Perfil 'En observación' debe generar el mensaje de atención correspondiente")
    void testPerfilEndeudamientoAlto() {
        // Given: un usuario en un punto intermedio (endeudamiento 50%)
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(50);
        request.setIngresoMensual(4500.0);

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("En observación", 0.78, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertEquals("En observación", response.getPerfilFinanciero());
        assertTrue(response.getRecomendaciones().get(0).contains("requiere atención"));
    }

    // -----------------------------------------------------------------
    // CASO 3: perfil "Saludable"
    // Qué probamos: la tercera rama del switch, con el caso "bueno"
    // (usuario sin problemas financieros).
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Perfil 'Saludable' debe generar el mensaje positivo correspondiente")
    void testPerfilEndeudamientoSaludable() {
        // Given: un usuario con bajo endeudamiento (20%)
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(4500.0);

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertEquals("Saludable", response.getPerfilFinanciero());
        assertTrue(response.getRecomendaciones().get(0).contains("perfil financiero es saludable"));
    }

    // -----------------------------------------------------------------
    // CASO 4: perfil desconocido (no es ninguno de los 3 esperados)
    // Qué probamos: el "default" del switch. Esto simula que el modelo
    // de Ciencia de Datos cambie las etiquetas algún día (ej. de
    // "Saludable" a "Sano") y el backend no falle, sino que caiga en
    // un mensaje genérico en vez de un error o un null.
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Un perfil no reconocido debe caer en el mensaje genérico (default), sin fallar")
    void testPerfilNoReconocidoUsaMensajeGenerico() {
        // Given: el ML devuelve una etiqueta que el switch no contempla
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(35);
        request.setIngresoMensual(4500.0);

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Etiqueta_Rara", 0.60, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then: no debe lanzar excepción, y debe usar el mensaje genérico del "default"
        assertNotNull(response);
        assertTrue(response.getRecomendaciones().get(0).contains("Revisa tus finanzas"));
    }

    // -----------------------------------------------------------------
    // CASO 5: ingreso mensual nulo
    // Qué probamos: la validación defensiva dentro de generarRecomendaciones()
    // (if datos.getIngresoMensual() != null...). Si alguien quita ese check
    // por accidente, este test debe empezar a fallar con NullPointerException.
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Ingreso mensual nulo no debe romper el servicio (no agrega recomendación de ahorro)")
    void testManejoDeIngresoNuloNoRompeElServidor() {
        // Given: una petición sin ingreso mensual (campo queda en null)
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        // ingresoMensual se queda sin asignar (null) a propósito

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then: la respuesta debe existir y solo traer la recomendación de perfil,
        // SIN la recomendación de "destina el 20% de tu ingreso" (porque no hay ingreso)
        assertNotNull(response);
        assertEquals(1, response.getRecomendaciones().size(),
                "Sin ingreso mensual no debe agregarse la recomendación de ahorro del 20%");
    }

    // -----------------------------------------------------------------
    // CASO 6: agrupación de gastos por categoría
    // Qué probamos: agruparPorCategoria(). Verificamos que transacciones
    // de la MISMA categoría se sumen correctamente en resumen_gastos.
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Transacciones de la misma categoría deben sumarse en resumen_gastos")
    void testAgrupacionDeGastosPorCategoria() {
        // Given: 2 transacciones de "Alimentación" y 1 de "Transporte"
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(4500.0);

        List<TransaccionClasificadaDTO> transacciones = List.of(
                crearTransaccion("Alimentación", 300.0),
                crearTransaccion("Alimentación", 120.0),
                crearTransaccion("Transporte", 300.0)
        );

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, transacciones));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then: Alimentación debe sumar 420 (300 + 120), Transporte debe quedar en 300
        assertEquals(420.0, response.getResumenGastos().get("Alimentación"));
        assertEquals(300.0, response.getResumenGastos().get("Transporte"));
    }
}