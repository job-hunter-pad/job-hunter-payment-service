package jobhunter.payment.service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
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
    private final String webhookSecret;

    public StripeWebhookController(JobHunterPaymentService jobHunterPaymentService, JobOfferPaymentProducer jobOfferPaymentProducer) {
        this.jobHunterPaymentService = jobHunterPaymentService;
        this.jobOfferPaymentProducer = jobOfferPaymentProducer;

        webhookSecret = System.getenv("WEB_HOOK_KEY");
    }

    @PostMapping("/webhook")
    public String handleStripeEvents(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        if (sigHeader == null) {
            return "";
        }

        if (webhookSecret == null || webhookSecret.isEmpty()) {
            System.err.println("No WEB HOOK SECRET");
            return "";
        }

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

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

            updateDBAndKafka(paymentIntent.getMetadata(), paymentIntent.getId(), paymentIntent.getStatus(), paymentIntent.getAmount());
        } else if ("charge.succeeded".equals(event.getType())) {
            Charge charge = (Charge) stripeObject;

            updateDBAndKafka(charge.getMetadata(), charge.getId(), charge.getStatus(), charge.getAmount());
        } else {
            System.out.println("Unhandled event type: " + event.getType());
        }

        return "";
    }

    private void updateDBAndKafka(Map<String, String> metadata, String id, String status, long amount) {

        String jobId = metadata.get("jobId");
        String jobName = metadata.get("jobName");
        String employerId = metadata.get("employerId");
        String freelancerId = metadata.get("freelancerId");

        JobOfferPayment jobOfferPayment = new JobOfferPayment(id, status,
                amount / 100.0f, jobId, jobName, employerId, freelancerId);

        jobHunterPaymentService.addPayment(employerId, jobOfferPayment);

        jobOfferPaymentProducer.postJobOfferPayment(jobOfferPayment);

        System.out.println("Payment succeeded for " + amount);
    }
}
