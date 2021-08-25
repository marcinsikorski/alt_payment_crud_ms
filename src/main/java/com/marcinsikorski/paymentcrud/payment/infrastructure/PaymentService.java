package com.marcinsikorski.paymentcrud.payment.infrastructure;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.NewPaymentInput;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentDataProviderAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@AllArgsConstructor
@Service
public class PaymentService {

    private final PaymentDataProviderAdapter paymentDataProviderAdapter;

    public PaymentDTO findById(Long paymentId){
        return paymentDataProviderAdapter.findById(paymentId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find given payment id")
        );
    }

    @Transactional
    public Long savePayment(NewPaymentInput newPaymentInput){
        PaymentDTO paymentDTO = newPaymentInputToDTO(newPaymentInput);
        return paymentDataProviderAdapter.save(paymentDTO).getPaymentId();
    }

    @Transactional
    public void deletePayment(Long paymentId){
        Optional<PaymentDTO> optionalPaymentDTO = paymentDataProviderAdapter.findById(paymentId);
        if(!optionalPaymentDTO.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find given payment id");
        }
        paymentDataProviderAdapter.delete(optionalPaymentDTO.get());
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
