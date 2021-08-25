package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentDbDataProviderAdapter implements PaymentDataProvider {

    private PaymentDbRepository paymentDbRepository;

    @Override
    public Optional<PaymentDTO> findById(Long paymentId){
        Optional<PaymentEntity> optionalPaymentEntity = paymentDbRepository.findById(paymentId);
        return optionalPaymentEntity.map(this::newEntityToDTO);
    }

    @Override
    public List<PaymentDTO> findAll(){
        List<PaymentEntity> paymentEntities = paymentDbRepository.findAll();
        return paymentEntities.stream()
                .map(this::newEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> findAllByUserId(Long userId){
        List<PaymentEntity> paymentEntities = paymentDbRepository.findAllByUserId(userId);
        return paymentEntities.stream()
                .map(this::newEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> findAllByCurrency(Currency currency){
        List<PaymentEntity> paymentEntities = paymentDbRepository.findAllByCurrency(currency);
        return paymentEntities.stream()
                .map(this::newEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> findAllByUserIdAndCurrency(Long userId, Currency currency){
        List<PaymentEntity> paymentEntities = paymentDbRepository.findAllByUserIdAndCurrency(userId, currency);
        return paymentEntities.stream()
                .map(this::newEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO save(PaymentDTO paymentDTO){
        return newEntityToDTO(
                paymentDbRepository.save(DTOtoEntity(paymentDTO))
        );
    }

    @Override
    public PaymentDTO update(PaymentDTO paymentDTO){
        if(paymentDTO.getPaymentId() == null){
            throw new RuntimeException("Wrong request, paymentDTO needs to have payment id here");
        }
        PaymentEntity paymentEntity = paymentDbRepository.findById(paymentDTO.getPaymentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find given payment id"));
        paymentEntity.setAmount(paymentDTO.getAmount());
        paymentEntity.setCurrency(paymentDTO.getCurrency());
        paymentEntity.setUserId(paymentDTO.getUserId());
        paymentEntity.setTargetBankAccount(paymentDTO.getTargetBankAccount());
        return newEntityToDTO(paymentDbRepository.save(paymentEntity));
    }

    @Override
    public void delete(PaymentDTO paymentDTO){
        paymentDbRepository.delete(DTOtoEntity(paymentDTO));
    }

    private PaymentDTO newEntityToDTO(PaymentEntity paymentEntity){
        return PaymentDTO.builder()
                .paymentId(paymentEntity.getPaymentId())
                .amount(paymentEntity.getAmount())
                .currency(paymentEntity.getCurrency())
                .userId(paymentEntity.getUserId())
                .targetBankAccount(paymentEntity.getTargetBankAccount())
                .build();
    }

    private PaymentEntity DTOtoEntity(PaymentDTO paymentDTO){
        return PaymentEntity.builder()
                .paymentId(paymentDTO.getPaymentId())
                .amount(paymentDTO.getAmount())
                .currency(paymentDTO.getCurrency())
                .userId(paymentDTO.getUserId())
                .targetBankAccount(paymentDTO.getTargetBankAccount())
                .build();
    }

}
