package com.marcinsikorski.paymentcrud.payment.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentDataProvider {
    Optional<PaymentDTO> findById(Long paymentId);
    List<PaymentDTO> findAllByUserId(Long userId);
    PaymentDTO save(PaymentDTO paymentDTO);
    PaymentDTO update(PaymentDTO paymentDTO);
    void delete(PaymentDTO paymentDTO);
}
