package jobhunter.payment.service.service;

import com.stripe.exception.StripeException;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.customer.JobHunterCustomer;

import java.util.Optional;

public interface JobHunterCustomerService {
    JobHunterCustomer createCustomer(CreateCustomerDTO createCustomerDTO) throws StripeException;

    Optional<JobHunterCustomer> updateCustomer(UpdateCustomerDTO updateCustomerDTO) throws StripeException;

    Optional<JobHunterCustomer> getCustomer(String userId);

    Optional<JobHunterCustomer> getCustomerByStripeId(String stripeId);
}
