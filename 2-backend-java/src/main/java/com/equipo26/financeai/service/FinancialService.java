package com.equipo26.financeai.service;

import com.equipo26.financeai.dto.FinancialRequest;
import com.equipo26.financeai.dto.FinancialResponse;
import com.equipo26.financeai.dto.ClasificarTransaccionesRequest;
import com.equipo26.financeai.dto.TransaccionClasificadaDTO;
/*
    Define el contrato para los servicios encargados de analizar y consultar información financiera
*/
public interface FinancialService {
    // Recibe los datos necesarios para realizar un análisis financiero y devuelve el resultado de ese análisis
    FinancialResponse analizar(FinancialRequest datos);
    FinancialResponse buscarPorId(Long id);
    FinancialResponse editar(Long id, FinancialRequest datos);
    // Clasifica transacciones sin calcular el perfil financiero completo
    TransaccionClasificadaDTO[] clasificarTransacciones(ClasificarTransaccionesRequest datos);
}
