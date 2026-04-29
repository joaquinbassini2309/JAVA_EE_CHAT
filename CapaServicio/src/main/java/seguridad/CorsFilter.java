package seguridad;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION - 1)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String DEFAULT_ORIGINS = String.join(",",
            "https://java-ee-chat.onrender.com",
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:8080");

    private static Set<String> allowedOrigins() {
        String configured = System.getenv("CORS_ALLOWED_ORIGINS");
        String source = (configured == null || configured.isBlank()) ? DEFAULT_ORIGINS : configured;
        Set<String> origins = new HashSet<>();
        Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(origins::add);
        return origins;
    }

    private static String resolveAllowedOrigin(String requestOrigin) {
        if (requestOrigin == null || requestOrigin.isBlank()) {
            return null;
        }
        return allowedOrigins().contains(requestOrigin) ? requestOrigin : null;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            String origin = resolveAllowedOrigin(requestContext.getHeaderString("Origin"));
            if (origin == null) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }
            requestContext.abortWith(Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String origin = resolveAllowedOrigin(requestContext.getHeaderString("Origin"));
        if (origin == null) {
            return;
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
        responseContext.getHeaders().putSingle("Vary", "Origin");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        responseContext.getHeaders().putSingle("Access-Control-Expose-Headers", "Location, Content-Type, Authorization");
    }
}


