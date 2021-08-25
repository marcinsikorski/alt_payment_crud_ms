package com.marcinsikorski.paymentcrud.payment.domain;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface PaymentDataProvider {
    Optional<PaymentDTO> findById(Long paymentId);
    List<PaymentDTO> findAll();
    List<PaymentDTO> findAllByUserId(Long userId);
    List<PaymentDTO> findAllByCurrency(Currency currency);
    List<PaymentDTO> findAllByUserIdAndCurrency(Long userId, Currency currency);
    PaymentDTO save(PaymentDTO paymentDTO);
    PaymentDTO update(PaymentDTO paymentDTO);
    void delete(PaymentDTO paymentDTO);
}
