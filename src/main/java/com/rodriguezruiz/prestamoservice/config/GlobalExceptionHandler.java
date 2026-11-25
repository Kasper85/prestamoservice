package com.rodriguezruiz.prestamoservice.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static class EmailDuplicadoException extends RuntimeException {
        public EmailDuplicadoException(String message) {
            super(message);
        }
    }

    public static class CredencialesInvalidasException extends RuntimeException {
        public CredencialesInvalidasException(String message) {
            super(message);
        }
    }

    public static class RecursoNoEncontradoException extends RuntimeException {
        public RecursoNoEncontradoException(String message) {
            super(message);
        }
    }

    public static class ValidacionNegocioException extends RuntimeException {
        private final String campo;

        public ValidacionNegocioException(String campo, String message) {
            super(message);
            this.campo = campo;
        }

        public String getCampo() {
            return campo;
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(Map.of("errores", errores));
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessValidation(ValidacionNegocioException ex) {
        return ResponseEntity.badRequest().body(Map.of("errores", Map.of(ex.getCampo(), ex.getMessage())));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleConflict(EmailDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }
}
