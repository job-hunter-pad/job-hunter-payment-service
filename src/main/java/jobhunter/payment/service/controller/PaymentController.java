package jobhunter.payment.service.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.JobHunterCustomer;
import jobhunter.payment.service.models.JobOfferPayment;
import jobhunter.payment.service.models.JobOfferPaymentStatus;
import jobhunter.payment.service.service.JobHunterCustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class PaymentController {
    private final JobHunterCustomerService jobHunterCustomerService;

    public PaymentController(JobHunterCustomerService jobHunterCustomerService) {
        this.jobHunterCustomerService = jobHunterCustomerService;
    }

    @PostMapping("/pay")
    public JobOfferPayment makePayment(@RequestBody PaymentDTO paymentDTO) {

        Optional<JobHunterCustomer> customerOptional = jobHunterCustomerService.getCustomer(paymentDTO.getEmployerId());
        if (customerOptional.isEmpty()) {
            System.out.println("Employer not found");
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        JobHunterCustomer jobHunterCustomer = customerOptional.get();

        Map<String, String> metaData = new HashMap<>();
        metaData.put("jobId", paymentDTO.getJobId());
        metaData.put("employerId", paymentDTO.getEmployerId());
        metaData.put("freelancerId", paymentDTO.getFreelancerId());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(paymentDTO.convertAmount())
                .setCurrency("USD")
                .setCustomer(jobHunterCustomer.getStripeId())
                .putAllMetadata(metaData)
                .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return jobHunterCustomerService.addPayment(jobHunterCustomer, paymentDTO, paymentIntent.getId());


        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/confirmPayment/{paymentId}")
    public JobOfferPayment confirmPayment(@PathVariable String paymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);

            PaymentIntentConfirmParams paymentIntentConfirmParams = PaymentIntentConfirmParams.builder()
                    .setPaymentMethod("pm_card_visa")
                    .build();

            PaymentIntent confirmedPaymentIntent = paymentIntent.confirm(paymentIntentConfirmParams);

            Map<String, String> metadata = confirmedPaymentIntent.getMetadata();
            if (!metadata.containsKey("employerId")) {
                System.out.println("No employerId found in Metadata");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            String employerId = metadata.get("employerId");

            Optional<JobHunterCustomer> optional = jobHunterCustomerService.getCustomer(employerId);
            if (optional.isEmpty()) {
                System.out.println("Employer not found");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            return jobHunterCustomerService.updatePaymentStatus(optional.get(), confirmedPaymentIntent.getId(), JobOfferPaymentStatus.SUCCEEDED)
                    .orElseThrow(() -> {
                        System.out.println("Payment not found in Customer Payments");
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                    });

        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getNotConfirmedPayments/{employerId}")
    public List<JobOfferPayment> getNotConfirmedPayments(@PathVariable String employerId) {
        return jobHunterCustomerService.getPayments(employerId, JobOfferPaymentStatus.REQUIRES_PAYMENT_METHOD).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
    }

    @GetMapping("/getSucceededPayments/{employerId}")
    public List<JobOfferPayment> getSucceededPayments(@PathVariable String employerId) {
        return jobHunterCustomerService.getPayments(employerId, JobOfferPaymentStatus.SUCCEEDED).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
    }

    @PostMapping("/cancelPayment/{paymentId}")
    public String cancelPayment(@PathVariable String paymentId) {

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
            return paymentIntent.cancel().toJson();
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getCustomer/{userId}")
    public JobHunterCustomer getCustomer(@PathVariable String userId) {
        return jobHunterCustomerService.getCustomer(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/getCustomerByStripeId/{stripeId}")
    public JobHunterCustomer getCustomerByStripeId(@PathVariable String stripeId) {
        return jobHunterCustomerService.getCustomerByStripeId(stripeId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/createCustomer")
    public JobHunterCustomer createCustomer(@RequestBody CreateCustomerDTO createCustomerDTO) {

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

        JobHunterCustomer jobHunterCustomer;
        try {
            Customer customer = Customer.create(customerCreateParams);
            jobHunterCustomer = jobHunterCustomerService.createCustomer(createCustomerDTO, customer.getId());

        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return jobHunterCustomer;
    }

    @PostMapping("/updateCustomer")
    public JobHunterCustomer updateCustomer(@RequestBody UpdateCustomerDTO updateCustomerDTO) {
        Optional<JobHunterCustomer> optional = jobHunterCustomerService.updateCustomer(updateCustomerDTO);
        if (optional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        JobHunterCustomer updatedJobHunterCustomer = optional.get();

        CustomerUpdateParams customerUpdateParams = CustomerUpdateParams.builder()
                .setName(updatedJobHunterCustomer.getName())
                .setPhone(updatedJobHunterCustomer.getPhoneNumber())
                .setAddress(
                        CustomerUpdateParams.Address.builder()
                                .setLine1(updatedJobHunterCustomer.getLocation())
                                .build()
                )
                .build();

        try {
            Customer customer = Customer.retrieve(updatedJobHunterCustomer.getStripeId());
            customer.update(customerUpdateParams);
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return updatedJobHunterCustomer;
    }
}
