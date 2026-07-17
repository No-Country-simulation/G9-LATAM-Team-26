package com.equipo26.financeai.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO que mapea el JSON de entrada completo que recibiremos en la API.
 * Contiene los datos generales del usuario y su lista de transacciones.
 */

@Data
public class FinancialRequest {
    private Double ingreso_mensual;
    private Integer nivel_endeudamiento;
    private String frecuencia_ahorro;
    private List<TransactionDTO> transacciones;
}