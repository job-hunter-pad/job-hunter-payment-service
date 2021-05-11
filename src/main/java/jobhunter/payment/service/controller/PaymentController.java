package jobhunter.payment.service.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.models.CheckoutSession;
import jobhunter.payment.service.models.CustomerType;
import jobhunter.payment.service.models.JobHunterCustomer;
import jobhunter.payment.service.models.JobOfferPayment;
import jobhunter.payment.service.service.JobHunterCustomerService;
import jobhunter.payment.service.service.JobHunterPaymentService;
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
    private final JobHunterPaymentService jobHunterPaymentService;

    public PaymentController(JobHunterCustomerService jobHunterCustomerService, JobHunterPaymentService jobHunterPaymentService) {
        this.jobHunterCustomerService = jobHunterCustomerService;
        this.jobHunterPaymentService = jobHunterPaymentService;
    }

    @PostMapping("/checkout")
    public CheckoutSession paymentWithCheckoutPage(@RequestBody PaymentDTO paymentDTO) {

        Optional<JobHunterCustomer> customerOptional = jobHunterCustomerService.getCustomer(paymentDTO.getEmployerId());
        JobHunterCustomer jobHunterCustomer;

        if (customerOptional.isEmpty()) {
            try {

                CreateCustomerDTO createCustomerDTO = new CreateCustomerDTO();
                createCustomerDTO.setUserId(paymentDTO.getEmployerId());
                createCustomerDTO.setCustomerType(CustomerType.EMPLOYER);

                jobHunterCustomer = jobHunterCustomerService.createCustomer(createCustomerDTO);
            } catch (StripeException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            jobHunterCustomer = customerOptional.get();
        }

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

    @GetMapping("/getPayments/{employerId}")
    public List<JobOfferPayment> getNotConfirmedPayments(@PathVariable String employerId) {
        return jobHunterPaymentService.getPayments(employerId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
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
        try {
            return jobHunterCustomerService.createCustomer(createCustomerDTO);
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updateCustomer")
    public JobHunterCustomer updateCustomer(@RequestBody UpdateCustomerDTO updateCustomerDTO) {
        try {
            return jobHunterCustomerService.updateCustomer(updateCustomerDTO).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND));
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
