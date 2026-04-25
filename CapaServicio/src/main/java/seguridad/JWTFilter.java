package seguridad;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.security.Principal;

/**
 * Filtro JWT para validar tokens en peticiones HTTP.
 * Extrae el token del header Authorization (Bearer <token>)
 * y lo valida usando JWTUtil.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {

    @Inject
    private JWTUtil jwtUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Si no hay header Authorization, continuar sin autenticación
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // Validar token
        if (!jwtUtil.esValido(token)) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorMessage("Invalid or expired token"))
                            .build());
            return;
        }

        // Extraer información del usuario desde el token
        Long usuarioId = jwtUtil.extraerUsuarioId(token);
        String username = jwtUtil.extraerUsername(token);

        if (usuarioId == null || username == null) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorMessage("Invalid token claims"))
                            .build());
            return;
        }

        // Establecer el SecurityContext con la información del usuario
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> usuarioId.toString(); // El principal es el ID del usuario
            }

            @Override
            public boolean isUserInRole(String role) {
                // TODO: Implementar roles si es necesario
                return false;
            }

            @Override
            public boolean isSecure() {
                return requestContext.getSecurityContext().isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });
    }

    /**
     * Clase interna para respuestas de error.
     */
    public static class ErrorMessage {
        public String message;

        public ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}