package com.marcinsikorski.paymentcrud.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsController {

    @GetMapping("/healthCheck")
    public String helloWorld() {
        return "Hello World!";
    }
}
