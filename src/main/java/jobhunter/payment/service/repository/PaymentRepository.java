package jobhunter.payment.service.repository;

import jobhunter.payment.service.models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
