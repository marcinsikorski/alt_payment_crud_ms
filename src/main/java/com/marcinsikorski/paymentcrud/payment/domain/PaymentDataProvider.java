package com.marcinsikorski.paymentcrud.payment.domain;

import java.util.Optional;

public interface PaymentDataProvider {

    Optional<PaymentDTO> findById(Long paymentId);
}
