package com.equipo26.financeai.service;

import com.equipo26.financeai.client.MlServiceClient;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import com.equipo26.financeai.dto.FrecuenciaAhorro;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;
import com.equipo26.financeai.dto.TransactionDTO;
import com.equipo26.financeai.entity.AnalisisFinanciero;
import com.equipo26.financeai.repository.AnalisisFinancieroRepository;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

    @Mock
    private AnalisisFinancieroRepository repository;

    @InjectMocks
    private FinancialServiceImpl financialService;

    @BeforeEach
    void configurarRepositorioMock() {
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * Helper: arma una respuesta simulada del ML con un perfil específico.
     * Siempre incluye transaccionesClasificadas (aunque sea vacía) porque
     * FinancialServiceImpl llama a .size() sobre esa lista — si se deja en
     * null, revienta con NullPointerException antes de llegar a la lógica
     * que realmente queremos probar.
     */
    private MlAnalysisResponse crearMlResponseMock(String perfil, Double probabilidad,
                                                   List<TransaccionClasificadaDTO> transacciones) {
        return crearMlResponseMock(perfil, probabilidad, transacciones, List.of());
    }

    private MlAnalysisResponse crearMlResponseMock(String perfil, Double probabilidad,
                                                   List<TransaccionClasificadaDTO> transacciones,
                                                   List<String> factoresClave) {
        MlAnalysisResponse mockMl = new MlAnalysisResponse();
        mockMl.setPerfilFinanciero(perfil);
        mockMl.setProbabilidad(probabilidad);
        mockMl.setTransaccionesClasificadas(transacciones);
        mockMl.setFactoresClave(factoresClave);
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
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Perfil 'En riesgo' debe generar el mensaje de alerta correspondiente")
    void testPerfilEndeudamientoCritico() {
        // Given
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(75);
        request.setIngresoMensual(new BigDecimal("4500.0")); // <-- Corrección aquí
        request.setTransacciones(List.of());

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("En riesgo", 0.91, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertEquals("En riesgo", response.getPerfilFinanciero());
        assertTrue(response.getRecomendaciones().get(0).contains("perfil financiero es de riesgo"),
                "Debe usar el mensaje específico de riesgo, no el mensaje genérico");
    }

    // -----------------------------------------------------------------
    // CASO 2: perfil "En observación"
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Perfil 'En observación' debe generar el mensaje de atención correspondiente")
    void testPerfilEndeudamientoAlto() {
        // Given
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(50);
        request.setIngresoMensual(new BigDecimal("4500.0")); // <-- Corrección aquí
        request.setTransacciones(List.of());

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
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Perfil 'Saludable' debe generar el mensaje positivo correspondiente")
    void testPerfilEndeudamientoSaludable() {
        // Given
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(new BigDecimal("4500.0")); // <-- Corrección aquí
        request.setTransacciones(List.of());

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertEquals("Saludable", response.getPerfilFinanciero());
        assertTrue(response.getRecomendaciones().get(0).contains("perfil financiero es saludable"));
    }

    // -----------------------------------------------------------------
    // CASO 4: perfil desconocido
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Un perfil no reconocido debe caer en el mensaje genérico (default), sin fallar")
    void testPerfilNoReconocidoUsaMensajeGenerico() {
        // Given
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(35);
        request.setIngresoMensual(new BigDecimal("4500.0")); // <-- Corrección aquí
        request.setTransacciones(List.of());

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Etiqueta_Rara", 0.60, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getRecomendaciones().get(0).contains("Revisa tus finanzas"));
    }

    // -----------------------------------------------------------------
    // CASO 5: ingreso mensual nulo
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Ingreso mensual nulo no debe romper el servicio (no agrega recomendación de ahorro)")
    void testManejoDeIngresoNuloNoRompeElServidor() {
        // Given
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setTransacciones(List.of());
        // ingresoMensual se queda sin asignar (null) a propósito

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getRecomendaciones().size(),
                "Sin ingreso mensual no debe agregarse la recomendación de ahorro del 20%");
    }

    // -----------------------------------------------------------------
    // CASO 6: agrupación de gastos por categoría
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Transacciones de la misma categoría deben sumarse en resumen_gastos")
    void testAgrupacionDeGastosPorCategoria() {
        // Given
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(new BigDecimal("4500.0")); // <-- Corrección aquí
        request.setTransacciones(List.of());

        List<TransaccionClasificadaDTO> transacciones = List.of(
                crearTransaccion("Alimentación", 300.0),
                crearTransaccion("Alimentación", 120.0),
                crearTransaccion("Transporte", 300.0)
        );

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, transacciones));

        // When
        FinancialResponse response = financialService.analizar(request);

        // Then
        assertEquals(420.0, response.getResumenGastos().get("Alimentación"));
        assertEquals(300.0, response.getResumenGastos().get("Transporte"));
    }

    // -----------------------------------------------------------------
    // CASO 7: nivel de endeudamiento alto (> 40%)
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Endeudamiento por encima del 40% debe agregar la recomendación de priorizar deudas")
    void testEndeudamientoAltoAgregaRecomendacion() {
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(65);
        request.setIngresoMensual(new BigDecimal("4500.0"));
        request.setTransacciones(List.of());

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        FinancialResponse response = financialService.analizar(request);

        assertTrue(response.getRecomendaciones().stream()
                .anyMatch(r -> r.contains("nivel de endeudamiento") && r.contains("65%")));
    }

    // -----------------------------------------------------------------
    // CASO 8: frecuencia de ahorro nula o baja
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Frecuencia de ahorro NULA debe agregar la recomendación de aumentar el ahorro")
    void testFrecuenciaAhorroNulaAgregaRecomendacion() {
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(new BigDecimal("4500.0"));
        request.setFrecuenciaAhorro(FrecuenciaAhorro.NULA);
        request.setTransacciones(List.of());

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        FinancialResponse response = financialService.analizar(request);

        assertTrue(response.getRecomendaciones().stream()
                .anyMatch(r -> r.contains("frecuencia de ahorro")));
    }

    // -----------------------------------------------------------------
    // CASO 9: categoría discrecional distinta a Entretenimiento supera el umbral
    // -----------------------------------------------------------------
    @Test
    @DisplayName("Gasto en 'Suscripciones' por encima del 15% del ingreso debe generar recomendación")
    void testCategoriaDiscrecionalSuscripcionesAgregaRecomendacion() {
        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(new BigDecimal("1000.0"));
        request.setTransacciones(List.of());

        List<TransaccionClasificadaDTO> transacciones = List.of(
                crearTransaccion("Suscripciones", 200.0));

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, transacciones));

        FinancialResponse response = financialService.analizar(request);

        assertTrue(response.getRecomendaciones().stream()
                .anyMatch(r -> r.contains("Suscripciones")));
    }

    // -----------------------------------------------------------------
    // CASO 10: editar() aplica la misma regla de negocio "gastos > ingreso ⇒ Crítico"
    // -----------------------------------------------------------------
    @Test
    @DisplayName("editar() debe forzar el perfil a Crítico cuando los gastos superan el ingreso, igual que analizar()")
    void testEditarAplicaMismoOverrideCriticoQueAnalizar() {
        AnalisisFinancieroRepository repository = mock(AnalisisFinancieroRepository.class);
        FinancialServiceImpl service = new FinancialServiceImpl(mlServiceClient, repository);

        AnalisisFinanciero existente = new AnalisisFinanciero();
        existente.setId(1L);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(existente));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialRequest request = new FinancialRequest();
        request.setNivelEndeudamiento(20);
        request.setIngresoMensual(new BigDecimal("100.0"));

        TransactionDTO transaccion = new TransactionDTO();
        transaccion.setDescripcion("Gasto grande");
        transaccion.setValor(new BigDecimal("500.0"));
        request.setTransacciones(List.of(transaccion));

        when(mlServiceClient.analizar(any()))
                .thenReturn(crearMlResponseMock("Saludable", 0.95, List.of()));

        FinancialResponse response = service.editar(1L, request);

        assertEquals("Crítico", response.getPerfilFinanciero());
    }
}