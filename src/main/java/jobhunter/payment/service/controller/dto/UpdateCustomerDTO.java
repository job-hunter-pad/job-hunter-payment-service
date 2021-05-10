package jobhunter.payment.service.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCustomerDTO {
    private String userId;
    private String name;
    private String phoneNumber;
    private String location;
}
