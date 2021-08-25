package com.marcinsikorski.paymentcrud;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.infrastructure.PaymentService;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.ModifiedPaymentInput;
import com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint.NewPaymentInput;
import com.marcinsikorski.paymentcrud.payment.infrastructure.repository.PaymentEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void testDeletePayment() {
        NewPaymentInput newPaymentInput = getExamplePaymentInput();
        Long paymentId = paymentService.savePayment(newPaymentInput);
        paymentService.deletePayment(paymentId);
        Assertions.assertThrows(ResponseStatusException.class, () -> paymentService.findById(paymentId));
    }

    @Test
    public void shouldThrowExceptionWhenCannotFindPaymentToDelete() {
        Assertions.assertThrows(ResponseStatusException.class,
                () -> paymentService.deletePayment(9432423432424L),
                "That number does not exist in db, so cannot be deleted");
    }

    @Test
    public void shouldThrowExceptionWhenCannotFindPaymentToUpdate() {
        NewPaymentInput newPaymentInput = getExamplePaymentInput();
        Long paymentId = paymentService.savePayment(newPaymentInput);
        PaymentDTO beforeUpdate = paymentService.findById(paymentId);
        ModifiedPaymentInput modifiedPaymentInput = ModifiedPaymentInput.builder()
                .amount(BigDecimal.valueOf(new Double(4350.00)))
                .currency(Currency.getInstance("USD"))
                .userId(beforeUpdate.getUserId())
                .targetBankAccount(beforeUpdate.getTargetBankAccount())
                .build();
        Assertions.assertThrows(ResponseStatusException.class,
                () -> paymentService.updatePayment(9432423432424L, modifiedPaymentInput),
                "That number does not exist in db, so cannot be updated");
    }

    @Test
    public void testUpdatePayment() {
        NewPaymentInput newPaymentInput = getExamplePaymentInput();
        Long paymentId = paymentService.savePayment(newPaymentInput);
        PaymentDTO beforeUpdate = paymentService.findById(paymentId);
        ModifiedPaymentInput modifiedPaymentInput = ModifiedPaymentInput.builder()
                .amount(BigDecimal.valueOf(new Double(4350.00)))
                .currency(Currency.getInstance("USD"))
                .userId(beforeUpdate.getUserId())
                .targetBankAccount(beforeUpdate.getTargetBankAccount())
                .build();
        PaymentDTO updatedPayment = paymentService.updatePayment(paymentId, modifiedPaymentInput);
        Assertions.assertEquals(updatedPayment.getPaymentId(), beforeUpdate.getPaymentId(), "Payment id wasn't modified so should be equal");
        Assertions.assertEquals(updatedPayment.getUserId(), beforeUpdate.getUserId(), "Payment user wasn't modified so should be equal");
        Assertions.assertEquals(updatedPayment.getTargetBankAccount(), beforeUpdate.getTargetBankAccount(), "Saved target bank account should be equal");
        Assertions.assertNotEquals(updatedPayment.getCurrency(), beforeUpdate.getCurrency(), "Payment currency was modified so should not be equal");
        Assertions.assertNotEquals(updatedPayment.getAmount().stripTrailingZeros(), beforeUpdate.getAmount().stripTrailingZeros(), "Amount was modified so should be not equal");

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
