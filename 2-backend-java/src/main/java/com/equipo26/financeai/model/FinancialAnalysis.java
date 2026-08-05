package com.equipo26.financeai.model;

import com.equipo26.financeai.model.enums.FinancialProfile;
import com.equipo26.financeai.model.enums.SavingsFrecuency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
    Representa tabla en base de datos donde se guardara el analisis ya calculado
*/
@Entity
@Table(name="Analizis_Financiero")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FinancialAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double ingreso_mensual;
    private Integer nivel_endeudamiento;

    @Enumerated(EnumType.STRING)
    private SavingsFrecuency frecuencia_ahorro;

    @Enumerated(EnumType.STRING)
    private FinancialProfile perfil_financiero;

    private Double probabilidad;

    @ElementCollection
    private Map<String, Double> resumen_gastos;

    @ElementCollection
    private List<String> recomendaciones;

    private LocalDateTime fechaCreacion;
}
