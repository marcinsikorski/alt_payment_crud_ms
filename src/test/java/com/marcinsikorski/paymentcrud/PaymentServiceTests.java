package com.marcinsikorski.paymentcrud;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.infrastructure.PaymentService;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.NewPaymentInput;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertThat;

@Profile({"test"})
@SpringBootTest
public class PaymentServiceTests {

    @Resource
    private PaymentService paymentService;

    @Test
    public void saveSinglePayment() {
        NewPaymentInput newPaymentInput = getExamplePaymentInput();
        Long paymentId = paymentService.savePayment(newPaymentInput);

        PaymentDTO createdDTO = paymentService.findById(paymentId);

        Assertions.assertNotNull(createdDTO.getPaymentId(), "Saved payment id should be assigned");
        Assertions.assertEquals(paymentId,createdDTO.getPaymentId(), "Saved payment id should be the same from both functions");
        Assertions.assertEquals(newPaymentInput.getUserId(), createdDTO.getUserId(), "Saved user id should be equal");
        Assertions.assertEquals(newPaymentInput.getCurrency(), createdDTO.getCurrency(), "Saved currency code should be equal");
        Assertions.assertEquals(newPaymentInput.getAmount().stripTrailingZeros(), createdDTO.getAmount().stripTrailingZeros(), "Saved amount should be equal");
        Assertions.assertEquals(newPaymentInput.getTargetBankAccount(), createdDTO.getTargetBankAccount(), "Saved target bank account should be equal");
    }

    @Test
    public void findPaymentById() {
        PaymentDTO foundDTO = paymentService.findById(8L);

        Assertions.assertNotNull(foundDTO.getPaymentId(), "Saved payment id should be assigned");
        Assertions.assertNotNull(foundDTO.getUserId(), "Saved user id should not be null");
        Assertions.assertNotNull(foundDTO.getCurrency(), "Saved currency code should not be null");
        Assertions.assertNotNull(foundDTO.getAmount(),  "Saved amount should not be null");
        Assertions.assertNotNull(foundDTO.getTargetBankAccount(), "Saved target bank account should not be null");
    }

    private NewPaymentInput getExamplePaymentInput() {
        return NewPaymentInput.builder()
                .amount(BigDecimal.valueOf(new Double(250.00)))
                .userId(4L)
                .currency(Currency.getInstance("PLN"))
                .targetBankAccount("PL86109024026573543627562617")
                .build();
    }
}
