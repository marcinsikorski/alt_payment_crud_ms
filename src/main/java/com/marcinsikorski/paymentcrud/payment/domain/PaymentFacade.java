package com.marcinsikorski.paymentcrud.payment.domain;

import com.marcinsikorski.paymentcrud.payment.infrastructure.PaymentService;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.ModifiedPaymentInput;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.NewPaymentInput;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

//Facade for futher improvements when other domain areas need use Payments
public class PaymentFacade {

    private final PaymentService paymentService;
    public PaymentFacade(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    public PaymentDTO findById(Long paymentId){
        return this.paymentService.findById(paymentId);
    }

    public List<PaymentDTO> findByFilter(Long userId, Currency currency){
        return this.paymentService.findByFilter(userId, currency);
    }

    public Long savePayment(NewPaymentInput newPaymentInput){
        return this.paymentService.savePayment(newPaymentInput);
    }

    public PaymentDTO updatePayment(Long paymentId, ModifiedPaymentInput modifiedPaymentInput){
        return this.paymentService.updatePayment(paymentId, modifiedPaymentInput);
    }

    public void deletePayment(Long paymentId){
        this.paymentService.deletePayment(paymentId);
    }
}
