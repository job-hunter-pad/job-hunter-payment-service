package jobhunter.payment.service;

import com.stripe.Stripe;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {

        String stripe_key = System.getenv("STRIPE_KEY");
        if (stripe_key != null && !stripe_key.isEmpty()) {
            Stripe.apiKey = stripe_key;
        }

        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
