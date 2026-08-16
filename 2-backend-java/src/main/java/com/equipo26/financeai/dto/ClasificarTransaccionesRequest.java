package com.equipo26.financeai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO de entrada para POST /clasificar-transaccion: clasifica una o varias
 * transacciones por su descripción, sin calcular el perfil financiero.
 */
@Data
@Schema(description = "Transacciones a clasificar, sin necesidad de calcular el perfil financiero")
public class ClasificarTransaccionesRequest {

    @Schema(description = "Lista de transacciones a clasificar")
    @JsonProperty("transacciones")
    @NotNull(message = "La lista de transacciones es obligatoria")
    @Size(min = 1, message = "Debes ingresar al menos una transacción")
    @Valid
    private List<TransactionDTO> transacciones;
}
