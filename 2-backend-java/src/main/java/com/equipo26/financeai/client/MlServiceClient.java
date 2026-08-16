package com.equipo26.financeai.client;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.dto.ClasificarTransaccionesRequest;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;
import com.equipo26.financeai.exception.MlServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
public class MlServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    // Timeouts generosos: en hosting gratuito (Render, etc.) el microservicio
    // ML puede estar "dormido" y tardar 30-60s en despertar en el primer request.
    public MlServiceClient(@Value("${ml.service.url}") String baseUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(65))
                .readTimeout(Duration.ofSeconds(65))
                .build();
        this.baseUrl = baseUrl;
    }

    public MlAnalysisResponse analizar(FinancialRequest datos) {
        try {
            log.info("Enviando análisis al microservicio ML ({} transacciones)",
                    datos.getTransacciones().size());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FinancialRequest> request = new HttpEntity<>(datos, headers);

            return restTemplate.postForObject(
                    baseUrl + "/analisis-financiero",
                    request,
                    MlAnalysisResponse.class);

        } catch (RestClientException e) {
            log.error("Fallo al comunicarse con el microservicio ML: {}", e.getMessage());
            throw new MlServiceException(
                    "El servicio de análisis no está disponible en este momento", e);
        }
    }

    // Passthrough al endpoint dedicado /clasificar-transaccion (sin calcular el perfil)
    public TransaccionClasificadaDTO[] clasificarTransacciones(ClasificarTransaccionesRequest datos) {
        try {
            log.info("Enviando {} transacciones a clasificar", datos.getTransacciones().size());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ClasificarTransaccionesRequest> request = new HttpEntity<>(datos, headers);

            return restTemplate.postForObject(
                    baseUrl + "/clasificar-transaccion",
                    request,
                    TransaccionClasificadaDTO[].class);

        } catch (RestClientException e) {
            log.error("Fallo al comunicarse con el microservicio ML: {}", e.getMessage());
            throw new MlServiceException(
                    "El servicio de clasificación no está disponible en este momento", e);
        }
    }
}