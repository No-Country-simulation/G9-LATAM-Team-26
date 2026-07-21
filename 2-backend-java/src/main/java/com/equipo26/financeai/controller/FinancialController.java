package com.equipo26.financeai.controller;

import com.equipo26.financeai.service.FinancialService;
import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/*
    Controlador REST encargado de recibir las solicitudes HTTP
    relacionadas con el analisis financiero de los usuarios
*/

@RestController
@RequestMapping("/analisis-financiero")//Ruta de la solicitudes del controlador
public class FinancialController {

    @Autowired
    private FinancialService financialService;

    //Manda información financiera del usuario
    @PostMapping
    public ResponseEntity<FinancialResponse> registrarFinanzas
        //@Valid ejecuta las validaciones definidas en la clase FinancialRequest
            (@RequestBody @Valid FinancialRequest datos, UriComponentsBuilder uriComponentsBuilder) {
        //Calcula análisis financiero y devuelve DTO con el resultado
        FinancialResponse resultado = financialService.analizar(datos);
        //Construye la URI del recurso creado para incluirla en la respuesta HTTP
        var uri = uriComponentsBuilder
                .path("/analisis-financiero/{id}")
                .buildAndExpand(resultado.id())
                .toUri();
        return ResponseEntity.created(uri).body(resultado);
    }

    //Obtiene los datos del analisis financiero correspondiente al id recibido
    @GetMapping("/{id}")
    public ResponseEntity<FinancialResponse> detallar(@PathVariable Long id){
        return ResponseEntity.ok(financialService.buscarPorId(id));
    }
}
