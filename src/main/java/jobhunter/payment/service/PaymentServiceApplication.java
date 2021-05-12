package jobhunter.payment.service;

import com.stripe.Stripe;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {

        String stripeApiKey = System.getenv("STRIPE_API_KEY");
        if (stripeApiKey != null && !stripeApiKey.isEmpty()) {
            Stripe.apiKey = stripeApiKey;
        }

        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
