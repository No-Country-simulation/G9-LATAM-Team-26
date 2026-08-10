package com.equipo26.financeai.controller;

import com.equipo26.financeai.exception.ErrorResponse;
import com.equipo26.financeai.service.FinancialService;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Controlador REST encargado de recibir las solicitudes HTTP
    relacionadas con el análisis financiero de los usuarios
*/
@Slf4j
@Tag(name="Análisis financiero", description = "Endpoint para analizar la salud financiera del usuario")
@RestController
@RequestMapping("/analisis-financiero")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;

    // Describe que hace este endpoint específico (aparece como título/descripción en Swagger UI)
    @Operation(
            summary = "Registrar y analizar información financiera",
            description = "Recibe transacciones e indicadores financieros del usuario, " +
                    "clasifica los gastos, determina el perfil financiero y genera recomendaciones."
    )

    //@ApiResponses Documenta cada posible código HTTP que este endpoint puede devolver
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Análisis creado exitosamente",
                    content = @Content(schema = @Schema(implementation = FinancialResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Errores de validación o formato/tipo de dato incorrecto.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Error de validación",
                                            value = """
                                                    [
                                                     {
                                                      "campo": "ingresoMensual",
                                                      "mensaje": "El ingreso mensual no puede ser menor a 0"
                                                     },
                                                     {
                                                      "campo": "nivelEndeudamiento",
                                                      "mensaje": "El nivel de endeudamiento no puede superar el 100%"
                                                     },
                                                     {
                                                      "campo": "frecuenciaAhorro",
                                                      "mensaje": "La frecuencia_ahorro no puede ser nula. Valores permitidos: Nula, Baja, Media, Alta"
                                                     }
                                                    ]
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Error de formato",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "mensaje": "El formato de los datos enviados no es válido.",
                                                      "fecha": "2026-08-09T23:15:39"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    // Recibe la información financiera del usuario y retorna el diagnóstico
    @PostMapping
    public ResponseEntity<FinancialResponse> registrarFinanzas(
            @RequestBody @Valid FinancialRequest datos) {

        log.info("Request recibido: ingreso={}, endeudamiento={}%, ahorro={}, transacciones={}",
                datos.getIngresoMensual(), datos.getNivelEndeudamiento(),
                datos.getFrecuenciaAhorro(), datos.getTransacciones().size());

        FinancialResponse resultado = financialService.analizar(datos);

        log.info("Respuesta enviada: perfil={}, probabilidad={}",
                resultado.getPerfilFinanciero(), resultado.getProbabilidad());

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @Operation(
            summary = "Consulta un análisis financiero por id",
            description = "Devuelve el resultado de un análisis previamente creado."
    )

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Análisis encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un análisis con ese id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                            "status": 404,
                                            "mensaje": "No se encontró un registro financiero con id: 100",
                                            "fecha": "2026-08-09T23:30:00"
                                            }
                                            """
                            )

                    )
            )
    })
    // Obtiene los datos del análisis financiero correspondiente al id recibido
    @GetMapping("/{id}")
    public ResponseEntity<FinancialResponse> detallar(@PathVariable Long id) {
        return ResponseEntity.ok(financialService.buscarPorId(id));
    }
}