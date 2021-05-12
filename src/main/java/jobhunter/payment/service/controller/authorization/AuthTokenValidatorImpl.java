package jobhunter.payment.service.controller.authorization;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthTokenValidatorImpl implements AuthTokenValidator {
    @Override
    public boolean authorize(String id, String token) {

        String validateIdUrl = System.getenv("AUTH_VERIFICATION_URL");

        RestTemplate restTemplate = new RestTemplate();
        ValidateIdRequest validateIdRequest = new ValidateIdRequest(id, token);

        ValidateIdResponse result;
        try {
            result = restTemplate.postForObject(validateIdUrl, validateIdRequest, ValidateIdResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (result == null) {
            return false;
        }

        return result.isValid();
    }
}
