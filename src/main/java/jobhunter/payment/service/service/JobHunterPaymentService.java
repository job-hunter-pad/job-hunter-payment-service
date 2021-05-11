package jobhunter.payment.service.service;

import jobhunter.payment.service.models.JobOfferPayment;

import java.util.List;
import java.util.Optional;

public interface JobHunterPaymentService {

    Optional<JobOfferPayment> addPayment(String employerId, JobOfferPayment jobOfferPayment);

    Optional<List<JobOfferPayment>> getPayments(String employerId);
}
