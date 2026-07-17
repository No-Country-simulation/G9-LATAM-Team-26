package com.equipo26.financeai.dto;

import lombok.Data;

/**
 * DTO que representa una transacción individual enviada por el frontend.
 * Lombok (@Data) genera automáticamente los getters y setters al compilar.
 */

@Data
public class TransactionDTO {
    private String descripcion;
    private Double valor;
}