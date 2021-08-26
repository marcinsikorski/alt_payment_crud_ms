package com.marcinsikorski.paymentcrud.payment.infrastructure.entrypoint;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.infrastructure.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("")
    public ResponseEntity<List<PaymentDTO>> getPaymentById(@RequestParam(name = "userId", required = false) Long userId,
                                                           @RequestParam(name = "currency", required = false) Currency currency) {
        List<PaymentDTO> paymentDTOList = paymentService.findByFilter(userId, currency);
        return ResponseEntity.status(HttpStatus.OK).body(paymentDTOList);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable("paymentId") Long paymentId) {
        PaymentDTO paymentDTO = paymentService.findById(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(paymentDTO);
    }

    @PostMapping("")
    public ResponseEntity<Long> registerPayment(@Valid @RequestBody NewPaymentInput newPaymentInput) {
        Long paymentId = this.paymentService.savePayment(newPaymentInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentId);
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> updatePayment(@PathVariable("paymentId") Long paymentId, @Valid @RequestBody ModifiedPaymentInput modifiedPaymentInput) {
        PaymentDTO paymentDTO = this.paymentService.updatePayment(paymentId, modifiedPaymentInput);
        return ResponseEntity.status(HttpStatus.OK).body(paymentDTO);
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity updatePayment(@PathVariable("paymentId") Long paymentId) {
        this.paymentService.deletePayment(paymentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(paymentId);
    }
}
