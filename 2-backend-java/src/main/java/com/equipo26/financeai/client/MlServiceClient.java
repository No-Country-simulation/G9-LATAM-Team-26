package com.equipo26.financeai.client;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.MlAnalysisResponse;
import com.equipo26.financeai.exception.MlServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class MlServiceClient {

    private final RestClient restClient;

    public MlServiceClient(@Value("${ml.service.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl); // lo creamos nosotros, sin inyectar el Builder
    }

    public MlAnalysisResponse analizar(FinancialRequest datos) {
        try {
            log.info("Enviando análisis al microservicio ML ({} transacciones)",
                    datos.getTransacciones().size());

            return restClient.post()
                    .uri("/analisis-financiero")
                    .body(datos)
                    .retrieve()
                    .body(MlAnalysisResponse.class);

        } catch (RestClientException e) {
            log.error("Fallo al comunicarse con el microservicio ML: {}", e.getMessage());
            throw new MlServiceException(
                    "El servicio de análisis no está disponible en este momento", e);
        }
    }
}
