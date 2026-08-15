package com.equipo26.financeai.entity;

import com.equipo26.financeai.dto.FrecuenciaAhorro;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "analisis_historial")
public class AnalisisFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal ingresoMensual;

    private Integer nivelEndeudamiento;

    private FrecuenciaAhorro frecuenciaAhorro;

    private String perfilFinanciero;

    private Double probabilidad;

    // Usamos longitudes grandes para evitar que se corte la información
    @Column(length = 1500)
    private String resumenGastos;

    @Column(length = 2500)
    private String recomendaciones;
}