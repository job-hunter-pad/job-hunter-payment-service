package jobhunter.payment.service.controller.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateIdRequest {
    private String id;
    private String token;
}
