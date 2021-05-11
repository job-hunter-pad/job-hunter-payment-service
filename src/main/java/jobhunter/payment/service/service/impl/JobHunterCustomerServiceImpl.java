package jobhunter.payment.service.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.customer.JobHunterCustomer;
import jobhunter.payment.service.repository.JobHunterCustomerRepository;
import jobhunter.payment.service.service.JobHunterCustomerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class JobHunterCustomerServiceImpl implements JobHunterCustomerService {

    private final JobHunterCustomerRepository jobHunterCustomerRepository;

    public JobHunterCustomerServiceImpl(JobHunterCustomerRepository jobHunterCustomerRepository) {
        this.jobHunterCustomerRepository = jobHunterCustomerRepository;
    }

    @Override
    public JobHunterCustomer createCustomer(CreateCustomerDTO createCustomerDTO) throws StripeException {
        CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                .setName(createCustomerDTO.getName())
                .setEmail(createCustomerDTO.getEmail())
                .setPhone(createCustomerDTO.getPhoneNumber())
                .setAddress(
                        CustomerCreateParams.Address.builder()
                                .setLine1(createCustomerDTO.getLocation())
                                .build()
                )
                .build();


        Customer customer = Customer.create(customerCreateParams);

        JobHunterCustomer jobHunterCustomer = new JobHunterCustomer(createCustomerDTO.getUserId(),
                customer.getId(), createCustomerDTO.getCustomerType(),
                createCustomerDTO.getName(), createCustomerDTO.getEmail(), createCustomerDTO.getPhoneNumber(),
                createCustomerDTO.getLocation(), new ArrayList<>());

        return jobHunterCustomerRepository.save(jobHunterCustomer);
    }

    @Override
    public Optional<JobHunterCustomer> updateCustomer(UpdateCustomerDTO updateCustomerDTO) throws StripeException {
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

        if (updateCustomerDTO.getEmail() != null) {
            jobHunterCustomer.setEmail(updateCustomerDTO.getEmail());
        }

        jobHunterCustomerRepository.save(jobHunterCustomer);

        CustomerUpdateParams customerUpdateParams = CustomerUpdateParams.builder()
                .setName(jobHunterCustomer.getName())
                .setPhone(jobHunterCustomer.getPhoneNumber())
                .setEmail(updateCustomerDTO.getEmail())
                .setAddress(
                        CustomerUpdateParams.Address.builder()
                                .setLine1(jobHunterCustomer.getLocation())
                                .build()
                )
                .build();

        Customer customer = Customer.retrieve(jobHunterCustomer.getStripeId());
        customer.update(customerUpdateParams);

        return Optional.of(jobHunterCustomer);
    }

    @Override
    public Optional<JobHunterCustomer> getCustomer(String userId) {
        return jobHunterCustomerRepository.findById(userId);
    }

    @Override
    public Optional<JobHunterCustomer> getCustomerByStripeId(String stripeId) {
        return jobHunterCustomerRepository.findByStripeId(stripeId);
    }
}
