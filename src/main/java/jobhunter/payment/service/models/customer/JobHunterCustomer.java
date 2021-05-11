package jobhunter.payment.service.models.customer;

import jobhunter.payment.service.models.payment.JobOfferPayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobHunterCustomer {
    @Id
    private String userId;
    private String stripeId;

    private CustomerType customerType;
    private String name;
    private String email;
    private String phoneNumber;
    private String location;
    private List<JobOfferPayment> payments = new ArrayList<>();

    public JobHunterCustomer(String userId, CustomerType customerType, String name, String email) {
        this.userId = userId;
        this.customerType = customerType;
        this.name = name;
        this.email = email;
    }
}
