package com.marcinsikorski.paymentcrud;

import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentDbRepository;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Profile({"test"})
@SpringBootTest
public class PaymentDbRepositoryTests {
    @Resource
    private PaymentDbRepository paymentDbRepository;


    @Transactional
    @Test
    public void saveSinglePayment() {
        //create
        PaymentEntity paymentEntity = getExamplePaymentEntity();
        //save
        paymentDbRepository.save(paymentEntity);
        //get and check
        PaymentEntity paymentEntity1 = paymentDbRepository.getById(paymentEntity.getPaymentId());
        Assertions.assertEquals(paymentEntity.getPaymentId(), paymentEntity1.getPaymentId(), "Saved payment id should be equal");
        Assertions.assertEquals(paymentEntity.getUserId(), paymentEntity1.getUserId(), "Saved user id should be equal");
        Assertions.assertEquals(paymentEntity.getCurrency(), paymentEntity1.getCurrency(), "Saved currency code should be equal");
        Assertions.assertEquals(paymentEntity.getAmount(), paymentEntity1.getAmount(), "Saved amount should be equal");
        Assertions.assertEquals(paymentEntity.getTargetBankAccount(), paymentEntity1.getTargetBankAccount(), "Saved target bank account should be equal");
    }

    @Transactional
    @Test
    public void findPaymentsForUser() {
        //input configuration
        Long userId = 2L;

        //load test data
        List<PaymentEntity> list = generateExamplePayments();
        Long countOfPaymentsOfUser2 = list.stream().filter(p->p.getUserId().equals(2L)).count();
        paymentDbRepository.saveAll(list);

        //test get functions of repository
        List<PaymentEntity> returnedList = paymentDbRepository.findAllByUserId(userId);
        Assertions.assertEquals(
                returnedList.size(),
                countOfPaymentsOfUser2,
                "Should return 32 payments of user 2"
        );
    }

    @Transactional
    @Test
    public void findPaymentForUserAndCurrency() {
        //input configuration
        Long userId = 2L;

        //load test data
        List<PaymentEntity> list = generateExamplePayments();
        Long countOfPaymentsOfUser2InPLN = list.stream().filter(
                p -> p.getCurrency().equals(Currency.getInstance("PLN")) &&
                        p.getUserId().equals(userId)
        ).count();
        Long countOfPaymentsOfUser2InUSD = list.stream().filter(
                p -> p.getCurrency().equals(Currency.getInstance("USD")) &&
                        p.getUserId().equals(userId)
        ).count();
        paymentDbRepository.saveAll(list);

        //test get functions of repository
        List<PaymentEntity> listInPLN = paymentDbRepository.findAllByUserIdAndCurrency(userId, Currency.getInstance("PLN"));
        List<PaymentEntity> listInUSD = paymentDbRepository.findAllByUserIdAndCurrency(userId, Currency.getInstance("USD"));
        Assertions.assertEquals(listInPLN.size(),
                countOfPaymentsOfUser2InPLN,
                "Should return 32 PLN payments of user 2"
        );
        Assertions.assertEquals(
                listInUSD.size(),
                countOfPaymentsOfUser2InUSD,
                "Should return 0 USD payments of user 2"
                );
    }

    @Test
    public void savePaymentThenDelete() {
        //create payment to delete
        PaymentEntity paymentEntity = getExamplePaymentEntity();
        paymentDbRepository.save(paymentEntity);

        //delete
        PaymentEntity paymentEntity2 = paymentDbRepository.getById(paymentEntity.getPaymentId());
        paymentDbRepository.delete(paymentEntity2);

        //check if it is deleted
        Optional<PaymentEntity> optionalPaymentEntity = paymentDbRepository.findById(paymentEntity.getPaymentId());
        Assertions.assertTrue(!optionalPaymentEntity.isPresent(), "Payment should not be received" );
    }

    @Transactional
    @Test
    public void shouldThrowValidationErrorOnMissingUser(){
        PaymentEntity missingUserId = PaymentEntity.builder()
                .amount(BigDecimal.valueOf(new Double(100.00)))
                .currency(Currency.getInstance("PLN"))
                .targetBankAccount("PL86109024026573543627562617")
                .build();
        assertThrows(RuntimeException.class, () -> paymentDbRepository.saveAndFlush(missingUserId));
    }

    @Transactional
    @Test
    public void shouldThrowValidationErrorOnMissingCurrency(){
        PaymentEntity missingCurrency = PaymentEntity.builder()
                .amount(BigDecimal.valueOf(new Double(200.00)))
                .userId(4L)
                .targetBankAccount("PL86109024026573543627562617")
                .build();
        assertThrows(RuntimeException.class, () -> paymentDbRepository.saveAndFlush(missingCurrency));
    }

    @Transactional
    @Test
    public void shouldThrowValidationErrorOnMissingAmount(){
        PaymentEntity missingAmount = PaymentEntity.builder()
                .userId(4L)
                .currency(Currency.getInstance("PLN"))
                .targetBankAccount("PL86109024026573543627562617")
                .build();
        assertThrows(RuntimeException.class, () -> paymentDbRepository.saveAndFlush(missingAmount));
    }

    @Transactional
    @Test
    public void shouldThrowValidationErrorOnMissingTargetBankNumber(){
        PaymentEntity missingTargetBankAccount = PaymentEntity.builder()
                .amount(BigDecimal.valueOf(new Double(100.00)))
                .currency(Currency.getInstance("PLN"))
                .userId(5L)
                .build();
        assertThrows(RuntimeException.class, () -> paymentDbRepository.saveAndFlush(missingTargetBankAccount));
    }

    @Transactional
    @Test
    public void shouldThrowValidationErrorOnTooLongTargetBankNumber(){
        PaymentEntity tooLongTargetBankNumber = PaymentEntity.builder()
                .amount(BigDecimal.valueOf(new Double(100.00)))
                .currency(Currency.getInstance("PLN"))
                .userId(5L)
                .targetBankAccount("PL86109024026573543342343264836248362486248264962324324627562617")
                .build();
        assertThrows(RuntimeException.class, () -> paymentDbRepository.saveAndFlush(tooLongTargetBankNumber));
    }

    private List<PaymentEntity> generateExamplePayments(){
        Integer countOfPaymentsOfUser1 = 50;
        Integer countOfPaymentsOfUser2 = 32;
        List<PaymentEntity> list = new ArrayList<>();
        for(int i=0;i<countOfPaymentsOfUser1;i++){
            list.add(
                    PaymentEntity.builder()
                            .amount(BigDecimal.valueOf(getRandomNumber(5,99999)))
                            .userId(1L)
                            .currency(Currency.getInstance("PLN"))
                            .targetBankAccount("PL34109024022414643971929632")
                            .build()
            );
        }
        //load user 2
        for(int i=0;i<countOfPaymentsOfUser2;i++){
            list.add(
                    PaymentEntity.builder()
                            .amount(BigDecimal.valueOf(getRandomNumber(5,99999)))
                            .userId(2L)
                            .currency(Currency.getInstance("PLN"))
                            .targetBankAccount("PL14109024026288554829314994")
                            .build()
            );
        }
        return list;
    }

    private PaymentEntity getExamplePaymentEntity() {
        return PaymentEntity.builder()
                .amount(BigDecimal.valueOf(new Double(100.00)))
                .userId(4L)
                .currency(Currency.getInstance("PLN"))
                .targetBankAccount("PL86109024026573543627562617")
                .build();
    }

    private double getRandomNumber(int min, int max) {
        int integerNumber= (int) ((Math.random() * (max*100 - min*100)) + min*100);
        return new Double(integerNumber)/100;
    }

}
