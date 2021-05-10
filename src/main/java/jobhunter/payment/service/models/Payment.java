package jobhunter.payment.service.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class Payment {
    @Id
    private String id;

    private String jobId;
    private String employerId;
    private String freelancerId;
    private float amount;

    public Payment(String jobId, String employerId, String freelancerId, float amount) {
        this.jobId = jobId;
        this.employerId = employerId;
        this.freelancerId = freelancerId;
        this.amount = amount;
    }
}
