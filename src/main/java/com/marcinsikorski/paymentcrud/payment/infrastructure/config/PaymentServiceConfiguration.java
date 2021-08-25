package com.marcinsikorski.paymentcrud.payment.infrastructure.config;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import com.marcinsikorski.paymentcrud.payment.infrastructure.PaymentService;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentCSVDataProviderAdapter;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentDbDataProviderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentServiceConfiguration {

    @Bean
    PaymentService paymentService(PaymentCSVDataProviderAdapter paymentCSVDataProviderAdapter){
        return new PaymentService(paymentCSVDataProviderAdapter);
    }

}
