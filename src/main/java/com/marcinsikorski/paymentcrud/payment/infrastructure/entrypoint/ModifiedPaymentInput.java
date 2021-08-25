package com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint;

import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifiedPaymentInput {
    private BigDecimal amount;
    private Long userId;
    private Currency currency;
    private String targetBankAccount;
}
