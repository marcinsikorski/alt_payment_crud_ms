package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCSVRecord {
    @CsvBindByName
    @CsvBindByPosition(position = 0)
    private Long paymentId;
    @CsvBindByName
    @CsvBindByPosition(position = 1)
    private BigDecimal amount;
    @CsvBindByName
    @CsvBindByPosition(position = 2)
    private Long userId;
    @CsvBindByName
    @CsvBindByPosition(position = 3)
    private Currency currency;
    @CsvBindByName
    @CsvBindByPosition(position = 4)
    private String targetBankAccount;
}
