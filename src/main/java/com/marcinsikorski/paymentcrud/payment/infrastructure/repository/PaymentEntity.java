package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.CurrencyType;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "payment")
public class PaymentEntity {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long paymentId;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Currency currency;
    //length based on ISO 13616 for different countries
    @Column(nullable = false, length = 32)
    private String targetBankAccount;
}
