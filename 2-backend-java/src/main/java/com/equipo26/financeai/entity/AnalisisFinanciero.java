package com.equipo26.financeai.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "analisis_historial")
public class AnalisisFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String perfilFinanciero;

    private Double probabilidad;

    // Usamos longitudes grandes para evitar que se corte la información
    @Column(length = 1500)
    private String resumenGastos;

    @Column(length = 2500)
    private String recomendaciones;
}