package jobhunter.payment.service.interceptor;

import org.springframework.stereotype.Service;

@Service
public class BearerExtractorImpl implements BearerExtractor {
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public String extract(String header) {
        if (header == null || header.isEmpty()) {
            return "";
        }

        if (!header.startsWith(TOKEN_PREFIX)) {
            return "";
        }

        return header.replace(TOKEN_PREFIX, "");
    }
}