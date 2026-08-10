package com.equipo26.financeai.exception;

public class FinancialNotFoundException extends RuntimeException {
    public FinancialNotFoundException(Long id) {
        super("No se encontró un registro financiero con id: " + id);
    }
}
