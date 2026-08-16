package com.equipo26.financeai.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j // Esta anotación ya crea el objeto 'log' automáticamente
@RestControllerAdvice
public class GlobalExceptionHandler {


    // 400 - Errores de validación (@Valid falló en FinancialRequest)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorResponse>> handleValidationError(MethodArgumentNotValidException ex) {

        List<ValidationErrorResponse> errores = ex.getFieldErrors()
                .stream()
                .map(ValidationErrorResponse::new)
                .toList();

        return ResponseEntity.badRequest().body(errores);
    }
    // 400 - JSON mal formado o datos que no pueden convertirse al tipo esperado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex){
        log.warn("JSON inválido recibido: {}", ex.getMessage());

        var error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "El formato de los datos enviados no es válido.",
                LocalDateTime.now());
        return ResponseEntity.badRequest().body(error);
    }

    // 404 - Registro financiero no encontrado
    @ExceptionHandler(FinancialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFinancialNotFound(FinancialNotFoundException ex) {
        var error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 503 - El modelo de IA no está disponible o falló al cargar
    @ExceptionHandler(ModelUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleModelUnavailable(ModelUnavailableException ex) {
        log.error("Modelo de IA no disponible: {}", ex.getMessage());

        var error = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // 500 - Cualquier otro error no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {

        log.error("Error inesperado", ex);

        var error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error inesperado. Intenta más tarde.",
                LocalDateTime.now());
        return ResponseEntity.internalServerError().body(error);
    }

    // 503 - Error en la integración con el servicio de ML
    @ExceptionHandler(MlServiceException.class)
    public ResponseEntity<Map<String, String>> handleMlService(MlServiceException ex) {
        log.error("Error de integración con ML: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", ex.getMessage()));
    }
    //404 - Ruta o recurso HTTP no controlado
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(NoResourceFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", 404,
                        "mensaje", "La ruta solicitada no existe.",
                        "fecha", LocalDateTime.now()));
    }
}