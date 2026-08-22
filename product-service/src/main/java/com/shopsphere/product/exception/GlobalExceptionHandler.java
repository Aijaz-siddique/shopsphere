package com.shopsphere.product.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ValidationErrorResponse handleValidationException(
                MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult()
                        .getFieldErrors()
                        .forEach(error ->
                                errors.put(error.getField(), error.getDefaultMessage())
                        );

                return new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                );
        }

        @ExceptionHandler(IllegalArgumentException.class)
                @ResponseStatus(HttpStatus.BAD_REQUEST)
                public Map<String, String> handleIllegalArgumentException(
                        IllegalArgumentException ex) {

                return Map.of(
                        "status", "400",
                        "message", ex.getMessage()
                );
        }

        @ExceptionHandler(ProductNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public Map<String, String> handleProductNotFoundException(
                ProductNotFoundException ex) {

                return Map.of(
                        "status", "404",
                        "message", ex.getMessage()
                );
        }
        
}