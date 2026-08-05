package com.equipo26.financeai.exception;

public class MlServiceException extends RuntimeException {
    public MlServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
