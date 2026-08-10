package com.equipo26.financeai.exception;

/**
 * Excepción lanzada cuando el modelo de IA no está disponible.
 */
public class ModelUnavailableException extends RuntimeException{
    public ModelUnavailableException(String mensaje){
        super(mensaje);
    }
}
