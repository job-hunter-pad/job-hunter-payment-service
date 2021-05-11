package jobhunter.payment.service.models.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserProfile {
    private String userId;
    private String name;
    private String email;
    private String userType;
    private String location;
    private String phoneNumber;
}
