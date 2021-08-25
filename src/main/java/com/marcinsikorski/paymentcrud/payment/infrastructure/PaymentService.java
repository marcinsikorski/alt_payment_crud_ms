package com.marcinsikorski.paymentcrud.payment.infrastructure;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.ModifiedPaymentInput;
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
        PaymentDTO inputPaymentDTO = newPaymentInputToDTO(newPaymentInput);
        PaymentDTO savedPaymentDTO = paymentDataProvider.save(inputPaymentDTO);
        return savedPaymentDTO.getPaymentId();
    }

    @Transactional
    public PaymentDTO updatePayment(Long paymentId, ModifiedPaymentInput modifiedPaymentInput){
        PaymentDTO paymentDTO = modifiedPaymentInputToDTO(modifiedPaymentInput, paymentId);
        PaymentDTO updatedDTO = paymentDataProvider.update(paymentDTO);
        return updatedDTO;
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

    private PaymentDTO modifiedPaymentInputToDTO(ModifiedPaymentInput modifiedPaymentInput, Long paymentId){
        return PaymentDTO.builder()
                .amount(modifiedPaymentInput.getAmount())
                .targetBankAccount(modifiedPaymentInput.getTargetBankAccount())
                .userId(modifiedPaymentInput.getUserId())
                .currency(modifiedPaymentInput.getCurrency())
                .paymentId(paymentId)
                .build();
    }
}
