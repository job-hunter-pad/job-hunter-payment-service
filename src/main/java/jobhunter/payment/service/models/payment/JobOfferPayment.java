package jobhunter.payment.service.models.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobOfferPayment {
    private String stripeId;
    private String status;
    private float amount;
    private String jobId;
    private String jobName;
    private String employerId;
    private String freelancerId;
}
