package jobhunter.payment.service.controller.dto;

import jobhunter.payment.service.models.CustomerType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCustomerDTO {
    private String userId;
    private CustomerType customerType;
    private String name;
    private String email;
    private String phoneNumber;
    private String location;
}
