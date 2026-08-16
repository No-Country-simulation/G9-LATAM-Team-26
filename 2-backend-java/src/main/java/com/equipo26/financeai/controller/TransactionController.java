package com.equipo26.financeai.controller;

import com.equipo26.financeai.dto.ClasificarTransaccionesRequest;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;
import com.equipo26.financeai.service.FinancialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Endpoint dedicado a clasificar transacciones por categoría (Alimentación,
    Transporte, Salud, etc.) sin calcular el perfil financiero completo.
    Complementa a /analisis-financiero, que sí hace ambas cosas.
*/
@Slf4j
@Tag(name = "Clasificación de transacciones", description = "Endpoint para clasificar transacciones por categoría")
@RestController
@RequestMapping("/clasificar-transaccion")
@RequiredArgsConstructor
public class TransactionController {

    private final FinancialService financialService;

    @Operation(
            summary = "Clasificar transacciones por categoría",
            description = "Recibe una o varias transacciones y devuelve su categoría " +
                    "(Alimentación, Transporte, Salud, etc.), sin calcular el perfil financiero completo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacciones clasificadas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<TransaccionClasificadaDTO[]> clasificar(
            @RequestBody @Valid ClasificarTransaccionesRequest datos) {

        log.info("Clasificando {} transacciones", datos.getTransacciones().size());
        return ResponseEntity.ok(financialService.clasificarTransacciones(datos));
    }
}
