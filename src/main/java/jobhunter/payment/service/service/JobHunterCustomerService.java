package jobhunter.payment.service.service;

import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.JobHunterCustomer;
import jobhunter.payment.service.models.JobOfferPayment;
import jobhunter.payment.service.models.JobOfferPaymentStatus;

import java.util.List;
import java.util.Optional;

public interface JobHunterCustomerService {
    JobHunterCustomer createCustomer(CreateCustomerDTO createCustomerDTO, String stripeId);

    Optional<JobHunterCustomer> updateCustomer(UpdateCustomerDTO updateCustomerDTO);

    Optional<JobHunterCustomer> getCustomer(String userId);

    Optional<JobHunterCustomer> getCustomerByStripeId(String stripeId);

    Optional<JobOfferPayment> addPayment(JobHunterCustomer jobHunterCustomer, PaymentDTO paymentDTO, String stripeId);

    Optional<JobOfferPayment> updatePaymentStatus(JobHunterCustomer jobHunterCustomer, String paymentStripeId, JobOfferPaymentStatus status);

    Optional<List<JobOfferPayment>> getPayments(String employerId, JobOfferPaymentStatus paymentStatus);
}
