package jobhunter.payment.service.service.impl;

import jobhunter.payment.service.models.JobHunterCustomer;
import jobhunter.payment.service.models.JobOfferPayment;
import jobhunter.payment.service.repository.JobHunterCustomerRepository;
import jobhunter.payment.service.service.JobHunterPaymentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobHunterPaymentServiceImpl implements JobHunterPaymentService {

    private final JobHunterCustomerRepository jobHunterCustomerRepository;

    public JobHunterPaymentServiceImpl(JobHunterCustomerRepository jobHunterCustomerRepository) {
        this.jobHunterCustomerRepository = jobHunterCustomerRepository;
    }

    @Override
    public Optional<JobOfferPayment> addPayment(String employerId, JobOfferPayment jobOfferPayment) {

        Optional<JobHunterCustomer> optional = jobHunterCustomerRepository.findById(employerId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        JobHunterCustomer jobHunterCustomer = optional.get();
        jobHunterCustomer.getPayments().add(jobOfferPayment);

        jobHunterCustomerRepository.save(jobHunterCustomer);

        return Optional.of(jobOfferPayment);
    }

    @Override
    public Optional<List<JobOfferPayment>> getPayments(String employerId) {
        Optional<JobHunterCustomer> optional = jobHunterCustomerRepository.findById(employerId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        List<JobOfferPayment> payments = optional.get().getPayments();
        return Optional.of(payments);
    }
}
