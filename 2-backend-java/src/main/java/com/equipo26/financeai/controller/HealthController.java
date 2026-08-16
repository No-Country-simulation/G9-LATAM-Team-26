package com.equipo26.financeai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
    Endpoint simple en "/" para que el hosting (Render) y servicios de
    keep-alive (UptimeRobot, cron-job.org) puedan verificar que el
    servicio está vivo, sin tocar el endpoint real de negocio.
*/
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> healthCheck() {
        return Map.of("status", "ok", "servicio", "financeai-backend-java");
    }
}
