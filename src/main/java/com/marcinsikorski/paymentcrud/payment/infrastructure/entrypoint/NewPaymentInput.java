package com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewPaymentInput {
    @Min(value = 0L, message = "The value must be positive")
    @NotNull
    private BigDecimal amount;
    @Min(value = 0L, message = "User id has to be valid, positive number")
    @NotNull
    private Long userId;
    @NotNull
    private Currency currency;
    @NotEmpty(message = "Target Bank Account cannot be empty")
    private String targetBankAccount;
}
