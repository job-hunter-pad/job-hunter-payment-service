package jobhunter.payment.service.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthTokenHTTPInterceptor implements HandlerInterceptor {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final BearerExtractor bearerExtractor;

    public AuthTokenHTTPInterceptor(BearerExtractor bearerExtractor) {
        this.bearerExtractor = bearerExtractor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        String token = bearerExtractor.extract(header);

        if (token != null && !token.isEmpty()) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}