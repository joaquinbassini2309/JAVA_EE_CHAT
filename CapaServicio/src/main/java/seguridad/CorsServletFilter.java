package seguridad;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Servlet-level CORS filter que intercepta OPTIONS antes de JAX-RS.
 * Esto resuelve el problema de WildFly rechazando OPTIONS en 405.
 */
@WebFilter(urlPatterns = "/*")
public class CorsServletFilter implements Filter {

    private static final String DEFAULT_ORIGINS = String.join(",",
            "https://java-ee-chat.onrender.com",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String origin = httpRequest.getHeader("Origin");
        String method = httpRequest.getMethod();

        System.out.println("[CorsServletFilter] received " + method + " from origin '" + origin + "'");

        // Determinar si el origen es permitido
        String allowedOrigin = resolveAllowedOrigin(origin);

        if (allowedOrigin != null) {
            System.out.println("[CorsServletFilter] origin '" + allowedOrigin + "' permitido");
            httpResponse.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
            httpResponse.setHeader("Access-Control-Expose-Headers", "Location, Content-Type, Authorization");
            httpResponse.setHeader("Vary", "Origin");
        }

        // Responder a preflight OPTIONS con 200
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("[CorsServletFilter] preflight OPTIONS -> devolviendo 200");
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveAllowedOrigin(String requestOrigin) {
        if (requestOrigin == null || requestOrigin.isBlank()) {
            return null;
        }

        Set<String> origins = getAllowedOrigins();
        if (origins.contains("*") || origins.contains("ALLOW_ALL")) {
            return requestOrigin;
        }

        return origins.contains(requestOrigin) ? requestOrigin : null;
    }

    private Set<String> getAllowedOrigins() {
        String configured = System.getenv("CORS_ALLOWED_ORIGINS");
        String source = (configured == null || configured.isBlank()) ? DEFAULT_ORIGINS : configured;
        Set<String> origins = new HashSet<>();
        Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(origins::add);
        return origins;
    }
}

