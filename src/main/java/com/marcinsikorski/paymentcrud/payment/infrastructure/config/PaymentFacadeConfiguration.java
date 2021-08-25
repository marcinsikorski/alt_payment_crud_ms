package com.marcinsikorski.paymentcrud.payment.infrastructure.config;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentFacade;
import com.marcinsikorski.paymentcrud.payment.infrastructure.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentFacadeConfiguration {

    @Bean
    PaymentFacade paymentFacade(PaymentService paymentService){
        return new PaymentFacade(paymentService);
    }
}
