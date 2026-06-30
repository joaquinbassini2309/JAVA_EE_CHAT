package seguridad;

import chat.Sistema.ISistema;
import chat.clases.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Servicio de autenticación.
 * Resuelve el userId del SecurityContext directamente desde el principal numérico del JWT.
 */
@ApplicationScoped
public class AuthService {

    @Inject
    private ISistema sistema;

    @Inject
    private JWTUtil jwtUtil;

    @Inject
    private chat.Manejadores.ManejadorTokenBlacklist blacklistHandler;

    /**
     * Obtiene el id del usuario autenticado a partir del SecurityContext.
     * El principal del JWT siempre es el userId numérico (ver JWTFilter).
     */
    public Long getAuthenticatedUserId(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        String principalName = sc.getUserPrincipal().getName();
        if (principalName == null || principalName.isBlank()) return null;

        // El principal es siempre el userId numérico — sin reflexión.
        try {
            return Long.parseLong(principalName);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Devuelve el nombre del principal si está presente. */
    public String getAuthenticatedUsername(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        String principalName = sc.getUserPrincipal().getName();
        return (principalName == null || principalName.isBlank()) ? null : principalName;
    }

    /**
     * Extrae el token JWT del header Authorization (formato Bearer token).
     */
    public String extraerTokenDelEncabezado(String encabezadoAutorizacion) {
        if (encabezadoAutorizacion == null || encabezadoAutorizacion.isBlank()) return null;
        if (!encabezadoAutorizacion.startsWith("Bearer ")) return null;
        return encabezadoAutorizacion.substring("Bearer ".length()).trim();
    }

    /**
     * Valida un token JWT y extrae el ID del usuario.
     */
    public Long validarTokenYExtraerIdUsuario(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return jwtUtil.extraerUsuarioId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Valida un token JWT y extrae el nombre de usuario.
     */
    public String validarTokenYExtraerNombreUsuario(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return jwtUtil.extraerUsername(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un token JWT es válido y no está en la blacklist.
     */
    public boolean esTokenValido(String token) {
        if (token == null || token.isBlank()) return false;
        return jwtUtil.esValido(token) && !blacklistHandler.estaInvalidado(token);
    }
}
