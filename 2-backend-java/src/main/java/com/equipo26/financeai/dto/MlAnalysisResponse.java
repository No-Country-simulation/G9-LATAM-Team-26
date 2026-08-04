package com.equipo26.financeai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO interno: mapea la respuesta cruda de FastAPI.
 * NO se expone al frontend — el frontend sigue recibiendo FinancialResponse.
 */
@Data
public class MlAnalysisResponse {

    @JsonProperty("perfil_financiero")
    private String perfilFinanciero;

    @JsonProperty("probabilidad")
    private Double probabilidad;

    @JsonProperty("transacciones_clasificadas")
    private List<TransaccionClasificadaDTO> transaccionesClasificadas;
}
