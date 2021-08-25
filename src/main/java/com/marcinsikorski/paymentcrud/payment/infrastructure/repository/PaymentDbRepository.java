package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.List;

@Repository
public interface PaymentDbRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findAll();
    List<PaymentEntity> findAllByUserId(Long userId);
    List<PaymentEntity> findAllByCurrency(Currency currency);
    List<PaymentEntity> findAllByUserIdAndCurrency(Long userId, Currency currency);
    List<PaymentEntity> findAllByUserIdAndTargetBankAccount(Long userId, String targetBankAccount);
}
