package jobhunter.payment.service.kafka.producer;

import jobhunter.payment.service.models.payment.JobOfferPayment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobOfferPaymentProducer {

    private final KafkaTemplate<String, JobOfferPayment> jobOfferPaymentKafkaTemplate;

    private static final String TOPIC = "payment";

    public JobOfferPaymentProducer(KafkaTemplate<String, JobOfferPayment> jobOfferPaymentKafkaTemplate) {
        this.jobOfferPaymentKafkaTemplate = jobOfferPaymentKafkaTemplate;
    }

    public String postJobOfferPayment(JobOfferPayment jobApplication) {
        jobOfferPaymentKafkaTemplate.send(TOPIC, jobApplication);
        return "Published successfully";
    }
}
