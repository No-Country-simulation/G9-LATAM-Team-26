package com.equipo26.financeai.controller;

import com.equipo26.financeai.service.FinancialService;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
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
@RestController
@RequestMapping("/analisis-financiero")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;

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

    // Obtiene los datos del análisis financiero correspondiente al id recibido
    @GetMapping("/{id}")
    public ResponseEntity<FinancialResponse> detallar(@PathVariable Long id) {
        return ResponseEntity.ok(financialService.buscarPorId(id));
    }
}