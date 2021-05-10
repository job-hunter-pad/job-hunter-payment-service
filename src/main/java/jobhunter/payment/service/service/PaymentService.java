package jobhunter.payment.service.service;

import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.models.Payment;

public interface PaymentService {
    Payment makePayment(PaymentDTO paymentDTO);
}
