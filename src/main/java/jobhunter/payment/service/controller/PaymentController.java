package jobhunter.payment.service.controller;

import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.models.Payment;
import jobhunter.payment.service.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public Payment makePayment(@RequestBody PaymentDTO paymentDTO) {
        return paymentService.makePayment(paymentDTO);
    }
}
