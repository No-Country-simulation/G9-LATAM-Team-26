package com.equipo26.financeai.dto;

import lombok.Data;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO que mapea el JSON de entrada completo que recibiremos en la API.
 * Contiene los datos generales del usuario y su lista de transacciones.
 */
@Data
public class FinancialRequest {

    // El ingreso mensual no puede ser nulo ni menor a cero
    @NotNull(message = "El ingreso mensual es obligatorio")
    @Min(value = 0, message = "El ingreso mensual no puede ser menor a 0")
    private Double ingreso_mensual;

    // El endeudamiento debe estar entre 0% y 100%
    @NotNull(message = "El nivel de endeudamiento es obligatorio")
    @Min(value = 0, message = "El nivel de endeudamiento no puede ser menor a 0%")
    @Max(value = 100, message = "El nivel de endeudamiento no puede superar el 100%")
    private Integer nivel_endeudamiento;

    // La frecuencia no puede ser un texto en blanco ni vacío
    @NotBlank(message = "La frecuencia de ahorro es obligatoria")
    private String frecuencia_ahorro;

    // La lista no puede estar vacía y debe validar cada transacción interna
    @NotNull(message = "La lista de transacciones es obligatoria")
    @Size(min = 1, message = "Debes ingresar al menos una transacción para el análisis")
    @Valid // Esto le dice a Spring que también valide los datos dentro de TransactionDTO
    private List<TransactionDTO> transacciones;
}