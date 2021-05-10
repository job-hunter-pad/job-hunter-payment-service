package jobhunter.payment.service.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StripeWebhookController {
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

        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
        }

        System.out.println(event.getType());
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            System.out.println("Payment succeeded for " + paymentIntent.getAmount());
        } else {
            System.out.println("Unhandled event type: " + event.getType());
        }


        return "";
    }
}
