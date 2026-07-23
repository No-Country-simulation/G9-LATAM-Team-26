package com.equipo26.financeai.controller;

import com.equipo26.financeai.service.FinancialService;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Controlador REST encargado de recibir las solicitudes HTTP
    relacionadas con el análisis financiero de los usuarios
*/

@RestController
@RequestMapping("/analisis-financiero")
public class FinancialController {

    @Autowired
    private FinancialService financialService;

    // Recibe la información financiera del usuario y retorna el diagnóstico
    @PostMapping
    public ResponseEntity<FinancialResponse> registrarFinanzas(
            @RequestBody @Valid FinancialRequest datos) {

        FinancialResponse resultado = financialService.analizar(datos);
        return ResponseEntity.ok(resultado);
    }

    // Obtiene los datos del análisis financiero correspondiente al id recibido
    @GetMapping("/{id}")
    public ResponseEntity<FinancialResponse> detallar(@PathVariable Long id) {
        return ResponseEntity.ok(financialService.buscarPorId(id));
    }
}