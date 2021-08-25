package com.marcinsikorski.paymentcrud.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class CustomExceptionHandlerAspect {
    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity authenticationExceptionThrown(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
}
