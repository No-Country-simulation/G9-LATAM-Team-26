package com.equipo26.financeai.client;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.exception.MlServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class MlServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MlServiceClient(@Value("${ml.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public MlAnalysisResponse analizar(FinancialRequest datos) {
        try {
            log.info("Enviando análisis al microservicio ML ({} transacciones)",
                    datos.getTransacciones().size());

            // DIAGNÓSTICO TEMPORAL: ver el JSON real antes de enviarlo
            try {
                log.info("JSON a enviar: {}", objectMapper.writeValueAsString(datos));
            } catch (Exception ex) {
                log.debug("FALLO AL SERIALIZAR datos a JSON: {}", ex.getMessage());
            }

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
}
