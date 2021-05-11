package jobhunter.payment.service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import jobhunter.payment.service.kafka.producer.JobOfferPaymentProducer;
import jobhunter.payment.service.models.payment.JobOfferPayment;
import jobhunter.payment.service.service.JobHunterPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class StripeWebhookController {
    private final JobHunterPaymentService jobHunterPaymentService;
    private final JobOfferPaymentProducer jobOfferPaymentProducer;

    public StripeWebhookController(JobHunterPaymentService jobHunterPaymentService, JobOfferPaymentProducer jobOfferPaymentProducer) {
        this.jobHunterPaymentService = jobHunterPaymentService;
        this.jobOfferPaymentProducer = jobOfferPaymentProducer;
    }

    @PostMapping("/webhook")
    public String handleStripeEvents(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        if (sigHeader == null) {
            return "";
        }


        // If you are testing your webhook locally with the Stripe CLI you
        // can find the endpoint's secret by running `stripe listen`
        // Otherwise, find your endpoint's secret in your webhook settings in the Developer Dashboard
        String endpointSecret = "whsec_pJSLAljBoNu2Ewd5FB5g8z7lhFek1HV7";

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

        } catch (SignatureVerificationException e) {
            System.out.println("Invalid Signature");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();

        }

        System.out.println(event.getType());
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;

            Map<String, String> metadata = paymentIntent.getMetadata();

            String jobId = metadata.get("jobId");
            String jobName = metadata.get("jobName");
            String employerId = metadata.get("employerId");
            String freelancerId = metadata.get("freelancerId");

            Long amount = paymentIntent.getAmount();

            JobOfferPayment jobOfferPayment = new JobOfferPayment(paymentIntent.getId(), paymentIntent.getStatus(),
                    amount / 100.0f, jobId, jobName, employerId, freelancerId);

            jobHunterPaymentService.addPayment(employerId, jobOfferPayment);

            jobOfferPaymentProducer.postJobOfferPayment(jobOfferPayment);

            System.out.println("Payment succeeded for " + amount);
        } else {
            System.out.println("Unhandled event type: " + event.getType());
        }

        return "";
    }
}
