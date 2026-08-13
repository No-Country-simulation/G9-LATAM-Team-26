package com.equipo26.financeai.exception;

import org.springframework.validation.FieldError;

public record ValidationErrorResponse(String campo, String mensaje) {

    public ValidationErrorResponse(FieldError error){
        this(error.getField(), error.getDefaultMessage());
    }
}
