package com.marcinsikorski.paymentcrud.payment.infrastructure;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.NewPaymentInput;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentDbDataProviderAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

public class PaymentService {

    private final PaymentDataProvider paymentDataProvider;

    public PaymentService(PaymentDataProvider paymentDataProvider){
        this.paymentDataProvider = paymentDataProvider;
    }

    public PaymentDTO findById(Long paymentId){
        return paymentDataProvider.findById(paymentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find given payment id")
        );
    }

    @Transactional
    public Long savePayment(NewPaymentInput newPaymentInput){
        PaymentDTO paymentDTO = newPaymentInputToDTO(newPaymentInput);
        return paymentDataProvider.save(paymentDTO).getPaymentId();
    }

    @Transactional
    public void deletePayment(Long paymentId){
        Optional<PaymentDTO> optionalPaymentDTO = paymentDataProvider.findById(paymentId);
        if(!optionalPaymentDTO.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find given payment id");
        }
        paymentDataProvider.delete(optionalPaymentDTO.get());
    }

    private PaymentDTO newPaymentInputToDTO(NewPaymentInput newPaymentInput){
        return PaymentDTO.builder()
                .amount(newPaymentInput.getAmount())
                .targetBankAccount(newPaymentInput.getTargetBankAccount())
                .userId(newPaymentInput.getUserId())
                .currency(newPaymentInput.getCurrency())
                .build();
    }
}
