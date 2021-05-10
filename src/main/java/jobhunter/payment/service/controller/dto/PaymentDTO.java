package jobhunter.payment.service.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PaymentDTO {
    private String jobId;
    private String jobName;
    private String employerId;
    private String freelancerId;
    private Float amount;
    private String successUrl;
    private String cancelUrl;

    public Long convertAmount() {
        return (long) (amount * 100);
    }
}
