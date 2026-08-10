package com.equipo26.financeai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

/**
 * DTO que mapea el JSON de entrada completo que recibiremos en la API.
 * Contiene los datos generales del usuario y su lista de transacciones.
 */
@Data
@Schema(description = "Datos financieros del usuario para generar el análisis")
public class FinancialRequest {

    // El ingreso mensual no puede ser nulo ni menor a cero
    @Schema(description = "Ingreso mensual del usuario en pesos MXN", example ="15000.00")
    @JsonProperty("ingreso_mensual")
    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.0", message = "El ingreso mensual no puede ser menor a 0")
    private BigDecimal ingresoMensual;

    // El endeudamiento debe estar entre 0% y 100%
    @Schema(description = "Porcentaje de endeudamiento (0-100)", example = "25")
    @JsonProperty("nivel_endeudamiento")
    @NotNull(message = "El nivel de endeudamiento es obligatorio")
    @Min(value = 0, message = "El nivel de endeudamiento no puede ser menor a 0%")
    @Max(value = 100, message = "El nivel de endeudamiento no puede superar el 100%")
    private Integer nivelEndeudamiento;

    // La frecuencia no puede ser un texto en blanco ni vacío
    @Schema(description = "Frecuencia con la que el usuario ahorra", example = "Media")
    @JsonProperty("frecuencia_ahorro")
    @NotNull(message = "La frecuencia_ahorro no puede ser nula. Valores permitidos: Nula, Baja, Media, Alta")
    private FrecuenciaAhorro frecuenciaAhorro;

    // La lista no puede estar vacía y debe validar cada transacción interna
    @Schema(description = "Lista de transacciones a clasificar")
    @JsonProperty("transacciones")
    @NotNull(message = "La lista de transacciones es obligatoria")
    @Size(min = 1, message = "Debes ingresar al menos una transacción para el análisis")
    @Valid // Esto le dice a Spring que también valide los datos dentro de TransactionDTO
    private List<TransactionDTO> transacciones;
}