package com.equipo26.financeai.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Transacción ya clasificada, tal como la devuelve el microservicio FastAPI.
 */

@Data
public class TransaccionClasificadaDTO {
    private String descripcion;
    private BigDecimal valor;
    private String categoria;
}
