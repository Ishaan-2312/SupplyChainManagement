package com.IshaanBansal.SupplyChainManagement.Controller;


import com.IshaanBansal.SupplyChainManagement.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<String> initiatePayment(@RequestParam Long orderId, @RequestParam BigInteger amount) throws Exception {
        return ResponseEntity.ok(paymentService.initiatePayment(orderId, amount));
    }
    @GetMapping("/getPaymentLink")
    public ResponseEntity<String> getExistingOrderPaymentLink(@RequestParam Long orderId) throws Exception {
        return ResponseEntity.ok(paymentService.getExistingOrderPaymentLink(orderId));
    }


    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestParam Long orderId, @RequestParam String payeeAddress) throws Exception {
        return ResponseEntity.ok(paymentService.recordPayment(orderId, payeeAddress));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawPayment(@RequestParam Long orderId) throws Exception {
        return ResponseEntity.ok(paymentService.withdrawPayment(orderId));
    }
    @GetMapping("/fetch")
    public String getPaymentId(@RequestParam Long orderId) throws Exception{
        return paymentService.processPayment(orderId);
    }
}


