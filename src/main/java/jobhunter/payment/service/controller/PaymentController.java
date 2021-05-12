package jobhunter.payment.service.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jobhunter.payment.service.controller.authorization.AuthTokenValidator;
import jobhunter.payment.service.controller.dto.CreateCustomerDTO;
import jobhunter.payment.service.controller.dto.PaymentDTO;
import jobhunter.payment.service.controller.dto.UpdateCustomerDTO;
import jobhunter.payment.service.interceptor.AuthTokenHTTPInterceptor;
import jobhunter.payment.service.interceptor.BearerExtractor;
import jobhunter.payment.service.models.customer.CustomerType;
import jobhunter.payment.service.models.customer.JobHunterCustomer;
import jobhunter.payment.service.models.payment.CheckoutSession;
import jobhunter.payment.service.models.payment.JobOfferPayment;
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

    private final AuthTokenValidator authTokenValidator;
    private final BearerExtractor bearerExtractor;

    public PaymentController(JobHunterCustomerService jobHunterCustomerService,
                             JobHunterPaymentService jobHunterPaymentService,
                             AuthTokenValidator authTokenValidator,
                             BearerExtractor bearerExtractor) {
        this.jobHunterCustomerService = jobHunterCustomerService;
        this.jobHunterPaymentService = jobHunterPaymentService;
        this.authTokenValidator = authTokenValidator;
        this.bearerExtractor = bearerExtractor;
    }

    @PostMapping("/checkout")
    public CheckoutSession paymentWithCheckoutPage(@RequestBody PaymentDTO paymentDTO, @RequestHeader(AuthTokenHTTPInterceptor.AUTHORIZATION_HEADER) String header) {

        if (!authTokenValidator.authorize(paymentDTO.getEmployerId(), bearerExtractor.extract(header))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

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
    public List<JobOfferPayment> getNotConfirmedPayments(@PathVariable String employerId, @RequestHeader(AuthTokenHTTPInterceptor.AUTHORIZATION_HEADER) String header) {
        if (!authTokenValidator.authorize(employerId, bearerExtractor.extract(header))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return jobHunterPaymentService.getPayments(employerId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
    }

    @GetMapping("/getCustomer/{userId}")
    public JobHunterCustomer getCustomer(@PathVariable String userId, @RequestHeader(AuthTokenHTTPInterceptor.AUTHORIZATION_HEADER) String header) {

        if (!authTokenValidator.authorize(userId, bearerExtractor.extract(header))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return jobHunterCustomerService.getCustomer(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/getCustomerByStripeId/{stripeId}")
    public JobHunterCustomer getCustomerByStripeId(@PathVariable String stripeId, @RequestHeader(AuthTokenHTTPInterceptor.AUTHORIZATION_HEADER) String header) {

        Optional<JobHunterCustomer> customerByStripeIdOptional = jobHunterCustomerService.getCustomerByStripeId(stripeId);

        if (customerByStripeIdOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        JobHunterCustomer jobHunterCustomer = customerByStripeIdOptional.get();

        if (!authTokenValidator.authorize(jobHunterCustomer.getUserId(), bearerExtractor.extract(header))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return jobHunterCustomer;
    }

    @PostMapping("/createCustomer")
    public JobHunterCustomer createCustomer(@RequestBody CreateCustomerDTO createCustomerDTO, @RequestHeader(AuthTokenHTTPInterceptor.AUTHORIZATION_HEADER) String header) {
        if (!authTokenValidator.authorize(createCustomerDTO.getUserId(), bearerExtractor.extract(header))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return jobHunterCustomerService.createCustomer(createCustomerDTO);
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updateCustomer")
    public JobHunterCustomer updateCustomer(@RequestBody UpdateCustomerDTO updateCustomerDTO, @RequestHeader(AuthTokenHTTPInterceptor.AUTHORIZATION_HEADER) String header) {
        if (!authTokenValidator.authorize(updateCustomerDTO.getUserId(), bearerExtractor.extract(header))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return jobHunterCustomerService.updateCustomer(updateCustomerDTO).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND));
        } catch (StripeException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
