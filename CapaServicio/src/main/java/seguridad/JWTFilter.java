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
import java.util.Set;

/**
 * Filtro JWT para validar tokens en peticiones HTTP.
 * Rutas públicas permitidas sin token: /login, /register, /login-google.
 * Todo lo demás requiere Bearer token válido.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {

    @Inject
    private JWTUtil jwtUtil;

    @Inject
    private chat.Manejadores.ManejadorTokenBlacklist blacklistHandler;

    private static final String BEARER_PREFIX = "Bearer ";

    // Rutas públicas que no requieren autenticación.
    private static final Set<String> RUTAS_PUBLICAS = Set.of(
        "usuarios/login",
        "usuarios/register",
        "usuarios/login-google"
    );

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Permitir rutas públicas sin token.
        if (esRutaPublica(path)) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Sin token en ruta protegida → 401.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorMessage("Authentication required"))
                            .build());
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // Token inválido, expirado o en la blacklist → 401.
        if (!jwtUtil.esValido(token) || blacklistHandler.estaInvalidado(token)) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorMessage("Invalid or expired token"))
                            .build());
            return;
        }

        Long usuarioId = jwtUtil.extraerUsuarioId(token);
        String username = jwtUtil.extraerUsername(token);
        String role = jwtUtil.extraerRole(token);

        if (usuarioId == null || username == null) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorMessage("Invalid token claims"))
                            .build());
            return;
        }

        // Establecer SecurityContext con id, username y role del JWT.
        final String finalRole = role != null ? role : "USUARIO";
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                // El principal expone el userId numérico.
                return () -> usuarioId.toString();
            }

            @Override
            public boolean isUserInRole(String r) {
                return finalRole.equalsIgnoreCase(r);
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

    private boolean esRutaPublica(String path) {
        if (path != null && path.contains("archivos/descargar/")) {
            return true;
        }
        for (String ruta : RUTAS_PUBLICAS) {
            if (path != null && path.endsWith(ruta)) return true;
        }
        return false;
    }

    /** Clase interna para respuestas de error de autenticación. */
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