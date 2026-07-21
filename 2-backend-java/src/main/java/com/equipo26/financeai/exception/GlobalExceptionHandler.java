package com.equipo26.financeai.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 400 - Errores de validación (@Valid falló en FinancialRequest)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorResponse>> handleValidationError(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse> errores = ex.getFieldErrors().stream()
                .map(ValidationErrorResponse::new)
                .toList();
        return ResponseEntity.badRequest().body(errores);
    }

    public record ValidationErrorResponse(String campo, String mensaje) {
        public ValidationErrorResponse(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }

    // 404 - Registro financiero no encontrado
    @ExceptionHandler(FinancialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(FinancialNotFoundException ex) {
        var error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    public class FinancialNotFoundException extends RuntimeException {
        public FinancialNotFoundException(Long id) {
            super("No se encontró un registro financiero con id: " + id);
        }
    }

    // 503 - El modelo de IA no está disponible o falló al cargar
    @ExceptionHandler(ModelUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleModelUnavailable(ModelUnavailableException ex) {
        var error = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    public class ModelUnavailableException extends RuntimeException {
        public ModelUnavailableException(String mensaje) {
            super(mensaje);
        }
    }

    public record ErrorResponse(int status, String mensaje, LocalDateTime fecha) {}

    // 500 - Cualquier otro error no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        var error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error inesperado. Intenta más tarde.",
                LocalDateTime.now()
        );
        return ResponseEntity.internalServerError().body(error);
    }
}
