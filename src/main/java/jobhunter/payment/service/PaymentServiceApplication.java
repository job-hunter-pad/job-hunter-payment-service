package jobhunter.payment.service;

import com.stripe.Stripe;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {

        Stripe.apiKey = args[0];

        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
