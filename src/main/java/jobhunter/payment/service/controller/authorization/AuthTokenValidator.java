package jobhunter.payment.service.controller.authorization;

public interface AuthTokenValidator {
    boolean authorize(String id, String token);
}
