package jobhunter.payment.service.kafka.consumer;

import com.stripe.exception.StripeException;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.customer.CustomerType;
import jobhunter.payment.service.models.customer.JobHunterCustomer;
import jobhunter.payment.service.models.customer.UserProfile;
import jobhunter.payment.service.service.JobHunterCustomerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileConsumer {

    private final JobHunterCustomerService jobHunterCustomerService;

    public UserProfileConsumer(JobHunterCustomerService jobHunterCustomerService) {
        this.jobHunterCustomerService = jobHunterCustomerService;
    }

    @KafkaListener(topics = "profile", groupId = "group_profile_payment", containerFactory = "userProfileKafkaListenerContainerFactory")
    public void consumeJobApplication(UserProfile userProfile) {
        Optional<JobHunterCustomer> optional = jobHunterCustomerService.getCustomer(userProfile.getUserId());

        try {
            if (optional.isPresent()) {
                UpdateCustomerDTO updateCustomerDTO = new UpdateCustomerDTO();

                updateCustomerDTO.setUserId(userProfile.getUserId());
                updateCustomerDTO.setName(userProfile.getName());
                updateCustomerDTO.setEmail(userProfile.getEmail());
                updateCustomerDTO.setPhoneNumber(userProfile.getPhoneNumber());
                updateCustomerDTO.setLocation(userProfile.getLocation());

                jobHunterCustomerService.updateCustomer(updateCustomerDTO);

            } else {
                CreateCustomerDTO createCustomerDTO = new CreateCustomerDTO();
                createCustomerDTO.setUserId(userProfile.getUserId());

                if (userProfile.getUserType().equals("EMPLOYER")) {
                    createCustomerDTO.setCustomerType(CustomerType.EMPLOYER);
                } else if (userProfile.getUserType().equals("FREELANCER")) {
                    createCustomerDTO.setCustomerType(CustomerType.FREELANCER);
                }
                createCustomerDTO.setName(userProfile.getName());
                createCustomerDTO.setEmail(userProfile.getEmail());
                createCustomerDTO.setPhoneNumber(userProfile.getPhoneNumber());
                createCustomerDTO.setLocation(userProfile.getLocation());

                jobHunterCustomerService.createCustomer(createCustomerDTO);
            }

        } catch (StripeException e) {
            e.printStackTrace();
        }
        System.out.println("Consumed JSON Message: " + userProfile);
    }

}
