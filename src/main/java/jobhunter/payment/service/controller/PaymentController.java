package jobhunter.payment.service.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.CheckoutSession;
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

    @PostMapping("/checkout")
    public CheckoutSession paymentWithCheckoutPage(@RequestBody PaymentDTO paymentDTO) {

        Optional<JobHunterCustomer> customerOptional = jobHunterCustomerService.getCustomer(paymentDTO.getEmployerId());
        if (customerOptional.isEmpty()) {
            System.out.println("Employer not found");
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        JobHunterCustomer jobHunterCustomer = customerOptional.get();

        Map<String, String> paymentMetaData = new HashMap<>();
        paymentMetaData.put("jobId", paymentDTO.getJobId());
        paymentMetaData.put("jobName", paymentDTO.getJobName());
        paymentMetaData.put("employerId", paymentDTO.getEmployerId());
        paymentMetaData.put("freelancerId", paymentDTO.getFreelancerId());

        Map<String, Object> paymentIntentData = new HashMap<>();
        paymentIntentData.put("metadata", paymentMetaData);

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(paymentDTO.convertAmount())
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData
                                .builder()
                                .setName(paymentDTO.getJobName())
                                .build())
                        .build())
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomer(jobHunterCustomer.getStripeId())
                .setSuccessUrl(paymentDTO.getSuccessUrl())
                .setCancelUrl(paymentDTO.getCancelUrl())
                .putExtraParam("payment_intent_data", paymentIntentData)
                .addLineItem(lineItem)
                .build();

        try {
            Session session = Session.create(params);
            return new CheckoutSession(session.getId());
        } catch (StripeException e) {
            System.out.println("Session could not be created!");
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
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
