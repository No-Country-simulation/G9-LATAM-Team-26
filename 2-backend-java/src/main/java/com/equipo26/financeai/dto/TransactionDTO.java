package com.equipo26.financeai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO que representa una transacción individual enviada por el frontend.
 * Lombok (@Data) genera automáticamente los getters y setters al compilar.
 */
@Data
public class TransactionDTO {

    // La descripción no debe ser nula ni estar vacía
    @NotBlank(message = "La descripción de la transacción es obligatoria")
    private String descripcion;

    // El valor no debe ser nulo y debe ser estrictamente mayor a cero
    @NotNull(message = "El valor de la transacción es obligatorio")
    @Positive(message = "El valor de la transacción debe ser un número mayor a cero")
    private Double valor;
}