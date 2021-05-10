package jobhunter.payment.service.service;

import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.models.Payment;
import jobhunter.payment.service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment makePayment(PaymentDTO paymentDTO) {
        return paymentRepository.save(new Payment(paymentDTO.getJobId(), paymentDTO.getEmployerId(),
                paymentDTO.getFreelancerId(), paymentDTO.getAmount()));
    }
}
