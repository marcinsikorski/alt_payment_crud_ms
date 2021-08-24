package com.marcinsikorski.paymentcrud;

import com.marcinsikorski.paymentcrud.payment.control.repository.PaymentRepository;
import com.marcinsikorski.paymentcrud.payment.entity.PaymentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Profile({"test"})
@SpringBootTest
public class PaymentRepositoryTests {
    @Resource
    private PaymentRepository paymentRepository;

    @Transactional
    @Test
    public void saveSinglePayment() {
        //create
        PaymentEntity paymentEntity = getExamplePaymentEntity();
        //save
        paymentRepository.save(paymentEntity);
        //get and check
        PaymentEntity paymentEntity1 = paymentRepository.getById(paymentEntity.getPaymentId());
        assertEquals("Saved payment id should be equal", paymentEntity.getPaymentId(), paymentEntity1.getPaymentId());
        assertEquals("Saved user id should be equal", paymentEntity.getUserId(), paymentEntity1.getUserId());
        assertEquals("Saved currency code should be equal", paymentEntity.getCurrency(), paymentEntity1.getCurrency());
        assertEquals("Saved amount should be equal", paymentEntity.getAmount(), paymentEntity1.getAmount());
        assertEquals("Saved target bank account should be equal", paymentEntity.getTargetBankAccount(), paymentEntity1.getTargetBankAccount());
    }

    @Transactional
    @Test
    public void findPagedPaymentsForUser() {
        //page configuration
        Integer perPage = 10;
        Integer pageNumber = 0;
        Long userId = 2L;

        //load test data
        List<PaymentEntity> list = generateExamplePayments();
        Long countOfPaymentsOfUser2 = list.stream().filter(p->p.getUserId().equals(2L)).count();
        paymentRepository.saveAll(list);

        //test get functions of repository
        Pageable pageable = PageRequest.of(pageNumber, perPage);
        Page<PaymentEntity> page = paymentRepository.findByUserId(userId, pageable);
        assertEquals("Should return 32 payments of user 2",
                page.getTotalElements(),
                countOfPaymentsOfUser2
        );
        assertEquals("Should return 4 pages of payments of user 2",
                page.getTotalPages(),
                (int) Math.ceil((double) countOfPaymentsOfUser2 / (double) perPage)
        );

    }

    @Transactional
    @Test
    public void findPagedPaymentForUserAndCurrency() {
        //page configuration
        Integer perPage = 10;
        Integer pageNumber = 0;
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
        paymentRepository.saveAll(list);

        //test get functions of repository
        Pageable pageable = PageRequest.of(pageNumber, perPage);
        Page<PaymentEntity> pageInPLN = paymentRepository.findByUserIdAndCurrency(userId, Currency.getInstance("PLN"),pageable);
        Page<PaymentEntity> pageInUSD = paymentRepository.findByUserIdAndCurrency(userId, Currency.getInstance("USD"),pageable);
        assertEquals("Should return 32 PLN payments of user 2",
                pageInPLN.getTotalElements(),
                countOfPaymentsOfUser2InPLN
        );
        assertEquals("Should return 4 pages of PLN payments of user 2",
                pageInPLN.getTotalPages(),
                (int) Math.ceil((double) countOfPaymentsOfUser2InPLN / (double) perPage)
        );
        assertEquals("Should return 0 USD payments of user 2",
                pageInUSD.getTotalElements(),
                countOfPaymentsOfUser2InUSD
                );
    }

    @Test
    public void savePaymentThenDelete() {
        //create payment to delete
        PaymentEntity paymentEntity = getExamplePaymentEntity();
        paymentRepository.save(paymentEntity);

        //delete
        PaymentEntity paymentEntity2 = paymentRepository.getById(paymentEntity.getPaymentId());
        paymentRepository.delete(paymentEntity2);

        //check if it is deleted
        Optional<PaymentEntity> optionalPaymentEntity = paymentRepository.findById(paymentEntity.getPaymentId());
        assertTrue("Payment should not be received", !optionalPaymentEntity.isPresent());

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
