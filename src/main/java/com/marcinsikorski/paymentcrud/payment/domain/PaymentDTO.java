package com.marcinsikorski.paymentcrud.payment.domain;


import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long paymentId;
    private BigDecimal amount;
    private Long userId;
    private Currency currency;
    private String targetBankAccount;
}
