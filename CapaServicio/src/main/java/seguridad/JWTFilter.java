package seguridad;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
// Eliminamos la importación de @Provider
// import jakarta.ws.rs.ext.Provider; 

import java.io.IOException;
import java.security.Principal;

/**
 * Filtro JWT para validar tokens en peticiones HTTP.
 * Se activa solo en los endpoints anotados con @Secured.
 * Ya NO usa @Provider, se registrará manualmente en JAXRSConfiguration.
 */
@Secured // El filtro está vinculado a esta anotación.
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {

    @Inject
    private JWTUtil jwtUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (jwtUtil == null) {
            try {
                jwtUtil = CDI.current().select(JWTUtil.class).get();
            } catch (Exception ignored) {
                requestContext.abortWith(
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(new ErrorMessage("JWT service unavailable"))
                                .build());
                return;
            }
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Si no hay header Authorization, abortar (porque este filtro solo corre en endpoints seguros)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorMessage("Authorization header must be provided"))
                            .build());
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
