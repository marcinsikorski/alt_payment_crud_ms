package com.marcinsikorski.paymentcrud.payment.control.repository;

import com.marcinsikorski.paymentcrud.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Currency;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Page<PaymentEntity> findByUserId(Long userId, Pageable pageable);
    Page<PaymentEntity> findByUserIdAndCurrency(Long userId, Currency currency, Pageable pageable);
    Page<PaymentEntity> findByUserIdAndTargetBankAccount(Long userId, String targetBankAccount, Pageable pageable);
}
