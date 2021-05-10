package jobhunter.payment.service.repository;

import jobhunter.payment.service.models.JobHunterCustomer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JobHunterCustomerRepository extends MongoRepository<JobHunterCustomer, String> {
    Optional<JobHunterCustomer> findByStripeId(String stripeId);
}
