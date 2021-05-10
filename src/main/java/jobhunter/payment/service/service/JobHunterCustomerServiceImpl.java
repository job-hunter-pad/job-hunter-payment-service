package jobhunter.payment.service.service;

import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.JobHunterCustomer;
import jobhunter.payment.service.models.JobOfferPayment;
import jobhunter.payment.service.models.JobOfferPaymentStatus;
import jobhunter.payment.service.repository.JobHunterCustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobHunterCustomerServiceImpl implements JobHunterCustomerService {

    private final JobHunterCustomerRepository jobHunterCustomerRepository;

    public JobHunterCustomerServiceImpl(JobHunterCustomerRepository jobHunterCustomerRepository) {
        this.jobHunterCustomerRepository = jobHunterCustomerRepository;
    }

    @Override
    public JobHunterCustomer createCustomer(CreateCustomerDTO createCustomerDTO, String stripeId) {
        JobHunterCustomer jobHunterCustomer = new JobHunterCustomer(createCustomerDTO.getUserId(), stripeId, createCustomerDTO.getCustomerType(),
                createCustomerDTO.getName(), createCustomerDTO.getEmail(), createCustomerDTO.getPhoneNumber(),
                createCustomerDTO.getLocation(), new ArrayList<>());

        return jobHunterCustomerRepository.save(jobHunterCustomer);
    }

    @Override
    public Optional<JobHunterCustomer> updateCustomer(UpdateCustomerDTO updateCustomerDTO) {
        Optional<JobHunterCustomer> optional = jobHunterCustomerRepository.findById(updateCustomerDTO.getUserId());
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        JobHunterCustomer jobHunterCustomer = optional.get();

        if (updateCustomerDTO.getLocation() != null) {
            jobHunterCustomer.setLocation(updateCustomerDTO.getLocation());
        }

        if (updateCustomerDTO.getName() != null) {
            jobHunterCustomer.setName(updateCustomerDTO.getName());
        }

        if (updateCustomerDTO.getPhoneNumber() != null) {
            jobHunterCustomer.setPhoneNumber(updateCustomerDTO.getPhoneNumber());
        }

        return Optional.of(jobHunterCustomerRepository.save(jobHunterCustomer));
    }

    @Override
    public Optional<JobHunterCustomer> getCustomer(String userId) {
        return jobHunterCustomerRepository.findById(userId);
    }

    @Override
    public Optional<JobHunterCustomer> getCustomerByStripeId(String stripeId) {
        return jobHunterCustomerRepository.findByStripeId(stripeId);
    }

    @Override
    public JobOfferPayment addPayment(JobHunterCustomer jobHunterCustomer, PaymentDTO paymentDTO, String stripeId) {
        JobOfferPayment jobOfferPayment = new JobOfferPayment(stripeId, JobOfferPaymentStatus.REQUIRES_PAYMENT_METHOD,
                paymentDTO.getAmount(), paymentDTO.getJobId(), paymentDTO.getEmployerId(), paymentDTO.getFreelancerId());
        jobHunterCustomer.getPayments().add(jobOfferPayment);

        jobHunterCustomerRepository.save(jobHunterCustomer);
        return jobOfferPayment;
    }

    @Override
    public Optional<JobOfferPayment> updatePaymentStatus(JobHunterCustomer jobHunterCustomer, String paymentStripeId, JobOfferPaymentStatus status) {
        List<JobOfferPayment> payments = jobHunterCustomer.getPayments();
        for (JobOfferPayment payment : payments) {
            if (payment.getStripeId().equals(paymentStripeId)) {
                payment.setStatus(status);

                jobHunterCustomerRepository.save(jobHunterCustomer);

                return Optional.of(payment);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<JobOfferPayment>> getPayments(String employerId, JobOfferPaymentStatus paymentStatus) {

        Optional<JobHunterCustomer> optional = jobHunterCustomerRepository.findById(employerId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        List<JobOfferPayment> payments = optional.get().getPayments().stream()
                .filter(jobOfferPayment -> jobOfferPayment.getStatus().equals(paymentStatus))
                .collect(Collectors.toList());

        return Optional.of(payments);
    }
}
