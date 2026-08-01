package com.equipo26.financeai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO que representa una transacción individual enviada por el frontend.
 * Lombok (@Data) genera automáticamente los getters y setters al compilar.
 */
@Data
@Schema(description = "Transacción individual reportada por el usuario")
public class TransactionDTO {

    // La descripción no debe ser nula ni estar vacía
    @Schema(description = "Descripción de la transacción", example = "Alimentación")
    @NotBlank(message = "La descripción de la transacción es obligatoria")
    private String descripcion;

    // El valor no debe ser nulo y debe ser estrictamente mayor a cero
    @Schema(description = "Valor de la transacción en MXN", example = "900")
    @NotNull(message = "El valor de la transacción es obligatorio")
    @Positive(message = "El valor de la transacción debe ser un número mayor a cero")
    private BigDecimal valor;
}