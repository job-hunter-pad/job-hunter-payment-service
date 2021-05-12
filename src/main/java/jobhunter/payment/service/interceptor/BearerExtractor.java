package jobhunter.payment.service.interceptor;

public interface BearerExtractor {
    String extract(String header);
}